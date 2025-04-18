package com.davidbugayov.financeanalyzer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.math.BigDecimal
import java.util.Calendar
import kotlin.math.abs
import java.time.LocalDateTime

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property getTransactionsUseCase UseCase для загрузки транзакций
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property deleteTransactionUseCase UseCase для удаления транзакций
 * @property repository Репозиторий для прямого доступа к транзакциям с поддержкой пагинации и подпиской на изменения
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
class HomeViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val repository: TransactionRepository
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

    // Добавляем константу TAG для логов
    companion object {
        private const val TAG = "HomeViewModel"
    }

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
                // Обновляем доход без полной перезагрузки транзакций
                _state.update { it.copy(income = income) }
            }
        }
        
        viewModelScope.launch {
            financialMetrics.totalExpense.collect { expense ->
                // Обновляем расход без полной перезагрузки транзакций
                _state.update { it.copy(expense = expense) }
            }
        }
    }

    /**
     * Обрабатывает события экрана Home
     * @param event Событие для обработки
     */
    fun onEvent(event: HomeEvent) {
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
                deleteTransaction(event.transaction)
            }
        }
    }

    /**
     * Удаляет транзакцию
     * @param transaction Транзакция для удаления
     */
    private fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transaction).fold(
                    onSuccess = {
                        // Очищаем кэши при удалении транзакции
                        clearCaches()
                        // Уведомление об удалении теперь происходит через SharedFlow репозитория
                        // Скрываем диалог подтверждения
                        _state.update { it.copy(transactionToDelete = null) }
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
                // Просим FinancialMetrics запланировать проверку в фоне
                // Это обновит глобальные метрики, которые HomeViewModel слушает через StateFlow
                financialMetrics.lazyInitialize(priority = true)
                
                // УДАЛЯЕМ ЛОГИКУ ПРОВЕРКИ И ПЕРЕЗАГРУЗКИ ТРАНЗАКЦИЙ ЗДЕСЬ
                // // Проверяем, нужно ли загружать транзакции полностью
                // val currentTransactions = _state.value.transactions
                // if (currentTransactions.isEmpty() || currentTransactions.size < 20) {
                //     Timber.d("Запрашиваем полную загрузку транзакций в фоне")
                //     
                //     // Небольшая задержка, чтобы не перегружать UI
                //     delay(100)
                //     
                //     // Используем loadTransactions, который уже имеет защиту от множественных вызовов
                //     loadTransactions()
                // } else {
                //     Timber.d("Предварительная загрузка уже выполнена, обновляем только метрики")
                //     
                //     // Только обновляем метрики, но не перезагружаем транзакции
                //     val metrics = withContext(Dispatchers.IO) {
                //         try {
                //             val fm = FinancialMetrics.getInstance()
                //             fm.initializeMetricsFromCache() 
                //             Triple(fm.getTotalIncome(), fm.getTotalExpense(), fm.getBalance())
                //         } catch (e: Exception) {
                //             Timber.e(e, "Ошибка при получении финансовых метрик: ${e.message}")
                //             Triple(0.0, 0.0, 0.0) 
                //         }
                //     }
                //     
                //     // Обновляем только метрики
                //     _state.update { 
                //         it.copy(
                //             income = Money(metrics.first),
                //             expense = Money(metrics.second),
                //             balance = Money(metrics.third)
                //         )
                //     }
                // }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при фоновом обновлении метрик")
            } finally {
                isBackgroundRefreshInProgress = false
            }
        }
    }

    /**
     * Загружает транзакции за текущий месяц
     */
    private var lastLoadTime = 0L // Время последней загрузки
    private var isLoadingInProgress = false // Флаг, указывающий на то, что загрузка уже идет
    private var loadedPeriod = 30 // По умолчанию загружаем за 30 дней вместо 60
    
    private fun loadTransactions() {
        // Проверка, чтобы избежать множественных вызовов за короткий промежуток времени (дебаунсинг)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadTime < 500 || isLoadingInProgress) { // Увеличил до 500 мс для более эффективного дебаунсинга
            Timber.d("Пропускаем загрузку данных - слишком частый вызов или загрузка уже идет")
            return
        }
        
        viewModelScope.launch {
            isLoadingInProgress = true
            lastLoadTime = currentTime
            
            Timber.d("Начинаем загрузку транзакций для домашнего экрана (последние $loadedPeriod дней)")
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Определяем диапазон дат: последние N дней
                val calendar = Calendar.getInstance()
                val endDate = calendar.time // Сегодня
                calendar.add(Calendar.DAY_OF_YEAR, -loadedPeriod)
                val startDate = calendar.time // N дней назад
                
                Timber.d("Загрузка транзакций с $startDate по $endDate")

                // 1. Используем SuspendableLoadingCache для кэширования результатов между вызовами loadTransactions
                val cacheKey = "transactions_${startDate.time}_${endDate.time}"
                val cachedTransactions = transactionCache[cacheKey]
                
                val transactions = if (cachedTransactions != null) {
                    Timber.d("Используем кэшированные транзакции")
                    cachedTransactions
                } else {
                    // Параллельно загружаем метрики и транзакции для ускорения
                    val transactionsDeferred = async(Dispatchers.IO) {
                        try {
                            Timber.d("Запрос транзакций за последние $loadedPeriod дней")
                            val result = repository.getTransactionsByDateRangeList(startDate, endDate)
                            // Кэшируем результат
                            transactionCache[cacheKey] = result
                            result
                        } catch (e: Exception) {
                            Timber.e(e, "Ошибка при загрузке транзакций за диапазон дат: ${e.message}")
                            emptyList()
                        }
                    }
                    
                    transactionsDeferred.await()
                }
                
                // 2. Используем ранее инициализированные метрики вместо их перевычисления
                val metricsResult = Triple(
                    financialMetrics.getTotalIncomeAsMoney(),
                    financialMetrics.getTotalExpenseAsMoney(),
                    financialMetrics.getCurrentBalance()
                )

                // 3. Обновляем состояние с загруженными транзакциями и метриками
                Timber.d("Загружено ${transactions.size} транзакций за последние $loadedPeriod дней")
                _state.update { 
                    it.copy(
                        transactions = transactions,
                        income = metricsResult.first,
                        expense = metricsResult.second,
                        balance = metricsResult.third,
                        isLoading = false,
                        error = null
                    ) 
                }
                
                // 4. Запускаем обновление категорий и фильтрацию параллельно
                viewModelScope.launch(Dispatchers.Default) {
                    updateCategoryStats(transactions)
                }
                
                viewModelScope.launch(Dispatchers.Default) {
                    updateFilteredTransactions(_state.value.currentFilter)
                }
                
                // Не блокируем основной поток, просто даем задачам выполниться в фоне
                
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке данных: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка" 
                    ) 
                }
            } finally {
                isLoadingInProgress = false
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
     * Генерирует и сохраняет тестовые данные
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            try {
                Timber.d("Generating test data")
                // Устанавливаем флаг загрузки
                _state.update { it.copy(isLoading = true) }
                
                // Генерируем 100 транзакций за последний месяц
                val testTransactions = TestDataGenerator.generateTransactions(100)

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
                    // Очищаем кэши при добавлении тестовых данных
                    clearCaches()
                    Timber.d("Test data generation completed successfully")
                } else {
                    _state.update { it.copy(error = "Ошибка при сохранении некоторых тестовых транзакций") }
                }
                
                // Запускаем обновление транзакций для отображения новых данных
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error generating test data")
                _state.update { it.copy(error = e.message ?: "Ошибка при генерации тестовых данных") }
            } finally {
                // В любом случае (успех или ошибка) отключаем индикатор загрузки
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Обновляет список отфильтрованных транзакций на основе выбранного фильтра
     * @param filter Выбранный фильтр транзакций
     */
    private fun updateFilteredTransactions(filter: TransactionFilter) {
        val allTransactions = _state.value.transactions
        if (allTransactions.isEmpty()) {
            _state.update { it.copy(
                filteredTransactions = emptyList(),
                filteredIncome = Money.zero(),
                filteredExpense = Money.zero(),
                filteredBalance = Money.zero(),
                isLoading = false
            ) }
            return
        }

        val calendar = Calendar.getInstance()

        val filteredTransactions = when (filter) {
            TransactionFilter.TODAY -> {
                // Получаем только транзакции за сегодня
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.time
                
                allTransactions.filter { transaction ->
                    transaction.date in startOfDay..<endOfDay
                }
            }
            TransactionFilter.WEEK -> {
                // Получаем транзакции за последние 7 дней
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_MONTH, -6) // -6 дней + сегодня = 7 дней
                val startOfWeek = calendar.time
                
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                val endOfWeek = calendar.time
                
                allTransactions.filter { transaction ->
                    transaction.date in startOfWeek..<endOfWeek
                }
            }
            TransactionFilter.MONTH -> {
                // Получаем транзакции за последние 30 дней
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_MONTH, -29) // -29 дней + сегодня = 30 дней
                val startOfMonth = calendar.time
                
                calendar.add(Calendar.DAY_OF_MONTH, 30)
                val endOfMonth = calendar.time
                
                allTransactions.filter { transaction ->
                    transaction.date in startOfMonth..<endOfMonth
                }
            }
            TransactionFilter.ALL -> {
                // Для фильтра "Все" берем все транзакции без фильтрации
                allTransactions
            }
        }

        // Рассчитываем сумму доходов, расходов и баланс для отфильтрованных транзакций
        val (filteredIncome, filteredExpense, filteredBalance) = calculateStats(filteredTransactions)

        val transactionGroups = groupTransactionsByDate(filteredTransactions)
        _state.update { it.copy(
            filteredTransactions = filteredTransactions, 
            transactionGroups = transactionGroups,
            filteredIncome = filteredIncome,
            filteredExpense = filteredExpense,
            filteredBalance = filteredBalance,
            isLoading = false  // Отключаем индикатор загрузки после обновления данных
        ) }
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

        // Используем fold для более функционального и лаконичного кода
        val income = transactions
            .filter { !it.isExpense }
            .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
            
        val expense = transactions
            .filter { it.isExpense }
            .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }

        // Вычисляем баланс как разницу между доходами и расходами
        val balance = income - expense

        // Создаем Triple с Money объектами для возврата
        val result = Triple(income, expense, balance)

        // Кэшируем результат для будущих запросов
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
} 