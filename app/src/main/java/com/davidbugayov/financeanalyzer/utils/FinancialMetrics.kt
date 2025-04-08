package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionType
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Collections
import java.math.BigDecimal
import kotlinx.coroutines.withContext

/**
 * Класс для централизованного управления финансовыми метриками.
 * Реализует шаблон Singleton для обеспечения единой точки доступа к данным.
 * Кэширует результаты вычислений для уменьшения нагрузки на базу данных.
 */
class FinancialMetrics private constructor() : KoinComponent {
    
    private val preferencesManager: PreferencesManager by inject()
    private val repository: ITransactionRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // StateFlow для основных финансовых метрик
    private val _totalIncome = MutableStateFlow(Money.zero())
    val totalIncome: StateFlow<Money> = _totalIncome.asStateFlow()
    
    private val _totalExpense = MutableStateFlow(Money.zero())
    val totalExpense: StateFlow<Money> = _totalExpense.asStateFlow()
    
    private val _balance = MutableStateFlow(Money.zero())
    val balance: StateFlow<Money> = _balance.asStateFlow()
    
    // Состояние загрузки данных
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Последнее время инициализации
    private var lastInitTime = 0L

    // Кэш для недавних транзакций, чтобы не запрашивать их повторно
    private val transactionsCache = Collections.synchronizedList(mutableListOf<Transaction>())
    private var transactionsCacheTimestamp = 0L
    
    // Добавляем трекеры для предотвращения лишних вызовов
    private var lastLazyInitTime = 0L
    private var isLazyInitInProgress = false
    private var secondaryInitPending = false
    
    // Константы для кэширования
    companion object {
        @Volatile
        private var instance: FinancialMetrics? = null
        
        // Время жизни кэша транзакций - 5 минут
        private const val TRANSACTIONS_CACHE_TTL = 5 * 60 * 1000L
        
        // Минимальный интервал между принудительными пересчетами - 10 секунд
        private const val MIN_RECALCULATION_INTERVAL = 10 * 1000L
        
        /**
         * Возвращает экземпляр класса FinancialMetrics (Singleton)
         * @return экземпляр FinancialMetrics
         */
        fun getInstance(): FinancialMetrics {
            return instance ?: synchronized(this) {
                instance ?: FinancialMetrics().also { instance = it }
            }
        }
    }
    
    /**
     * Инициализирует кэш метрик.
     * Загружает данные из SharedPreferences, если они актуальны,
     * или пересчитывает их на основе данных из репозитория.
     */
    init {
        Timber.d("Инициализация FinancialMetrics")
        // Загружаем данные из кэша без перерасчета
        initializeFromCache()
    }
    
    /**
     * Инициализирует метрики только из кэша, без обращения к базе данных
     */
    private fun initializeFromCache() {
        scope.launch {
            try {
                // Загружаем только из SharedPreferences, без обращения к базе данных
                val (income, expense, balance) = preferencesManager.getFinancialStats()
                _totalIncome.value = income
                _totalExpense.value = expense
                _balance.value = balance
                
                Timber.d("Загружены быстрые метрики из кэша: доход=$income, расход=$expense, баланс=$balance")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке метрик из кэша")
            }
        }
    }
    
    /**
     * Загружает начальные статистические данные с отложенной инициализацией
     */
    private fun loadInitialStats() {
        scope.launch {
            // Предотвращение множественных одновременных вызовов
            if (_isLoading.value) {
                Timber.d("Загрузка метрик уже выполняется, пропускаем")
                return@launch
            }
            
            // Проверяем, не слишком ли часто вызывается пересчет
            val now = System.currentTimeMillis()
            if (now - lastInitTime < MIN_RECALCULATION_INTERVAL) {
                Timber.d("Слишком частый вызов инициализации метрик, используем кэшированные данные")
                return@launch
            }
            
            lastInitTime = now
            _isLoading.value = true
            
            try {
                // Проверяем наличие актуальных данных в SharedPreferences
                if (preferencesManager.isStatsUpToDate()) {
                    Timber.d("Загрузка данных из кэша")
                    val (income, expense, bal) = preferencesManager.getFinancialStats()
                    _totalIncome.value = income
                    _totalExpense.value = expense
                    _balance.value = bal
                    
                    Timber.d("Загружено из кэша: доход=$income, расход=$expense, баланс=$bal")
                } else {
                    Timber.d("Кэш устарел, пересчитываем метрики")
                    recalculateStats(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке начальных статистических данных")
                recalculateStats(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Получает транзакции, используя кэш, если это возможно
     */
    private suspend fun getTransactions(): List<Transaction> {
        // Если кэш актуален, используем его
        val now = System.currentTimeMillis()
        if (transactionsCache.isNotEmpty() && now - transactionsCacheTimestamp < TRANSACTIONS_CACHE_TTL) {
            Timber.d("Используем кэшированные транзакции (размер: ${transactionsCache.size})")
            return transactionsCache
        }
        
        // Иначе загружаем заново
        Timber.d("Загружаем транзакции из базы данных")
        return try {
            val transactions = repository.loadTransactions()
            
            // Обновляем кэш
            transactionsCache.clear()
            transactionsCache.addAll(transactions)
            transactionsCacheTimestamp = now
            
            Timber.d("Обновлен кэш транзакций (размер: ${transactions.size})")
            transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке транзакций")
            emptyList()
        }
    }

    /**
     * Обеспечивает вычисление финансовых метрик.
     * В отличие от ensureMetricsCalculated, всегда выполняется асинхронно и
     * не вызывает ANR при запуске приложения.
     * 
     * @param priority если true, увеличивает приоритет корутины
     */
    fun lazyInitialize(priority: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        
        // Если прошло менее 2 секунд с последнего запроса на инициализацию, и не высокий приоритет, просто запланируем вторичную проверку
        if (!priority && currentTime - lastLazyInitTime < 2000) {
            Timber.d("Слишком частый вызов инициализации метрик, используем кэшированные данные")
            
            // Планируем вторичную проверку, если ещё не запланирована и нет высокоприоритетной загрузки в процессе
            if (!secondaryInitPending && !isLazyInitInProgress) {
                secondaryInitPending = true
                Timber.d("Запланирована вторичная проверка метрик с высоким приоритетом")
                
                // Запускаем отложенный высокоприоритетный вызов
                CoroutineScope(Dispatchers.Default).launch {
                    delay(3000) // Значительная задержка перед повторной проверкой
                    secondaryInitPending = false
                    lazyInitialize(true) // Вызов повторной проверки с высоким приоритетом
                }
            }
            return
        }
        
        // Если уже идёт процесс инициализации, избегаем дублирования
        if (isLazyInitInProgress) {
            return
        }
        
        lastLazyInitTime = currentTime
        val dispatcher = if (priority) Dispatchers.IO else Dispatchers.Default
        
        CoroutineScope(dispatcher).launch {
            try {
                isLazyInitInProgress = true
                Timber.d("Запуск отложенной инициализации метрик с приоритетом=${priority}")
                
                // Сначала загружаем быстро из кэша для быстрого отображения UI
                initializeFromCache()
                
                // Если это не высокоприоритетный вызов, добавляем задержку
                if (!priority) {
                    delay(500)
                }
                
                // Проверяем необходимость пересчета на основе времени
                val now = System.currentTimeMillis()
                if (now - lastInitTime < MIN_RECALCULATION_INTERVAL && !priority) {
                    Timber.d("Пропускаем пересчет метрик - слишком частый вызов")
                    return@launch
                }
                
                // Затем проверяем необходимость пересчета
                loadInitialStats()
            } finally {
                isLazyInitInProgress = false
            }
        }
    }
    
    /**
     * Пересчитывает все статистические данные на основе данных из репозитория.
     * 
     * @param updatePreferences если true, то обновляет кэш в SharedPreferences
     */
    fun recalculateStats(updatePreferences: Boolean = true) {
        scope.launch {
            _isLoading.value = true
            
            try {
                val transactions = getTransactions()
                
                // Рассчитываем метрики на основе всех транзакций
                calculateMetrics(transactions)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при пересчете статистических данных")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun calculateMetrics(transactions: List<Transaction>) {
        val income = transactions
            .filter { !it.isExpense }
            .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
            
        val expense = transactions
            .filter { it.isExpense }
            .fold(Money.zero()) { acc, transaction -> acc + transaction.amount }
            
        val balance = income - expense
        
        _totalIncome.value = income
        _totalExpense.value = expense
        _balance.value = balance
        
        // Сохраняем в SharedPreferences
        preferencesManager.saveFinancialStats(
            _totalIncome.value,
            _totalExpense.value,
            _balance.value
        )
    }
    
    /**
     * Обновляет метрики при добавлении новой транзакции.
     * Также обновляет кэш транзакций.
     * 
     * @param transaction добавленная транзакция
     */
    fun updateAfterAdd(transaction: Transaction) {
        scope.launch {
            // Обновляем метрики
            if (transaction.isExpense) {
                _totalExpense.value = _totalExpense.value + transaction.amount
            } else {
                _totalIncome.value = _totalIncome.value + transaction.amount
            }
            
            // Пересчитываем баланс
            _balance.value = _totalIncome.value - _totalExpense.value
            
            // Добавляем в кэш транзакций
            if (transactionsCache.isNotEmpty()) {
                transactionsCache.add(transaction)
            }
            
            // Сохраняем обновленные данные в SharedPreferences
            preferencesManager.saveFinancialStats(
                _totalIncome.value,
                _totalExpense.value,
                _balance.value
            )
            
            Timber.d("Метрики обновлены после добавления: доход=${_totalIncome.value}, расход=${_totalExpense.value}, баланс=${_balance.value}")
        }
    }
    
    /**
     * Обновляет метрики при удалении транзакции.
     * Также обновляет кэш транзакций.
     * 
     * @param transaction удаленная транзакция
     */
    fun updateAfterDelete(transaction: Transaction) {
        scope.launch {
            // Обновляем метрики
            if (transaction.isExpense) {
                _totalExpense.value = _totalExpense.value - transaction.amount
            } else {
                _totalIncome.value = _totalIncome.value - transaction.amount
            }
            
            // Пересчитываем баланс
            _balance.value = _totalIncome.value - _totalExpense.value
            
            // Удаляем из кэша транзакций
            if (transactionsCache.isNotEmpty()) {
                transactionsCache.remove(transaction)
            }
            
            // Сохраняем обновленные данные в SharedPreferences
            preferencesManager.saveFinancialStats(
                _totalIncome.value,
                _totalExpense.value,
                _balance.value
            )
            
            Timber.d("Метрики обновлены после удаления: доход=${_totalIncome.value}, расход=${_totalExpense.value}, баланс=${_balance.value}")
        }
    }
    
    /**
     * Возвращает текущий баланс в формате Money
     * @return текущий баланс
     */
    fun getCurrentBalance(): Money {
        return _balance.value
    }
    
    /**
     * Возвращает общий доход в формате Money
     * @return общий доход
     */
    fun getTotalIncomeAsMoney(): Money {
        return _totalIncome.value
    }
    
    /**
     * Возвращает общий расход в формате Money
     * @return общий расход
     */
    fun getTotalExpenseAsMoney(): Money {
        return _totalExpense.value
    }
    
    /**
     * Обеспечивает вычисление финансовых метрик, если они еще не вычислены.
     * Этот метод может быть вызван в начале работы приложения для гарантии
     * актуальности данных.
     * 
     * @param forceRecalculation если true, то пересчитывает метрики вне зависимости от их актуальности
     */
    fun ensureMetricsCalculated(forceRecalculation: Boolean = false) {
        scope.launch {
            Timber.d("Проверка расчета финансовых метрик (принудительный пересчет: $forceRecalculation)")
            
            if (forceRecalculation || !preferencesManager.isStatsUpToDate()) {
                Timber.d("Метрики не актуальны или запрошен принудительный пересчет, выполняем вычисление")
                _isLoading.value = true
                
                try {
                    val transactions = getTransactions()
                    
                    // Вычисляем основные метрики
                    calculateMetrics(transactions)
                    
                    Timber.d("Финансовые метрики успешно вычислены и сохранены: доход=$totalIncome, расход=$totalExpense, баланс=$balance")
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при вычислении финансовых метрик")
                } finally {
                    _isLoading.value = false
                }
            } else {
                Timber.d("Метрики уже актуальны, пересчет не требуется")
            }
        }
    }
    
    /**
     * Возвращает текущий баланс в формате BigDecimal
     * @return текущий баланс
     */
    fun getBalance(): BigDecimal {
        return _balance.value.amount
    }
    
    /**
     * Возвращает общий доход в формате BigDecimal
     * @return общий доход
     */
    fun getTotalIncome(): BigDecimal {
        return _totalIncome.value.amount
    }
    
    /**
     * Возвращает общий расход в формате BigDecimal
     * @return общий расход
     */
    fun getTotalExpense(): BigDecimal {
        return _totalExpense.value.amount
    }
    
    /**
     * Инициализирует метрики из кэша (публичный метод для внешнего использования)
     */
    fun initializeMetricsFromCache() {
        initializeFromCache()
    }

    /**
     * Принудительно инвалидирует кэш метрик и запускает их пересчет.
     * Используется после изменения данных (добавление, удаление, обновление транзакции).
     */
    fun invalidateMetrics() {
        Timber.d("Инвалидация кэша метрик и запуск пересчета")
        // Помечаем кэш как неактуальный - УДАЛЕНО, т.к. нет такого метода
        // preferencesManager.invalidateStatsCache()
        // Запускаем пересчет напрямую
        recalculateStats()
    }
} 