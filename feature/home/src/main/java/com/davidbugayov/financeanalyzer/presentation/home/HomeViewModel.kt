package com.davidbugayov.financeanalyzer.presentation.home
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.fold
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodFlowUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import timber.log.Timber
import kotlinx.coroutines.flow.first
import androidx.paging.map
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import kotlinx.coroutines.flow.Flow
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property deleteTransactionUseCase UseCase для удаления транзакций
 * @property getTransactionsForPeriodFlowUseCase UseCase для получения транзакций за период с кэшированием
 * @property calculateBalanceMetricsUseCase UseCase для расчета финансовых метрик
 * @property repository Репозиторий для прямого доступа к транзакциям с поддержкой пагинации и подпиской на изменения
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
class HomeViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionsForPeriodFlowUseCase: GetTransactionsForPeriodFlowUseCase,
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase,
    private val repository: TransactionRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val navigationManager: NavigationManager,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    // -------- Paging ------------
    private val pagerTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    val pagedTransactions: Flow<PagingData<Transaction>> = pagerTrigger
        .flatMapLatest {
            val s = _state.value
            val (startDate, endDate) = getPeriodDates(s.currentFilter)
            val pageSize = 50
            if (s.currentFilter == TransactionFilter.ALL) {
                repository.getAllPaged(pageSize)
            } else {
                repository.getByPeriodPaged(startDate, endDate, pageSize)
            }
        }
        .cachedIn(viewModelScope)

    val pagedUiModels: Flow<PagingData<TransactionListItem>> = pagedTransactions
        .map { pagingData ->
            pagingData
                .map { tx -> TransactionListItem.Item(tx) }
                .insertSeparators { before: TransactionListItem.Item?, after: TransactionListItem.Item? ->
                    if (after == null) return@insertSeparators null

                    val beforeDateKey = before?.transaction?.date?.let { dayKey(it) }
                    val afterDateKey = dayKey(after.transaction.date)

                    if (before == null || beforeDateKey != afterDateKey) {
                        TransactionListItem.Header(afterDateKey)
                    } else {
                        null
                    }
                }
        }
        .cachedIn(viewModelScope)

    private fun reloadPaged() { pagerTrigger.tryEmit(Unit) }

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ru"))
    private fun dayKey(date: java.util.Date): String = dateFormatter.format(date)

    init {
        // Первичная инициализация Paging
        pagerTrigger.tryEmit(Unit)
        Timber.d("HomeViewModel initialized")
        subscribeToRepositoryChanges() // Подписываемся на изменения в репозитории

        // Наблюдаем за изменениями балансов
        viewModelScope.launch {
            financialMetrics.balance.collect { balance ->
                // Обновляем баланс без полной перезагрузки транзакций
                _state.update { it.copy(balance = balance) }
            }
        }

        viewModelScope.launch {
            financialMetrics.totalIncome.collect { income ->
                _state.update { it.copy(income = income) }
            }
        }

        viewModelScope.launch {
            financialMetrics.totalExpense.collect { expense ->
                _state.update { it.copy(expense = expense) }
            }
        }
    }

    /**
     * Обрабатывает события экрана Home
     * @param event Событие для обработки
     */
    fun onEvent(event: HomeEvent, context: Context? = null) {
        when (event) {
            is HomeEvent.SetFilter -> {
                // Обновляем только фильтр, не трогаем остальные данные, чтобы избежать мерцания
                _state.update { it.copy(currentFilter = event.filter) }

                // Плавно обновляем данные без очистки UI
                updateDataSmoothly()
            }
            is HomeEvent.LoadTransactions -> {
                Timber.d("HOME: Загрузка транзакций запрошена")
                loadTransactions()
            }
            is HomeEvent.GenerateTestData -> {
                generateAndSaveTestData()
            }
            is HomeEvent.SetShowGroupSummary -> {
                _state.update { it.copy(showGroupSummary = event.show) }
            }
            is HomeEvent.ShowDeleteConfirmDialog -> {
                _state.update { it.copy(transactionToDelete = event.transaction) }
            }

            is HomeEvent.HideDeleteConfirmDialog -> {
                _state.update { it.copy(transactionToDelete = null) }
            }

            is HomeEvent.DeleteTransaction -> {
                deleteTransaction(event.transaction, context)
            }
            is HomeEvent.NavigateToChart -> {
                // Получаем даты для текущего выбранного фильтра
                val (startDate, endDate) = getPeriodDates(state.value.currentFilter)

                // Преобразуем TransactionFilter в PeriodType
                val periodType = when (state.value.currentFilter) {
                    TransactionFilter.TODAY -> "DAY"
                    TransactionFilter.WEEK -> "WEEK"
                    TransactionFilter.MONTH -> "MONTH"
                    TransactionFilter.ALL -> "ALL"
                }

                // Передаем даты и тип периода в параметрах навигации
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(
                        Screen.FinancialStatistics.createRoute(startDate.time, endDate.time, periodType),
                    ),
                )
            }
            is HomeEvent.NavigateToProfile -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.Profile.route))
            }
            is HomeEvent.NavigateToHistory -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.History.route))
            }
            is HomeEvent.NavigateToAddTransaction -> {
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.AddTransaction.createRoute(forceExpense = true)))
            }
            is HomeEvent.EditTransaction -> {
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(Screen.EditTransaction.createRoute(event.transaction.id)),
                )
            }
            else -> {}
        }
    }

    /**
     * Удаляет транзакцию
     * @param transaction Транзакция для удаления
     * @param context Контекст для обновления виджетов (опционально)
     */
    private fun deleteTransaction(transaction: Transaction, context: Context? = null) {
        viewModelScope.launch {
            try {
                Timber.d("HOME: Начинаем удаление транзакции: id=${transaction.id}, сумма=${transaction.amount}, категория=${transaction.category}")

                deleteTransactionUseCase(transaction).fold(
                    onSuccess = {
                        Timber.d("HOME: Транзакция успешно удалена из базы данных")
                        _state.update { it.copy(transactionToDelete = null) }

                        // Логируем событие в аналитику
                        com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logTransactionDeleted(
                            amount = transaction.amount.abs(),
                            category = transaction.category,
                            isExpense = transaction.isExpense,
                        )

                        context?.let { ctx ->
                            updateWidgetsUseCase()
                            Timber.d(
                                "Виджеты обновлены после удаления транзакции из HomeViewModel.",
                            )
                        } ?: Timber.w(
                            "Context не предоставлен в HomeViewModel, виджеты не обновлены после удаления.",
                        )

                        Timber.d("HOME: Удаление транзакции завершено успешно")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "HOME: Ошибка при удалении транзакции: ${exception.message}")
                        _state.update {
                            it.copy(
                                error = exception.message ?: "Failed to delete transaction",
                                transactionToDelete = null,
                            )
                        }
                    },
                )
            } catch (e: Exception) {
                Timber.e(e, "HOME: Исключение при удалении транзакции: ${e.message}")
                _state.update {
                    it.copy(
                        error = e.message ?: "Error deleting transaction",
                        transactionToDelete = null,
                    )
                }
            }
        }
    }

    /**
     * Подписываемся на изменения данных в репозитории
     */
    private fun subscribeToRepositoryChanges() {
        viewModelScope.launch {
            Timber.d("HOME: Подписываемся на изменения данных в репозитории")
            repository.dataChangeEvents.collect { event ->
                Timber.d("HOME: Получено событие изменения данных: $event")
                when (event) {
                    is com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent.TransactionChanged -> {
                        val transactionId = event.transactionId
                        Timber.d("HOME: Получено событие изменения транзакции: $transactionId")

                        // Плавное обновление без полной перезагрузки
                        updateDataSmoothly()
                    }
                    else -> {
                        // Для других типов изменений - полная перезагрузка
                        Timber.d("HOME: Получено событие изменения данных, полная перезагрузка")
                        Timber.d("HOME: Устанавливаем isLoading = true для полной перезагрузки")
                        _state.update { it.copy(isLoading = true) }
                        loadTransactions()
                    }
                }
            }
        }
    }

    /**
     * Плавное обновление данных без полной перезагрузки
     */
    private fun updateDataSmoothly() {
        viewModelScope.launch {
            try {
                Timber.d("HOME: Начинаем плавное обновление данных")
                val currentState = _state.value
                val (startDate, endDate) = getPeriodDates(currentState.currentFilter)
                // ОЧИЩАЕМ КЭШ ПЕРЕД ЗАГРУЗКОЙ
                val transactions = getTransactionsForPeriodFlowUseCase(startDate, endDate).first()
                Timber.d("HOME: Плавно загружено транзакций: %d", transactions.size)
                updateFilteredTransactionsSmoothly(currentState.currentFilter, transactions)
            } catch (e: Exception) {
                Timber.e(e, "HOME: Ошибка при плавном обновлении: %s", e.message)
                Timber.d("HOME: Ошибка при плавном обновлении, переключаемся на полную перезагрузку")
                loadTransactions()
            }
        }
    }

    /**
     * Плавное обновление отфильтрованных транзакций без показа индикатора загрузки
     */
    private fun updateFilteredTransactionsSmoothly(filter: TransactionFilter, transactions: List<Transaction>) {
        viewModelScope.launch {
            Timber.d("HOME: updateFilteredTransactionsSmoothly - начинаем обновление")
            Timber.d("HOME: Текущее количество транзакций: ${_state.value.filteredTransactions.size}")
            Timber.d("HOME: Новое количество транзакций: ${transactions.size}")

            val (filteredIncome, filteredExpense, filteredBalance) = if (filter == TransactionFilter.ALL) {
                val income = financialMetrics.getTotalIncomeAsMoney()
                val expense = financialMetrics.getTotalExpenseAsMoney()
                val balance = financialMetrics.getCurrentBalance()
                Triple(income, expense, balance)
            } else {
                val stats = calculateStats(transactions)
                stats
            }

            val transactionGroups = groupTransactionsByDate(transactions)

            Timber.d("HOME: Обновляем состояние с новыми данными")
            _state.update {
                Timber.d("HOME: Внутри _state.update - старый isLoading: ${it.isLoading}")
                it.copy(
                    filteredTransactions = transactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    isLoading = false, // Не показываем индикатор загрузки
                )
            }
            Timber.d("HOME: Состояние обновлено, новый isLoading: ${_state.value.isLoading}")

            // Обновляем статистику по категориям в фоне
            updateCategoryStats(transactions)
        }
        reloadPaged()
    }

    /**
     * Инициирует фоновую загрузку данных
     * Обновляет данные в фоне, не блокируя UI
     * @param showLoading Показывать ли индикатор загрузки
     */
    private fun loadTransactions(showLoading: Boolean = true) {
        viewModelScope.launch {
            Timber.d("HOME: Начало загрузки транзакций, showLoading=$showLoading")
            Timber.d("HOME: Текущее состояние - isLoading: ${_state.value.isLoading}, транзакций: ${_state.value.filteredTransactions.size}")

            if (showLoading) {
                Timber.d("HOME: Устанавливаем isLoading = true")
                _state.update { it.copy(isLoading = true) }
            }

            try {
                val (startDate, endDate) = getPeriodDates(_state.value.currentFilter)
                val transactions = getTransactionsForPeriodFlowUseCase(startDate, endDate).first()
                Timber.d("HOME: Загружено транзакций: %d", transactions.size)
                updateFilteredTransactions(_state.value.currentFilter)
            } catch (e: Exception) {
                Timber.e(e, "HOME: Ошибка при загрузке транзакций: %s", e.message)
                Timber.d("HOME: Устанавливаем isLoading = false из-за ошибки")
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Подсчитывает и обновляет данные по категориям
     */
    private fun updateCategoryStats(transactions: List<Transaction>) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (transactions.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _state.update { it.copy(topCategories = emptyMap()) }
                    }
                    return@launch
                }

                // Группируем транзакции по категориям и суммируем значения
                val categoryTotals = transactions
                    .filter { it.isExpense } // Считаем только расходы
                    .groupBy { it.category }
                    .mapValues { (_, txs) -> // Суммируем расходы по категории с использованием Money
                        txs.fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
                    }
                    .toList()
                    .sortedByDescending { (_, amount) -> amount.amount.abs() } // Сортируем по убыванию сумм
                    .take(3) // Берем только топ-3 категории
                    .toMap()

                // Обновляем состояние в основном потоке
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(topCategories = categoryTotals) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении статистики по категориям: ${e.message}")
            }
        }
    }

    /**
     * Генерирует и сохраняет тестовые данные (только в Debug-сборке)
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            try {
                Timber.d("Generating test data")
                _state.update { it.copy(isLoading = true) }
                val testTransactions = TestDataGenerator.generateTransactions(10_000)
                var hasError = false
                testTransactions.forEach { transaction ->
                    Timber.d("Saving test transaction: ${transaction.category}")
                    addTransactionUseCase(transaction).fold(
                        onSuccess = { /* Transaction saved successfully */ },
                        onFailure = { exception: Throwable ->
                            hasError = true
                            Timber.e(
                                exception,
                                "Failed to save test transaction: ${transaction.category}",
                            )
                        },
                    )
                }
                if (!hasError) {
                    Timber.d("Test data generation completed successfully")
                } else {
                    _state.update {
                        it.copy(
                            error = "Ошибка при сохранении некоторых тестовых транзакций",
                        )
                    }
                }
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error generating test data")
                _state.update { it.copy(error = e.message ?: "Ошибка при генерации тестовых данных") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Обновляет список отфильтрованных транзакций на основе выбранного фильтра
     * @param filter Выбранный фильтр транзакций
     */
    private fun updateFilteredTransactions(filter: TransactionFilter) {
        viewModelScope.launch {
            Timber.d("HOME: updateFilteredTransactions - начинаем обновление с фильтром: $filter")
            Timber.d("HOME: Текущее состояние - isLoading: ${_state.value.isLoading}, транзакций: ${_state.value.filteredTransactions.size}")

            val (startDate, endDate) = getPeriodDates(filter)
            val filteredTransactions = getTransactionsForPeriodFlowUseCase(startDate, endDate).first()
            Timber.d("HOME: Получено отфильтрованных транзакций: ${filteredTransactions.size}")

            val (filteredIncome, filteredExpense, filteredBalance) = if (filter == TransactionFilter.ALL) {
                val income = financialMetrics.getTotalIncomeAsMoney()
                val expense = financialMetrics.getTotalExpenseAsMoney()
                val balance = financialMetrics.getCurrentBalance()
                Triple(income, expense, balance)
            } else {
                val stats = calculateStats(filteredTransactions)
                stats
            }
            val transactionGroups = groupTransactionsByDate(filteredTransactions)

            Timber.d("HOME: Обновляем состояние в updateFilteredTransactions")
            _state.update {
                Timber.d("HOME: Внутри updateFilteredTransactions _state.update - старый isLoading: ${it.isLoading}")
                it.copy(
                    filteredTransactions = filteredTransactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    isLoading = false,
                )
            }
            Timber.d("HOME: updateFilteredTransactions завершен, новый isLoading: ${_state.value.isLoading}")
            reloadPaged()
        }
    }

    /**
     * Вычисляет основные финансовые показатели для списка транзакций:
     * доход, расход и баланс
     *
     * @param transactions Список транзакций для анализа
     * @return Triple из (доход, расход, баланс)
     */
    private fun calculateStats(transactions: List<Transaction>): Triple<Money, Money, Money> {
        if (transactions.isEmpty()) {
            return Triple(Money.zero(), Money.zero(), Money.zero())
        }

        // Находим минимальную и максимальную даты в транзакциях
        val startDate = transactions.minByOrNull { it.date }?.date ?: java.util.Date()
        val endDate = transactions.maxByOrNull { it.date }?.date ?: java.util.Date()
        val metrics = calculateBalanceMetricsUseCase(transactions, startDate, endDate)
        val income = metrics.income
        val expense = metrics.expense
        val balance = income - expense

        val result = Triple(income, expense, balance)
        return result
    }

    /**
     * Группирует транзакции по дате
     * @param transactions Список транзакций
     * @return Список групп транзакций, сгруппированных по дате
     */
    private fun groupTransactionsByDate(
        transactions: List<Transaction>,
    ): List<com.davidbugayov.financeanalyzer.domain.model.TransactionGroup> {
        if (transactions.isEmpty()) {
            return emptyList()
        }

        // Группируем транзакции по дате (без времени)
        val groupedTransactions = transactions.groupBy { transaction ->
            // Получаем только дату без времени
            val calendar = Calendar.getInstance()
            calendar.time = transaction.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }

        // Сортируем даты по убыванию (сначала новые)
        val sortedDates = groupedTransactions.keys.sortedByDescending { it }

        // Создаем группы транзакций
        return sortedDates.map { date ->
            val transactionsForDate = groupedTransactions[date] ?: emptyList()

            // Вычисляем сумму доходов и расходов для группы
            val income = transactionsForDate
                .filter { !it.isExpense }
                .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }

            val expense = transactionsForDate
                .filter { it.isExpense }
                .fold(Money.zero()) { acc, transaction -> acc + transaction.amount.abs() }

            // Сортируем транзакции внутри группы по времени (сначала новые)
            val sortedTransactions = transactionsForDate.sortedByDescending { it.date }

            // Форматируем дату для отображения в UI
            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            dateFormat.format(date)

            // Вычисляем общий баланс (доходы - расходы)
            val balance = income - expense
            val balanceDouble = balance.amount.toDouble()

            com.davidbugayov.financeanalyzer.domain.model.TransactionGroup(
                date = date,
                transactions = sortedTransactions,
                balance = balanceDouble,
            )
        }
    }

    private fun getPeriodDates(filter: TransactionFilter): Pair<java.util.Date, java.util.Date> {
        val calendar = Calendar.getInstance()
        // Устанавливаем endDate на конец дня
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        val startCalendar = Calendar.getInstance()
        when (filter) {
            TransactionFilter.TODAY -> {
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TransactionFilter.WEEK -> {
                startCalendar.add(Calendar.DAY_OF_MONTH, -6)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TransactionFilter.MONTH -> {
                startCalendar.add(Calendar.DAY_OF_MONTH, -29)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
            TransactionFilter.ALL -> {
                startCalendar.set(2000, 0, 1, 0, 0, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)
            }
        }
        val startDate = startCalendar.time
        return Pair(startDate, endDate)
    }
}
