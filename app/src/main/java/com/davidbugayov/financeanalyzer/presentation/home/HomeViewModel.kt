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

    // Кэши для хранения результатов вычислений
    private val filteredTransactionsCache = mutableMapOf<FilterCacheKey, Triple<List<Transaction>, Triple<Money, Money, Money>, List<TransactionGroup>>>()
    private val statsCache = mutableMapOf<List<Transaction>, Triple<Money, Money, Money>>()
    // Кэш для загруженных транзакций по временным периодам
    private val transactionCache = mutableMapOf<String, List<Transaction>>()
    
    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    init {
        Timber.d("HomeViewModel initialized")
        subscribeToRepositoryChanges() // Подписываемся на изменения в репозитории
        
        // Наблюдаем за изменениями балансов
        viewModelScope.launch {
            financialMetrics.balance.collect { balance ->
                // Обновляем баланс без полной перезагрузки транзакций
                _state.update { it.copy(balance = Money(balance)) }
            }
        }
        
        viewModelScope.launch {
            financialMetrics.totalIncome.collect { income ->
                // Обновляем доход без полной перезагрузки транзакций
                _state.update { it.copy(income = Money(income)) }
            }
        }
        
        viewModelScope.launch {
            financialMetrics.totalExpense.collect { expense ->
                // Обновляем расход без полной перезагрузки транзакций
                _state.update { it.copy(expense = Money(expense)) }
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
                Timber.d("Устанавливаем новый фильтр: ${event.filter}")
                // Очищаем кэш перед сменой фильтра
                clearCaches()
                // Немедленно обновляем фильтр, ставим isLoading = true и очищаем старый список
                _state.update { 
                    it.copy(
                        currentFilter = event.filter, 
                        isLoading = true, // Показываем загрузку СРАЗУ
                        filteredTransactions = emptyList(), // Очищаем старый список СРАЗУ
                        transactionGroups = emptyList(), // Очищаем группы тоже
                        // Сбрасываем статистику для фильтра
                        filteredIncome = Money.zero(),
                        filteredExpense = Money.zero(),
                        filteredBalance = Money.zero()
                    ) 
                }
                // Запускаем обновление отфильтрованных транзакций в фоне
                // Эта функция позже установит isLoading = false
                updateFilteredTransactions()
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
                    financialMetrics.getTotalIncome(), 
                    financialMetrics.getTotalExpense(), 
                    financialMetrics.getBalance()
                )

                // 3. Обновляем состояние с загруженными транзакциями и метриками
                Timber.d("Загружено ${transactions.size} транзакций за последние $loadedPeriod дней")
                _state.update { 
                    it.copy(
                        transactions = transactions,
                        income = Money(metricsResult.first),
                        expense = Money(metricsResult.second),
                        balance = Money(metricsResult.third),
                        isLoading = false,
                        error = null
                    ) 
                }
                
                // 4. Запускаем обновление категорий и фильтрацию параллельно
                val job1 = viewModelScope.launch(Dispatchers.Default) {
                    updateCategoryStats(transactions)
                }
                
                val job2 = viewModelScope.launch(Dispatchers.Default) {
                    updateFilteredTransactions()
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
                        // Суммируем расходы по категории
                        val total = txs.sumOf { it.amount }
                        Money(total) 
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
            Timber.d("Generating test data")
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
        }
    }

    /**
     * Обновляет отфильтрованные транзакции и статистику на основе текущего фильтра
     */
    private var isFilteringInProgress = false
    private var lastFilterUpdateTime = 0L

    private fun updateFilteredTransactions() {
        // Защита от слишком частых обновлений
        val currentTime = System.currentTimeMillis()
        if (isFilteringInProgress || currentTime - lastFilterUpdateTime < 300) {
            Timber.d("Фильтрация уже выполняется или вызвана слишком часто, пропускаем")
            return
        }

        viewModelScope.launch(Dispatchers.Default) { // Сразу запускаем в фоновом потоке
            isFilteringInProgress = true
            lastFilterUpdateTime = currentTime
            
            try {
                val transactions = _state.value.transactions
                val currentFilter = _state.value.currentFilter

                // Оптимизированный ключ кэша - только фильтр и хэш-код списка транзакций
                val cacheKey = FilterCacheKey(currentFilter, transactions.hashCode())

                // Проверяем кэш перед вычислениями
                val cachedData = filteredTransactionsCache[cacheKey]
                if (cachedData != null) {
                    Timber.d("Используем кэшированные отфильтрованные данные для ${currentFilter.name}")

                    // Обновляем состояние сразу из кэша без вычислений
                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                filteredTransactions = cachedData.first,
                                filteredIncome = cachedData.second.first,
                                filteredExpense = cachedData.second.second,
                                filteredBalance = cachedData.second.third,
                                transactionGroups = cachedData.third,
                                isLoading = false
                            )
                        }
                    }
                    return@launch
                }

                // Фильтруем транзакции на основе текущего фильтра
                // Используем оптимизированные функции фильтрации
                val filteredTransactions = when (currentFilter) {
                    TransactionFilter.TODAY -> getTodayTransactions(transactions)
                    TransactionFilter.WEEK -> getLastWeekTransactions(transactions)
                    TransactionFilter.MONTH -> getLastMonthTransactions(transactions)
                }

                // Проверяем, есть ли кэшированная статистика для этих отфильтрованных транзакций
                val statsKey = filteredTransactions.hashCode()
                val stats = statsCache[filteredTransactions] ?: calculateStats(filteredTransactions).also {
                    // Кэшируем результаты вычислений
                    statsCache[filteredTransactions] = it
                }

                // Используем кэшированные данные по группам, если они доступны
                val groups = if (_state.value.showGroupSummary) 
                    groupTransactions(filteredTransactions, currentFilter) 
                else 
                    emptyList()

                // Сохраняем результаты в кэш
                filteredTransactionsCache[cacheKey] = Triple(filteredTransactions, stats, groups)

                // Обновляем состояние в главном потоке с новыми данными
                withContext(Dispatchers.Main) {
                    _state.update { state ->
                        state.copy(
                            filteredTransactions = filteredTransactions,
                            filteredIncome = stats.first,
                            filteredExpense = stats.second,
                            filteredBalance = stats.third,
                            transactionGroups = groups,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении отфильтрованных транзакций: ${e.message}")
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isLoading = false) }
                }
            } finally {
                isFilteringInProgress = false
            }
        }
    }

    /**
     * Возвращает транзакции за текущий календарный месяц
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за текущий календарный месяц
     */
    private fun getLastMonthTransactions(transactions: List<Transaction>): List<Transaction> {
        // Получаем текущую дату и устанавливаем начало текущего месяца
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Первый день месяца
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        // Конец месяца - текущий момент
        val endOfMonth = System.currentTimeMillis()

        return transactions
            .asSequence()
            .filter { 
                val time = it.date.time
                time >= startOfMonth && time <= endOfMonth 
            }
            .sortedByDescending { it.date }
            .toList()
    }
    
    /**
     * Возвращает транзакции за сегодня
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за сегодня
     */
    private fun getTodayTransactions(transactions: List<Transaction>): List<Transaction> {
        // Оптимизация: устанавливаем точную дату для сравнения, избегая многократных вычислений
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayStart = today.timeInMillis
        
        // Устанавливаем конец дня
        today.set(Calendar.HOUR_OF_DAY, 23)
        today.set(Calendar.MINUTE, 59)
        today.set(Calendar.SECOND, 59)
        today.set(Calendar.MILLISECOND, 999)
        val todayEnd = today.timeInMillis

        return transactions
            .asSequence()
            .filter { 
                val time = it.date.time
                time >= todayStart && time <= todayEnd
            }
            .sortedByDescending { it.date }
            .toList()
    }
    
    /**
     * Возвращает транзакции за текущую календарную неделю
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за текущую календарную неделю
     */
    private fun getLastWeekTransactions(transactions: List<Transaction>): List<Transaction> {
        // Получаем текущую дату и устанавливаем начало текущей недели
        val calendar = Calendar.getInstance()
        // Устанавливаем на начало недели (понедельник)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis
        
        // Конец недели - текущий момент
        val endOfWeek = System.currentTimeMillis()

        return transactions
            .asSequence()
            .filter {
                val time = it.date.time
                time >= startOfWeek && time <= endOfWeek
            }
            .sortedByDescending { it.date }
            .toList()
    }

    /**
     * Очищает все кэши
     */
    private fun clearCaches() {
        Timber.d("Очистка всех кэшей в HomeViewModel")
        filteredTransactionsCache.clear()
        statsCache.clear()
        transactionCache.clear()
        Timber.d("Кэши в HomeViewModel очищены")
    }

    /**
     * Ключ для кэша фильтрованных транзакций
     */
    private data class FilterCacheKey(
        val filter: TransactionFilter,
        val size: Int
    )

    private fun calculateTotalIncome(transactions: List<Transaction>): Money {
        val total = transactions
            .filter { !it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
        return Money(total)
    }

    private fun calculateTotalExpenses(transactions: List<Transaction>): Money {
        val total = transactions
            .filter { it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
        return Money(total)
    }

    /**
     * Получает общую сумму доходов
     */
    fun getTotalIncome(): Money {
        val total = _state.value.transactions
            .filter { !it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
        return Money(total)
    }

    /**
     * Получает общую сумму расходов
     */
    fun getTotalExpense(): Money {
        val total = _state.value.transactions
            .filter { it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, amount -> acc + amount } ?: 0.0
        return Money(total)
    }

    /**
     * Вычисляет статистику для списка транзакций
     * @return Triple(доход, расход, баланс)
     */
    private fun calculateStats(transactions: List<Transaction>): Triple<Money, Money, Money> {
        if (transactions.isEmpty()) {
            return Triple(Money.zero(), Money.zero(), Money.zero())
        }

        var income = 0.0
        var expense = 0.0

        // За один проход вычисляем все необходимые метрики
        transactions.forEach { transaction ->
            if (transaction.isExpense) {
                expense += transaction.amount
            } else {
                income += transaction.amount
            }
        }

        val balance = income - expense
        return Triple(Money(income), Money(expense), Money(balance))
    }

    /**
     * Группирует транзакции для отображения
     */
    private fun groupTransactions(
        transactions: List<Transaction>,
        filter: TransactionFilter
    ): List<TransactionGroup> {
        if (transactions.isEmpty() || !_state.value.showGroupSummary) {
            return emptyList()
        }

        // Группируем по категориям
        val categoryMap = mutableMapOf<String, MutableList<Transaction>>()
        val categoryTotals = mutableMapOf<String, Double>()

        // Группируем транзакции и вычисляем суммы по категориям
        transactions.forEach { transaction ->
            val category = transaction.category
            categoryMap.getOrPut(category) { mutableListOf() }.add(transaction)
            categoryTotals[category] =
                (categoryTotals[category] ?: 0.0) + Math.abs(transaction.amount)
        }

        // Преобразуем в итоговый список групп и отфильтруем группы с нулевым доходом
        return categoryMap.map { (category, categoryTransactions) ->
            val categoryTotal = categoryTotals[category] ?: 0.0

            // Создаем группу транзакций
            TransactionGroup(
                date = category,
                transactions = categoryTransactions,
                balance = Money(categoryTotal),
                name = category,
                total = Money(categoryTotal)
            )
        }
        .filter { it.total.amount > BigDecimal.ZERO } // Фильтруем группы с нулевым доходом
        .sortedByDescending { it.total.amount }
    }
} 