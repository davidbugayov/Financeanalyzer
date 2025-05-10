package com.davidbugayov.financeanalyzer.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.CalculateBalanceMetricsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsForPeriodWithCacheUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
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
    private val updateWidgetsUseCase: UpdateWidgetsUseCase
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // --- Кэширование --- 
    // Кэши для хранения результатов вычислений, чтобы избежать повторных затратных операций
    /**
     * Кэш для отфильтрованных транзакций и их статистики.
     * Ключ: комбинация фильтра и хэш списка исходных транзакций.
     * Значение: отфильтрованные транзакции, статистика (доход, расход, баланс), группы транзакций.
     * 
     * Это позволяет избежать повторных вычислений, если список транзакций и фильтр не изменились.
     */
    private val filteredTransactionsCache = mutableMapOf<FilterCacheKey, Triple<List<Transaction>, Triple<Money, Money, Money>, List<TransactionGroup>>>()
    
    /**
     * Кэш для базовой статистики (доход, расход, баланс) по списку транзакций.
     * Ключ: список транзакций.
     * Значение: статистика (доход, расход, баланс).
     * 
     * Используется для быстрого доступа к статистике, если она уже была вычислена.
     */
    private val statsCache = mutableMapOf<List<Transaction>, Triple<Money, Money, Money>>()
    
    /**
     * Кэш для исходных транзакций, загруженных из репозитория для определенного периода.
     * Ключ: строка, представляющая период (например, "transactions_start_end").
     * Значение: список транзакций за этот период.
     * 
     * Позволяет избежать повторных запросов к репозиторию для одного и того же периода.
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
                        filteredBalance = Money.zero()
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
            is HomeEvent.ChangeNotifications -> {
                if (event.enabled && context != null) {
                    NotificationScheduler.updateTransactionReminder(context, true)
                }
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
                        context?.let { ctx ->
                            updateWidgetsUseCase(ctx)
                            Timber.d("Виджеты обновлены после удаления транзакции из HomeViewModel.")
                        } ?: Timber.w("Context не предоставлен в HomeViewModel, виджеты не обновлены после удаления.")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to delete transaction")
                        _state.update {
                            it.copy(
                                error = exception.message ?: "Failed to delete transaction",
                                transactionToDelete = null
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error deleting transaction")
                _state.update {
                    it.copy(
                        error = e.message ?: "Error deleting transaction",
                        transactionToDelete = null
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

                // Показываем индикатор загрузки, чтобы пользователь видел, что происходит обновление
                _state.update { it.copy(isLoading = true) }
                
                // Очищаем кэши при изменении данных
                clearCaches()

                // Получаем ID транзакции из события, если оно есть
                val transactionId =
                    if (event is DataChangeEvent.TransactionChanged) event.transactionId else null
                Timber.d("Обновление по событию изменения данных: transactionId=$transactionId")
                
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
                Timber.d("[LOG_BALANCE] loadTransactions: startDate=$startDate, endDate=$endDate, income=${metrics.income.formatted()}, expense=${metrics.expense.formatted()}, balance=${(metrics.income - metrics.expense).formatted()}, txCount=${transactions.size}")
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
                        error = null
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
                    .mapValues { (_, txs) -> 
                        // Суммируем расходы по категории с использованием Money
                        txs.fold(Money.zero()) { acc, transaction -> 
                            acc + transaction.amount 
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
                            Timber.e(exception, "Failed to save test transaction: ${transaction.category}")
                        }
                    )
                }
                if (!hasError) {
                    clearCaches()
                    getTransactionsForPeriodWithCacheUseCase.clearCache() // Очищаем in-memory кэш
                    Timber.d("Test data generation completed successfully")
                } else {
                    _state.update { it.copy(error = "Ошибка при сохранении некоторых тестовых транзакций") }
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
                Timber.d("[LOG_BALANCE] updateFilteredTransactions(ALL): income=${income.formatted()}, expense=${expense.formatted()}, balance=${balance.formatted()}, txCount=${filteredTransactions.size}")
                Triple(income, expense, balance)
            } else {
                val stats = calculateStats(filteredTransactions)
                Timber.d("[LOG_BALANCE] updateFilteredTransactions(${filter.name}): income=${stats.first.formatted()}, expense=${stats.second.formatted()}, balance=${stats.third.formatted()}, txCount=${filteredTransactions.size}")
                stats
            }
            Timber.d("[DIAG] STATS: income=${filteredIncome.formatted()}, expense=${filteredExpense.formatted()}, balance=${filteredBalance.formatted()}")
            val transactionGroups = groupTransactionsByDate(filteredTransactions)
            _state.update {
                it.copy(
                    filteredTransactions = filteredTransactions,
                    transactionGroups = transactionGroups,
                    filteredIncome = filteredIncome,
                    filteredExpense = filteredExpense,
                    filteredBalance = filteredBalance,
                    isLoading = false
                )
            }
            if (filter == TransactionFilter.ALL) {
                Timber.d("[DIAG] HomeViewModel транзакций: ${filteredTransactions.size}")
                filteredTransactions.forEach { Timber.d("[DIAG] HVM TX: id=${it.id}, amount=${it.amount}, date=${it.date}, isExpense=${it.isExpense}") }
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
            Timber.d("[DIAG] calculateStats: пустой список транзакций")
            return Triple(Money.zero(), Money.zero(), Money.zero())
        }

        // Проверяем, есть ли эти транзакции в кэше
        val cachedStats = statsCache[transactions]
        if (cachedStats != null) {
            Timber.d("[DIAG] calculateStats: используем кэш")
            return cachedStats
        }

        // Используем CalculateBalanceMetricsUseCase для расчёта
        val metrics = calculateBalanceMetricsUseCase(transactions)
        val income = metrics.income
        val expense = metrics.expense
        val balance = income - expense

        Timber.d("[DIAG] calculateStats: income=${income.formatted()}, expense=${expense.formatted()}, balance=${balance.formatted()}")

        val result = Triple(income, expense, balance)
        statsCache[transactions] = result
        return result
    }

    /**
     * Группирует транзакции по дате
     * @param transactions Список транзакций
     * @return Список групп транзакций, сгруппированных по дате
     */
    private fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionGroup> {
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
                .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
            
            // Сортируем транзакции внутри группы по времени (сначала новые)
            val sortedTransactions = transactionsForDate.sortedByDescending { it.date }
            
            // Форматируем дату для отображения в UI
            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            val dateString = dateFormat.format(date)
            
            // Вычисляем общий баланс (доходы - расходы)
            val balance = income - expense
            
            TransactionGroup(
                date = dateString,
                transactions = sortedTransactions,
                balance = balance,
                name = dateString,
                total = balance
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
        val transactionListHashCode: Int // Используем хэш-код списка для точности кэширования
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
}