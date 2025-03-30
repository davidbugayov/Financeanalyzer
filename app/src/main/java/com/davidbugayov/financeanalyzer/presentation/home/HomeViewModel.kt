package com.davidbugayov.financeanalyzer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.DeleteTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.GetTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.utils.Event
import com.davidbugayov.financeanalyzer.utils.EventBus
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import com.davidbugayov.financeanalyzer.utils.TestDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.Calendar
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

/**
 * ViewModel для главного экрана.
 * Следует принципам MVI и Clean Architecture.
 *
 * @property getTransactionsUseCase UseCase для загрузки транзакций
 * @property addTransactionUseCase UseCase для добавления новых транзакций
 * @property deleteTransactionUseCase UseCase для удаления транзакций
 * @property repository Репозиторий для прямого доступа к транзакциям с поддержкой пагинации
 * @property eventBus Шина событий для коммуникации между компонентами
 * @property _state Внутренний MutableStateFlow для хранения состояния экрана
 * @property state Публичный StateFlow для наблюдения за состоянием экрана
 */
class HomeViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val repository: TransactionRepository,
    private val eventBus: EventBus
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Кэши для хранения результатов вычислений
    private val filteredTransactionsCache = mutableMapOf<FilterCacheKey, Triple<List<Transaction>, Triple<Money, Money, Money>, List<TransactionGroup>>>()
    private val statsCache = mutableMapOf<List<Transaction>, Triple<Money, Money, Money>>()
    
    // Финансовые метрики
    private val financialMetrics = FinancialMetrics.getInstance()

    init {
        Timber.d("HomeViewModel initialized")
        loadTransactions()

        // TODO: УДАЛИТЬ ПЕРЕД РЕЛИЗОМ - Тестовый код для проверки Crashlytics
        // testCrashlytics()
        subscribeToEvents()
        
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
                _state.update { it.copy(currentFilter = event.filter) }
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
                        // Уведомляем другие компоненты об удалении транзакции
                        eventBus.emit(Event.TransactionDeleted)
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
     * Подписываемся на события изменения транзакций через EventBus
     */
    private fun subscribeToEvents() {
        viewModelScope.launch {
            Timber.d("Subscribing to transaction events")
            eventBus.events.collect { event ->
                when (event) {
                    is Event.TransactionAdded,
                    is Event.TransactionDeleted,
                    is Event.TransactionUpdated -> {
                        // Очищаем кэши при изменении данных
                        clearCaches()
                        loadTransactions()
                    }
                }
            }
        }
    }

    /**
     * Инициирует фоновую загрузку данных
     * Обновляет данные в фоне, не блокируя UI
     */
    fun initiateBackgroundDataRefresh() {
        viewModelScope.launch {
            try {
                // Проверяем, нужно ли обновлять метрики
                Timber.d("Инициирована фоновая загрузка данных")
                // Просим FinancialMetrics запланировать проверку в фоне
                financialMetrics.lazyInitialize(priority = true)
                
                // Проверяем, нужно ли загружать транзакции полностью
                val currentTransactions = _state.value.transactions
                if (currentTransactions.isEmpty() || currentTransactions.size < 20) {
                    Timber.d("Запрашиваем полную загрузку транзакций в фоне")
                    loadTransactions(forceBackground = true)
                } else {
                    Timber.d("Предварительная загрузка уже выполнена, обновляем только метрики")
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при фоновом обновлении данных")
            }
        }
    }

    /**
     * Загружает транзакции из репозитория и обновляет состояние.
     * Использует пагинацию для снижения нагрузки.
     * 
     * @param forceBackground Если true, заставляет выполнение в фоне без блокировки UI
     */
    private fun loadTransactions(forceBackground: Boolean = false) {
        // Проверяем, если загрузка уже идет, не начинаем ее снова
        if (_state.value.isLoading && !forceBackground) {
            Timber.d("Загрузка транзакций уже выполняется, пропускаем дублирующий вызов")
            return
        }
        
        // Устанавливаем индикатор загрузки только если не в фоновом режиме
        if (!forceBackground) {
            _state.update { it.copy(isLoading = true) }
        }
        
        // Запускаем загрузку в фоновом потоке
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Используем кэшированные метрики для баланса и финансовых данных
                val income = financialMetrics.getTotalIncome()
                val expense = financialMetrics.getTotalExpense()
                val balance = financialMetrics.getCurrentBalance()
                
                // Обновляем состояние с метриками, не дожидаясь загрузки транзакций
                if (!forceBackground) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                income = income,
                                expense = expense,
                                balance = balance
                            )
                        }
                    }
                }
                
                // Готовим параметры для загрузки транзакций
                val calendar = Calendar.getInstance()
                val endDateValue = calendar.time
                calendar.add(Calendar.MONTH, -1)
                val startDateValue = calendar.time
                
                // Асинхронно загружаем только последние транзакции с пагинацией
                val initialTransactions = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        // Загружаем только первые 20 транзакций для быстрого отображения
                        repository.getTransactionsByDateRangePaginated(
                            startDate = startDateValue,
                            endDate = endDateValue,
                            limit = 20,
                            offset = 0
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при загрузке первой страницы транзакций: ${e.message}")
                        emptyList()
                    }
                }
                
                Timber.d("Загружено ${initialTransactions.size} последних транзакций для быстрого отображения")
                
                // Обновляем состояние с первой порцией данных в UI-потоке
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            transactions = initialTransactions,
                            isLoading = false
                        )
                    }
                    
                    // Обновляем отфильтрованные транзакции
                    updateFilteredTransactions()
                }
                
                // Добавляем небольшую задержку, чтобы не перегружать устройство
                kotlinx.coroutines.delay(300)
                
                // Затем асинхронно загружаем остальные транзакции, если нужно
                if (initialTransactions.size >= 20) {
                    Timber.d("Загружаем оставшиеся транзакции в фоне")
                    
                    // Запускаем загрузку всех транзакций с использованием repository напрямую
                    // чтобы воспользоваться его кэшированием
                    val allTransactions = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            repository.getAllTransactions()
                        } catch (e: Exception) {
                            Timber.e(e, "Ошибка при полной загрузке транзакций: ${e.message}")
                            emptyList()
                        }
                    }
                    
                    if (allTransactions.isNotEmpty() && allTransactions.size > initialTransactions.size) {
                        Timber.d("Загружены все ${allTransactions.size} транзакции")
                        
                        // Обновляем состояние только если есть больше транзакций
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    transactions = allTransactions
                                )
                            }
                            
                            // Обновляем отфильтрованные транзакции
                            updateFilteredTransactions()
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Критическая ошибка при загрузке данных: ${e.message}")
                if (!forceBackground) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Ошибка при загрузке данных"
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Генерирует и сохраняет тестовые данные
     */
    private fun generateAndSaveTestData() {
        viewModelScope.launch {
            Timber.d("Generating test data")
            val testTransactions = TestDataGenerator.generateTransactions(500)

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
                eventBus.emit(Event.TransactionAdded)
                Timber.d("Test data generation completed successfully")
            } else {
                _state.update { it.copy(error = "Ошибка при сохранении некоторых тестовых транзакций") }
            }
        }
    }

    /**
     * Обновляет отфильтрованные транзакции на основе текущего фильтра
     * Использует оптимизированные алгоритмы для фильтрации и группировки
     */
    private fun updateFilteredTransactions() {
        // Запускаем обработку в отдельной корутине
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            Timber.d("Обновление отфильтрованных транзакций запущено")
            val currentState = _state.value
            
            if (currentState.transactions.isEmpty()) {
                Timber.d("Список транзакций пуст, нет данных для фильтрации")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            filteredTransactions = emptyList(),
                            filteredIncome = Money(0.0),
                            filteredExpense = Money(0.0),
                            filteredBalance = Money(0.0),
                            transactionGroups = emptyList()
                        )
                    }
                }
                return@launch
            }
    
            // Создаем ключ для кэша
            val cacheKey = FilterCacheKey(
                filter = currentState.currentFilter.toString()
            )
    
            // Проверяем кэш быстро без блокировки
            val cachedEntry = filteredTransactionsCache[cacheKey]
            if (cachedEntry != null && cachedEntry.first.size == currentState.transactions.size) {
                Timber.d("Используем кэшированные данные для отфильтрованных транзакций")
                val filteredData = cachedEntry
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            filteredTransactions = filteredData.first,
                            filteredIncome = filteredData.second.first,
                            filteredExpense = filteredData.second.second,
                            filteredBalance = filteredData.second.third,
                            transactionGroups = filteredData.third
                        )
                    }
                }
                return@launch
            }
    
            // Выполняем фильтрацию в фоновом потоке
            Timber.d("Кэша нет, выполняем фильтрацию транзакций")
            val filtered = when (currentState.currentFilter) {
                TransactionFilter.TODAY -> getTodayTransactions(currentState.transactions)
                TransactionFilter.WEEK -> getLastWeekTransactions(currentState.transactions)
                TransactionFilter.MONTH -> getLastMonthTransactions(currentState.transactions)
            }
            
            Timber.d("Отфильтровано ${filtered.size} транзакций")
    
            // Вычисляем статистику для отфильтрованных транзакций
            Timber.d("Вычисляем статистику для отфильтрованных транзакций")
            
            // Оптимизированный расчет статистики - за один проход по данным
            var income = 0.0
            var expense = 0.0
            
            // Оптимизация: предварительно создаём структуры для группировки
            val categoryMap = if (currentState.showGroupSummary) mutableMapOf<String, MutableList<Transaction>>() else null
            val categoryTotals = if (currentState.showGroupSummary) mutableMapOf<String, Double>() else null
            
            // За один проход вычисляем все необходимые метрики
            filtered.forEach { transaction ->
                if (transaction.isExpense) {
                    expense += transaction.amount
                } else {
                    income += transaction.amount
                }
                
                // Если нужно группировать, делаем это за тот же проход
                if (currentState.showGroupSummary) {
                    val category = transaction.category
                    // Добавляем транзакцию в соответствующую группу
                    categoryMap?.getOrPut(category) { mutableListOf() }?.add(transaction)
                    // Обновляем сумму для категории
                    categoryTotals?.put(category, (categoryTotals[category] ?: 0.0) + kotlin.math.abs(transaction.amount))
                }
            }
            
            val balance = income - expense
            val filteredIncome = Money(income)
            val filteredExpense = Money(expense)
            val filteredBalance = Money(balance)
    
            // Формируем группы транзакций по категориям только если они нужны
            val groups = if (filtered.isNotEmpty() && currentState.showGroupSummary && categoryMap != null && categoryTotals != null) {
                // Преобразуем в итоговый список групп
                categoryMap.map { (category, categoryTransactions) ->
                    val categoryTotal = categoryTotals[category] ?: 0.0
                    
                    // Создаем группу транзакций
                    TransactionGroup(
                        date = category,
                        transactions = categoryTransactions,
                        balance = Money(categoryTotal),
                        name = category,
                        total = Money(categoryTotal)
                    )
                }.sortedByDescending { it.total.amount }
            } else {
                emptyList()
            }
            
            // Сохраняем в кэш результаты вычислений
            val statsTriple = Triple(filteredIncome, filteredExpense, filteredBalance)
            filteredTransactionsCache[cacheKey] = Triple(filtered, statsTriple, groups)
    
            // Обновляем состояние в основном потоке
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _state.update {
                    it.copy(
                        filteredTransactions = filtered,
                        filteredIncome = filteredIncome,
                        filteredExpense = filteredExpense,
                        filteredBalance = filteredBalance,
                        transactionGroups = groups
                    )
                }
            }
            
            Timber.d("Обновление отфильтрованных транзакций завершено")
        }
    }

    /**
     * Возвращает транзакции за последний месяц
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за последний месяц
     */
    private fun getLastMonthTransactions(transactions: List<Transaction>): List<Transaction> {
        // Оптимизация: устанавливаем точную дату для сравнения, избегая многократных вычислений
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val monthAgo = calendar.timeInMillis

        return transactions
            .asSequence()
            .filter { it.date.time >= monthAgo }
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
     * Возвращает транзакции за последнюю неделю
     * @param transactions Список всех транзакций
     * @return Отфильтрованный список транзакций за последнюю неделю
     */
    private fun getLastWeekTransactions(transactions: List<Transaction>): List<Transaction> {
        // Оптимизация: устанавливаем точную дату для сравнения, избегая многократных вычислений
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val weekAgo = calendar.timeInMillis

        return transactions
            .asSequence()
            .filter { it.date.time >= weekAgo }
            .sortedByDescending { it.date }
            .toList()
    }

    /**
     * Очищает все кэши
     */
    private fun clearCaches() {
        filteredTransactionsCache.clear()
    }

    /**
     * Ключ для кэша фильтрованных транзакций
     */
    private data class FilterCacheKey(
        val filter: String
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
} 