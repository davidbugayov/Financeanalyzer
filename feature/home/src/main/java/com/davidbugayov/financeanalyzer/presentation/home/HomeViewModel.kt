package com.davidbugayov.financeanalyzer.presentation.home
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.core.util.fold
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetExpenseOptimizationRecommendationsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.GetSmartExpenseTipsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodFlowUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import com.davidbugayov.financeanalyzer.utils.CurrencyProvider
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import timber.log.Timber

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
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionsForPeriodFlowUseCase: GetTransactionsForPeriodFlowUseCase,
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase,
    private val repository: TransactionRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val navigationManager: NavigationManager,
    private val getSmartExpenseTipsUseCase: GetSmartExpenseTipsUseCase, // внедряем use case
    private val getExpenseOptimizationRecommendationsUseCase: GetExpenseOptimizationRecommendationsUseCase, // внедряем use case
) : ViewModel(), KoinComponent {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    // -------- Paging ------------
    private val pagerTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    val pagedTransactions: Flow<PagingData<Transaction>> =
        pagerTrigger
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

    val pagedUiModels: Flow<PagingData<TransactionListItem>> =
        pagedTransactions
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

    private fun reloadPaged() {
        pagerTrigger.tryEmit(Unit)
    }

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ru"))

    private fun dayKey(date: java.util.Date): String = dateFormatter.format(date)

    init {
        // Первичная инициализация Paging
        pagerTrigger.tryEmit(Unit)
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

        // Подписываемся на изменения валюты
        viewModelScope.launch {
            CurrencyProvider.getCurrencyFlow().collect { newCurrency ->
                // Пересчитываем данные с новой валютой
                loadTransactions()
            }
        }
    }

    /**
     * Обрабатывает события экрана Home
     * @param event Событие для обработки
     */
    fun onEvent(
        event: HomeEvent,
        context: Context? = null,
    ) {
        when (event) {
            is HomeEvent.SetFilter -> {
                // Обновляем только фильтр, не трогаем остальные данные, чтобы избежать мерцания
                _state.update { it.copy(currentFilter = event.filter) }

                // Плавно обновляем данные без очистки UI
                updateDataSmoothly()
            }
            is HomeEvent.LoadTransactions -> {
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
                val periodType =
                    when (state.value.currentFilter) {
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
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(Screen.AddTransaction.createRoute(forceExpense = true)),
                )
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
    private fun deleteTransaction(
        transaction: Transaction,
        context: Context? = null,
    ) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transaction).fold(
                    onSuccess = {
                        _state.update { it.copy(transactionToDelete = null) }

                        // Логируем событие в аналитику
                        com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logTransactionDeleted(
                            amount = transaction.amount.abs(),
                            category = transaction.category,
                            isExpense = transaction.isExpense,
                        )

                        context?.let { ctx ->
                            updateWidgetsUseCase()
                        } ?: Timber.w(
                            "Context не предоставлен в HomeViewModel, виджеты не обновлены после удаления.",
                        )
                    },
                    onFailure = { exception ->
                        _state.update {
                            it.copy(
                                error = exception.message ?: "Failed to delete transaction",
                                transactionToDelete = null,
                            )
                        }
                    },
                )
            } catch (e: Exception) {
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
            repository.dataChangeEvents.collect { event ->
                when (event) {
                    is com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent.TransactionChanged -> {
                        event.transactionId

                        // Плавное обновление без полной перезагрузки
                        updateDataSmoothly()
                    }
                    else -> {
                        // Для других типов изменений - полная перезагрузка
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
                val currentState = _state.value
                val (startDate, endDate) = getPeriodDates(currentState.currentFilter)
                // ОЧИЩАЕМ КЭШ ПЕРЕД ЗАГРУЗКОЙ
                val transactions = getTransactionsForPeriodFlowUseCase(startDate, endDate).first()
                updateFilteredTransactionsSmoothly(currentState.currentFilter, transactions)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при плавном обновлении: %s", e.message)
                loadTransactions()
            }
        }
    }

    /**
     * Плавное обновление отфильтрованных транзакций без показа индикатора загрузки
     */
    private fun updateFilteredTransactionsSmoothly(
        filter: TransactionFilter,
        transactions: List<Transaction>,
    ) {
        viewModelScope.launch {
            val (filteredIncome, filteredExpense, filteredBalance) =
                if (filter == TransactionFilter.ALL) {
                    val income = financialMetrics.getTotalIncomeAsMoney()
                    val expense = financialMetrics.getTotalExpenseAsMoney()
                    val balance = financialMetrics.getCurrentBalance()
                    Triple(income, expense, balance)
                } else {
                    val stats = calculateStats(transactions)
                    stats
                }

            val transactionGroups = groupTransactionsByDate(transactions)

            _state.update {
                it.copy(
                    filteredTransactions = transactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    isLoading = false, // Не показываем индикатор загрузки
                )
            }

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
            if (showLoading) {
                _state.update { it.copy(isLoading = true) }
            }

            try {
                val (startDate, endDate) = getPeriodDates(_state.value.currentFilter)
                getTransactionsForPeriodFlowUseCase(startDate, endDate).first()
                updateFilteredTransactions(_state.value.currentFilter)
            } catch (e: Exception) {
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
                val currentCurrency = CurrencyProvider.getCurrency()
                val categoryTotals =
                    transactions
                        .filter { it.isExpense } // Считаем только расходы
                        .groupBy { it.category }
                        .mapValues { (_, txs) -> // Суммируем расходы по категории с использованием Money
                            txs.fold(Money.zero(currentCurrency)) { acc, transaction ->
                                val convertedAmount = Money(transaction.amount.amount, currentCurrency)
                                acc + convertedAmount
                            }
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
                _state.update { it.copy(isLoading = true) }
                val testTransactions = TestDataGenerator.generateTransactions(80)
                var hasError = false
                testTransactions.forEach { transaction ->
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
                    val rp: ResourceProvider = GlobalContext.get().get()
                    _state.update { it.copy(error = rp.getString(UiR.string.error_saving_test_transactions)) }
                }
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error generating test data")
                val rp: ResourceProvider = GlobalContext.get().get()
                _state.update { it.copy(error = e.message ?: rp.getString(UiR.string.error_generating_test_data)) }
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
            val (startDate, endDate) = getPeriodDates(filter)
            val filteredTransactions = getTransactionsForPeriodFlowUseCase(startDate, endDate).first()

            val (filteredIncome, filteredExpense, filteredBalance) =
                if (filter == TransactionFilter.ALL) {
                    val income = financialMetrics.getTotalIncomeAsMoney()
                    val expense = financialMetrics.getTotalExpenseAsMoney()
                    val balance = financialMetrics.getCurrentBalance()
                    Triple(income, expense, balance)
                } else {
                    val stats = calculateStats(filteredTransactions)
                    stats
                }
            val transactionGroups = groupTransactionsByDate(filteredTransactions)
            val tips = getSmartExpenseTipsUseCase.invoke(filteredTransactions)
            val recommendations = getExpenseOptimizationRecommendationsUseCase.invoke(filteredTransactions)
            _state.update {
                it.copy(
                    filteredTransactions = filteredTransactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    isLoading = false,
                    smartExpenseTips = tips, // обновляем советы
                    expenseOptimizationRecommendations = recommendations, // сохраняем рекомендации
                )
            }
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
            val currentCurrency = CurrencyProvider.getCurrency()
            return Triple(Money.zero(currentCurrency), Money.zero(currentCurrency), Money.zero(currentCurrency))
        }

        // Находим минимальную и максимальную даты в транзакциях
        val startDate = transactions.minByOrNull { it.date }?.date ?: java.util.Date()
        val endDate = transactions.maxByOrNull { it.date }?.date ?: java.util.Date()
        val currentCurrency = CurrencyProvider.getCurrency()
        val metrics = calculateBalanceMetricsUseCase(transactions, currentCurrency, startDate, endDate)
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
        val groupedTransactions =
            transactions.groupBy { transaction ->
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
            val income =
                transactionsForDate
                    .filter { !it.isExpense }
                    .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }

            val expense =
                transactionsForDate
                    .filter { it.isExpense }
                    .fold(Money.zero()) { acc, transaction -> acc + transaction.amount.abs() }

            // Сортируем транзакции внутри группы по времени (сначала новые)
            val sortedTransactions = transactionsForDate.sortedByDescending { it.date }

            // Форматируем дату для отображения в UI
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
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
