package com.davidbugayov.financeanalyzer.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.GetTransactionsForPeriodWithCacheUseCase
import com.davidbugayov.financeanalyzer.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.navigation.Screen
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.Calendar

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property deleteTransactionUseCase UseCase для удаления транзакций
 * @property getTransactionsForPeriodWithCacheUseCase UseCase для получения транзакций за период с кэшированием
 * @property calculateBalanceMetricsUseCase UseCase для расчета финансовых метрик
 * @property repository Репозиторий для прямого доступа к транзакциям с поддержкой пагинации и подпиской на изменения
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
class HomeViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionsForPeriodWithCacheUseCase: GetTransactionsForPeriodWithCacheUseCase,
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase,
    private val repository: TransactionRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val navigationManager: NavigationManager,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // --- Кэширование ---
    // Кэши для хранения результатов вычислений, чтобы избежать повторных затратных операций
    /**
     * Кэш для отфильтрованных транзакций.
     * Ключ: пара (фильтр, список всех транзакций).
     * Значение: тройка (отфильтрованные транзакции, статистика, группы транзакций).
     * * Позволяет избежать повторной фильтрации и группировки, если фильтр не изменился.
     */
    private val filteredTransactionsCache =
        mutableMapOf<FilterCacheKey, Triple<List<Transaction>, Triple<Money, Money, Money>, List<com.davidbugayov.financeanalyzer.domain.model.TransactionGroup>>>()

    /**
     * Кэш для базовой статистики (доход, расход, баланс) по списку транзакций.
     * Ключ: список транзакций.
     * Значение: статистика (доход, расход, баланс).
     * * Используется для быстрого доступа к статистике, если она уже была вычислена.
     */
    private val statsCache = mutableMapOf<List<Transaction>, Triple<Money, Money, Money>>()

    /**
     * Кэш для исходных транзакций, загруженных из репозитория для определенного периода.
     * Ключ: строка, представляющая период (например, "transactions_start_end").
     * Значение: список транзакций за этот период.
     * * Позволяет избежать повторных запросов к репозиторию для одного и того же периода.
     */
    private val transactionCache = mutableMapOf<String, List<Transaction>>()
    // --- Конец Кэширования ---

    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    init {
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
                _state.update {
                    it.copy(
                        currentFilter = event.filter,
                        isLoading = true, // Показываем индикатор загрузки
                        // Очищаем текущие отфильтрованные данные для UI
                        filteredTransactions = emptyList(),
                        transactionGroups = emptyList(), // Очищаем группы тоже
                        // Сбрасываем статистику для фильтра
                        filteredIncome = Money.zero(),
                        filteredExpense = Money.zero(),
                        filteredBalance = Money.zero(),
                    )
                }
                // Запускаем обновление отфильтрованных транзакций в фоне
                // Эта функция позже установит isLoading = false
                updateFilteredTransactions(event.filter)
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
                navigationManager.navigate(NavigationManager.Command.Navigate(Screen.AddTransaction.route))
            }
            is HomeEvent.EditTransaction -> {
                navigationManager.navigate(
                    NavigationManager.Command.Navigate(Screen.EditTransaction.createRoute(event.transaction.id)),
                )
            }
            // Commenting out this block as NotificationScheduler.updateTransactionReminder is not static
            // and HomeViewModel should likely not be managing this directly.
            // is HomeEvent.ChangeNotifications -> {
            //     if (event.enabled && context != null) {
            //         NotificationScheduler.updateTransactionReminder(context, true)
            //     }
            // }
            else -> {
                Timber.w("Unhandled HomeEvent: $event")
            }
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
                deleteTransactionUseCase(transaction).fold(
                    onSuccess = {
                        clearCaches()
                        getTransactionsForPeriodWithCacheUseCase.clearCache() // Очищаем in-memory кэш
                        _state.update { it.copy(transactionToDelete = null) }

                        // Логируем событие в аналитику
                        com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logTransactionDeleted(
                            amount = transaction.amount.abs(),
                            category = transaction.category,
                            isExpense = transaction.isExpense,
                        )

                        context?.let { ctx ->
                            updateWidgetsUseCase(ctx)
                            Timber.d(
                                "Виджеты обновлены после удаления транзакции из HomeViewModel.",
                            )
                        } ?: Timber.w(
                            "Context не предоставлен в HomeViewModel, виджеты не обновлены после удаления.",
                        )
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to delete transaction")
                        _state.update {
                            it.copy(
                                error = exception.message ?: "Failed to delete transaction",
                                transactionToDelete = null,
                            )
                        }
                    },
                )
            } catch (e: Exception) {
                Timber.e(e, "Error deleting transaction")
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
            Timber.d("Subscribing to repository data changes")
            repository.dataChangeEvents.collect { event ->
                Timber.d("Получено событие изменения данных из репозитория")
                // Очищаем кэши синхронно
                filteredTransactionsCache.clear()
                statsCache.clear()
                transactionCache.clear()
                getTransactionsForPeriodWithCacheUseCase.clearCache() // <--- ВАЖНО! Очищаем кэш use case
                Timber.d("Все кэши очищены (sync)")
                // Показываем индикатор загрузки
                _state.update { it.copy(isLoading = true) }
                // Перезагружаем данные
                loadTransactions()
            }
        }
    }

    /**
     * Инициирует фоновую загрузку данных
     * Обновляет данные в фоне, не блокируя UI
     */
    private var lastBackgroundRefreshTime = 0L
    private var isBackgroundRefreshInProgress = false

    fun initiateBackgroundDataRefresh() {
        // Защита от слишком частых вызовов
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackgroundRefreshTime < 1000 || isBackgroundRefreshInProgress) {
            Timber.d("Пропускаем фоновое обновление - слишком частый вызов или обновление уже идет")
            return
        }

        viewModelScope.launch {
            try {
                isBackgroundRefreshInProgress = true
                lastBackgroundRefreshTime = currentTime

                Timber.d("Инициирована фоновая загрузка метрик (без перезагрузки транзакций)")

                financialMetrics.recalculateStats()
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при фоновом обновлении метрик")
            } finally {
                isBackgroundRefreshInProgress = false
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val (startDate, endDate) = getPeriodDates(_state.value.currentFilter)
                val transactions = getTransactionsForPeriodWithCacheUseCase(startDate, endDate)
                val metrics = calculateBalanceMetricsUseCase(transactions, startDate, endDate)
                _state.update {
                    it.copy(
                        transactions = transactions,
                        income = metrics.income,
                        expense = metrics.expense,
                        filteredTransactions = transactions,
                        filteredIncome = metrics.income,
                        filteredExpense = metrics.expense,
                        filteredBalance = metrics.income - metrics.expense,
                        isLoading = false,
                        error = null,
                    )
                }
                viewModelScope.launch(Dispatchers.Default) {
                    updateCategoryStats(transactions)
                }
                viewModelScope.launch(Dispatchers.Default) {
                    updateFilteredTransactions(_state.value.currentFilter)
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке транзакций: ${e.message}")
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
                val testTransactions = TestDataGenerator.generateTransactions(10)
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
                    clearCaches()
                    getTransactionsForPeriodWithCacheUseCase.clearCache() // Очищаем in-memory кэш
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
            val (startDate, endDate) = getPeriodDates(filter)
            val filteredTransactions = getTransactionsForPeriodWithCacheUseCase(startDate, endDate)
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
            _state.update {
                it.copy(
                    filteredTransactions = filteredTransactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Очищает все кэши
     */
    private fun clearCaches() {
        viewModelScope.launch(Dispatchers.Default) {
            filteredTransactionsCache.clear()
            statsCache.clear()
            transactionCache.clear()
            Timber.d("Все кэши очищены")
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

        // Проверяем, есть ли эти транзакции в кэше
        val cachedStats = statsCache[transactions]
        if (cachedStats != null) {
            return cachedStats
        }

        // Используем CalculateBalanceMetricsUseCase для расчёта
        // Находим минимальную и максимальную даты в транзакциях
        val startDate = transactions.minByOrNull { it.date }?.date ?: java.util.Date()
        val endDate = transactions.maxByOrNull { it.date }?.date ?: java.util.Date()
        val metrics = calculateBalanceMetricsUseCase(transactions, startDate, endDate)
        val income = metrics.income
        val expense = metrics.expense
        val balance = income - expense

        val result = Triple(income, expense, balance)
        statsCache[transactions] = result
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
            val dateString = dateFormat.format(date)

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

    /**
     * Ключ для кэша фильтрованных транзакций (`filteredTransactionsCache`).
     * Включает сам фильтр и хэш-код списка транзакций, к которому он применялся.
     *
     * Использование хэш-кода вместо размера списка (`size`) гарантирует более точное
     * отслеживание изменений в данных. Размер может не измениться при изменении содержимого
     * (например, при замене одной транзакции другой), а хэш-код отразит это изменение.
     */
    private data class FilterCacheKey(
        val filter: TransactionFilter,
        val transactionListHashCode: Int, // Используем хэш-код списка для точности кэширования
    )

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

    fun onAddTransactionClicked() {
        navigationManager.navigate(NavigationManager.Command.Navigate(Screen.AddTransaction.route))
    }

    fun onTransactionClicked(transactionId: String) {
        navigationManager.navigate(
            NavigationManager.Command.Navigate(Screen.EditTransaction.createRoute(transactionId)),
        )
    }

    fun onNavigateToProfile() {
        navigationManager.navigate(NavigationManager.Command.Navigate(Screen.Profile.route))
    }

    fun onNavigateToHistory() {
        navigationManager.navigate(NavigationManager.Command.Navigate(Screen.History.route))
    }
}
