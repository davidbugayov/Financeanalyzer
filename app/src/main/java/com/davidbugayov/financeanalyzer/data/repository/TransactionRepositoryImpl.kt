package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDate
import timber.log.Timber
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Абстрактная политика кэширования для различных типов данных.
 * Реализует шаблон проектирования Стратегия (Strategy) из GoF.
 *
 * @param T Тип данных для кэширования
 * @param K Тип ключа для доступа к кэшу
 */
abstract class CachePolicy<K, T> {
    /**
     * Проверяет, действителен ли кэш для указанного ключа.
     * @param key Ключ кэша для проверки
     * @return true, если кэш действителен и может быть использован
     */
    abstract fun isValid(key: K): Boolean
    
    /**
     * Получает данные из кэша
     * @param key Ключ для доступа к данным
     * @return Данные из кэша или null, если кэш не содержит данных или недействителен
     */
    abstract fun get(key: K): T?
    
    /**
     * Сохраняет данные в кэш
     * @param key Ключ для доступа к данным
     * @param data Данные для сохранения
     */
    abstract fun put(key: K, data: T)
    
    /**
     * Инвалидирует (очищает) кэш
     */
    abstract fun invalidate()
}

/**
 * Реализация политики кэширования с истечением времени жизни (TTL).
 * Используется для основного кэша транзакций.
 *
 * @param ttlMillis Время жизни кэша в миллисекундах
 */
class TTLCachePolicy<K, T>(private val ttlMillis: Long) : CachePolicy<K, T>() {
    private val cache = mutableMapOf<K, Pair<T, Long>>() // Данные и время последнего обновления
    private val lock = ReentrantReadWriteLock()
    
    override fun isValid(key: K): Boolean {
        lock.readLock().lock()
        try {
            val entry = cache[key]
            return entry != null && (System.currentTimeMillis() - entry.second < ttlMillis)
        } finally {
            lock.readLock().unlock()
        }
    }
    
    override fun get(key: K): T? {
        lock.readLock().lock()
        try {
            val entry = cache[key]
            return if (entry != null && (System.currentTimeMillis() - entry.second < ttlMillis)) {
                entry.first
            } else {
                null
            }
        } finally {
            lock.readLock().unlock()
        }
    }
    
    override fun put(key: K, data: T) {
        lock.writeLock().lock()
        try {
            cache[key] = Pair(data, System.currentTimeMillis())
        } finally {
            lock.writeLock().unlock()
        }
    }
    
    override fun invalidate() {
        lock.writeLock().lock()
        try {
            cache.clear()
        } finally {
            lock.writeLock().unlock()
        }
    }
}

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует Room DAO для доступа к данным и предоставляет кэширование.
 * Отправляет уведомления об изменениях данных через SharedFlow.
 *
 * **Стратегия кэширования:**
 * - `transactionsCache`: Основной кэш, хранит список ВСЕХ загруженных транзакций.
 *   Используется для пагинации и запросов по ID. Очищается принудительно или по TTL.
 * - `monthlyTransactionsCache`, `weeklyTransactionsCache`: Кэши для быстрого доступа к транзакциям по месяцам/неделям.
 *   Используют `ConcurrentHashMap` для потокобезопасности.
 * - `CACHE_TTL`: Время жизни основного кэша (5 минут). По истечении TTL данные будут перезагружены из БД при следующем запросе.
 *
 * @param dao DAO для работы с транзакциями.
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository, ITransactionRepository {
    
    // Область корутин для репозитория
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Константы времени жизни кэша
    private val CACHE_TTL = 5 * 60 * 1000L // 5 минут
    
    // Кэши с использованием CachePolicy
    private val transactionCache = TTLCachePolicy<String, List<Transaction>>(CACHE_TTL)
    private val monthlyTransactionsCache = TTLCachePolicy<String, List<Transaction>>(CACHE_TTL)
    private val weeklyTransactionsCache = TTLCachePolicy<String, List<Transaction>>(CACHE_TTL)
    
    // Ключ для всех транзакций
    private val ALL_TRANSACTIONS_KEY = "all_transactions"
    
    // SharedFlow для уведомления об изменениях данных (например, для ViewModel)
    private val _dataChangeEvents = MutableSharedFlow<DataChangeEvent>(replay = 0, extraBufferCapacity = 1)
    override val dataChangeEvents: SharedFlow<DataChangeEvent> = _dataChangeEvents.asSharedFlow()

    /**
     * Очищает все кэши.
     */
    private fun clearCaches() {
        Timber.d("Все кэши репозитория очищаются")
        transactionCache.invalidate()
        monthlyTransactionsCache.invalidate()
        weeklyTransactionsCache.invalidate()
        Timber.d("Все кэши репозитория очищены")
    }

    /**
     * Инвалидирует основной кэш транзакций.
     * Вызывается при добавлении, обновлении или удалении транзакций.
     */
    private fun invalidateMainCache() {
        Timber.d("Инвалидация основного кэша транзакций")
        transactionCache.invalidate()
    }

    /**
     * Отправляет событие изменения данных в SharedFlow.
     * @param transactionId ID измененной транзакции или null для массовых изменений.
     */
    private fun internalNotifyDataChanged(transactionId: String? = null) {
        repositoryScope.launch {
            Timber.d("Отправка события изменения данных: transactionId=$transactionId")
            _dataChangeEvents.tryEmit(DataChangeEvent.TransactionChanged(transactionId))
        }
    }

    /**
     * Принудительно отправляет событие изменения данных.
     * Реализация интерфейсного метода для внешнего использования.
     * @param transactionId ID измененной транзакции или null для массовых изменений.
     */
    override suspend fun notifyDataChanged(transactionId: String?) {
        withContext(Dispatchers.IO) {
            Timber.d("Принудительная отправка события изменения данных из внешнего источника: transactionId=$transactionId")
            clearCaches() // Очищаем кэш перед уведомлением
            _dataChangeEvents.emit(DataChangeEvent.TransactionChanged(transactionId))
        }
    }

    /**
     * Получает все транзакции.
     * @return Список всех транзакций.
     */
    override suspend fun getAllTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Запрос ВСЕХ транзакций из базы данных")
            
            // Проверяем кэш
            transactionCache.get(ALL_TRANSACTIONS_KEY)?.let { cachedData ->
                Timber.d("Возвращаем кэшированные транзакции (${cachedData.size} шт.)")
                return@withContext cachedData
            }
            
            // Если кэша нет или он неактуален, загружаем из БД
            Timber.d("Загружаем все транзакции из базы данных")
            val transactionEntities = dao.getAllTransactions()
            val transactions = transactionEntities.map { mapEntityToDomain(it) }
            
            // Обновляем кэш
            transactionCache.put(ALL_TRANSACTIONS_KEY, transactions)
            
            Timber.d("Загружено ${transactions.size} транзакций из базы данных и обновлен кэш")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении всех транзакций: ${e.message}")
            emptyList()
        }
    }

    /**
     * Получает транзакции за указанный месяц.
     * @param year Год.
     * @param month Месяц (1-12).
     * @return Список транзакций за указанный месяц.
     */
    override suspend fun getTransactionsByMonth(year: Int, month: Int): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Получение транзакций за месяц $year-${month.toString().padStart(2, '0')}")
            val monthKey = "$year-${month.toString().padStart(2, '0')}"
            
            // Проверяем кэш
            monthlyTransactionsCache.get(monthKey)?.let { transactions ->
                Timber.d("Используем кэшированные транзакции за месяц $monthKey (размер=${transactions.size})")
                return@withContext transactions
            }
            
            // Получаем диапазон дат для месяца
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0) // -1 т.к. Calendar.MONTH начинается с 0
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val properEndDate = calendar.time
            
            // Получаем транзакции за указанный период дат
            Timber.d("Загружаем транзакции за месяц $monthKey из базы данных (диапазон: $startDate - $properEndDate)")
            val transactions = dao.getTransactionsByDateRange(startDate, properEndDate).map { mapEntityToDomain(it) }
            
            // Обновляем кэш
            monthlyTransactionsCache.put(monthKey, transactions)
            
            Timber.d("Загружено ${transactions.size} транзакций за месяц $monthKey")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении транзакций за месяц: ${e.message}")
            emptyList()
        }
    }

    /**
     * Получает транзакции за указанную неделю
     * @param year Год
     * @param week Неделя года (1-53)
     * @return Список транзакций за указанную неделю
     */
    override suspend fun getTransactionsByWeek(year: Int, week: Int): List<Transaction> = withContext(Dispatchers.IO) {
        val weekKey = "$year-W${week.toString().padStart(2, '0')}"
        
        // Проверяем кэш по неделям
        weeklyTransactionsCache.get(weekKey)?.let { transactions ->
            Timber.d("Используем кэшированные транзакции за неделю $weekKey (размер=${transactions.size})")
            return@withContext transactions
        }
        
        // Если нет в кэше, загружаем из базы данных
        try {
            // Создаем граничные даты для запроса
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.WEEK_OF_YEAR, week)
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startDate = calendar.time
            
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val properEndDate = calendar.time
            
            // Запрашиваем транзакции за неделю
            val transactions = dao.getTransactionsByDateRangePaginated(startDate, properEndDate, 1000, 0)
                .map { mapEntityToDomain(it) }
            
            // Обновляем кэш недели
            weeklyTransactionsCache.put(weekKey, transactions)
            
            Timber.d("Загружено ${transactions.size} транзакций за неделю $weekKey")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке транзакций за неделю $weekKey: ${e.message}")
            throw e
        }
    }

    /**
     * Получает транзакции за указанный период с пагинацией (метод из ITransactionRepository)
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации и диапазона дат
     */
    override suspend fun getTransactionsPaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int
    ): List<Transaction> = withContext(Dispatchers.IO) {
        // Делегируем вызов методу getTransactionsByDateRangePaginated
        return@withContext getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
    }
    
    /**
     * Получает транзакции за указанный период с поддержкой пагинации.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации и диапазона дат.
     */
    override suspend fun getTransactionsByDateRangePaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int
    ): List<Transaction> = withContext(Dispatchers.IO) {
        // Проверяем, актуален ли наш кэш
        if (transactionCache.isValid(ALL_TRANSACTIONS_KEY)) {
            // Фильтруем кэшированные транзакции по диапазону дат
            val filteredTransactions = transactionCache.get(ALL_TRANSACTIONS_KEY)?.filter {
                it.date >= startDate && it.date <= endDate
            } ?: emptyList()
            
            // Применяем пагинацию к отфильтрованным транзакциям
            val endIndex = (offset + limit).coerceAtMost(filteredTransactions.size)
            if (offset < filteredTransactions.size) {
                val result = filteredTransactions.subList(offset, endIndex)
                Timber.d("Используем кэшированные транзакции для пагинации по диапазону дат (размер=${result.size})")
                return@withContext result
            }
        }
        
        // Если кэш не актуален или нужен доступ за его пределами, запрашиваем из БД
        try {
            val transactions = dao.getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
                .map { mapEntityToDomain(it) }
            
            Timber.d("Загружено ${transactions.size} транзакций из БД с диапазоном дат и пагинацией")
            
            // Если это первая страница (offset = 0) и результаты меньше лимита, 
            // вероятно это все транзакции за указанный диапазон, сохраняем их в кэш
            if (offset == 0 && transactions.size < limit) {
                val dateRangeKey = "${formatDate(startDate)}_${formatDate(endDate)}"
                Timber.d("Кэшируем весь результат для диапазона дат $dateRangeKey")
            }
            
            // Обновляем кэш
            transactionCache.put(ALL_TRANSACTIONS_KEY, transactions)
            
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке транзакций с пагинацией и диапазоном дат: ${e.message}")
            throw e
        }
    }

    /**
     * Форматирует дату для использования в ключах кэша
     */
    private fun formatDate(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    /**
     * Получает общее количество транзакций
     * @return Общее количество транзакций в базе данных
     */
    override suspend fun getTransactionsCount(): Int = withContext(Dispatchers.IO) {
        dao.getTransactionsCount()
    }

    /**
     * Получает общее количество транзакций в указанном диапазоне дат.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Количество транзакций в указанном диапазоне дат.
     */
    override suspend fun getTransactionsCountByDateRange(
        startDate: Date,
        endDate: Date
    ): Int = withContext(Dispatchers.IO) {
        // Пытаемся использовать кэш, если он актуален
        if (transactionCache.isValid(ALL_TRANSACTIONS_KEY)) {
            val count = transactionCache.get(ALL_TRANSACTIONS_KEY)?.count { it.date >= startDate && it.date <= endDate } ?: 0
            Timber.d("Используем кэшированные данные для подсчета: $count")
            return@withContext count
        }
        
        // Если кэш не актуален, запрашиваем из БД
        try {
            val count = dao.getTransactionsCountByDateRange(startDate, endDate)
            Timber.d("Получено количество транзакций в диапазоне дат из БД: $count")
            return@withContext count
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении количества транзакций в диапазоне дат: ${e.message}")
            throw e
        }
    }

    /**
     * Загружает все транзакции (метод из ITransactionRepository)
     * @return Список транзакций
     */
    override suspend fun loadTransactions(): List<Transaction> {
        // Делегируем вызов методу getAllTransactions
        return getAllTransactions()
    }
    
    /**
     * Получает транзакцию по ID
     *
     * @param id ID транзакции
     * @return Transaction или null если не найдена
     */
    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        try {
            // Try to find the transaction directly in the cache first
            val cachedTransaction = transactionCache.get(ALL_TRANSACTIONS_KEY)?.find { it.id == id }
            if (cachedTransaction != null) {
                Timber.d("Transaction found in cache: ID=$id")
                return@withContext cachedTransaction
            }

            // If not found in cache, query the database directly by ID
            Timber.d("Searching for transaction with ID=$id in the database")
            val entity = dao.getTransactionByIdString(id) // Use direct DAO method

            if (entity != null) {
                Timber.d("Transaction found in database: ID=$id")
                mapEntityToDomain(entity)
            } else {
                Timber.d("Transaction not found: ID=$id")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting transaction by ID '$id': ${e.message}")
            null // Return null on error
        }
    }
    
    /**
     * Добавляет новую транзакцию.
     * Реализация для интерфейсов TransactionRepository и ITransactionRepository.
     * @param transaction Транзакция для добавления.
     * @return ID добавленной транзакции.
     */
    override suspend fun addTransaction(transaction: Transaction): String = withContext(Dispatchers.IO) {
        try {
            Timber.i("[РЕПОЗИТОРИЙ] Начало добавления транзакции: ID=${transaction.id}, Дата=${transaction.date}, Сумма=${transaction.amount}, Категория='${transaction.category}', Заголовок='${transaction.title}'")

            Timber.d(
                "[РЕПОЗИТОРИЙ-ОТЛАДКА] 🔍 Полные данные транзакции: ID=${transaction.id}, Дата=${transaction.date}, Сумма=${transaction.amount}, " +
                        "Категория='${transaction.category}', Заголовок='${transaction.title}', Источник='${transaction.source}', " +
                        "isExpense=${transaction.isExpense}, isTransfer=${transaction.isTransfer}"
            )
            
            val entity = mapDomainToEntity(transaction)
            Timber.d("[РЕПОЗИТОРИЙ] Сконвертирована в сущность: ID=${entity.id}, idString=${entity.idString}, Дата=${entity.date}, Сумма=${entity.amount}")

            Timber.i("[РЕПОЗИТОРИЙ-ОТЛАДКА] ⚠️ ПЕРЕД вызовом dao.insertTransaction...")
            val id = dao.insertTransaction(entity)
            Timber.i("[РЕПОЗИТОРИЙ-ОТЛАДКА] ✅ ПОСЛЕ вызова dao.insertTransaction, ID результата=$id")
            
            invalidateMainCache() // Инвалидируем основной кэш вместо полной очистки
            Timber.d("[РЕПОЗИТОРИЙ] Кэш инвалидирован после добавления транзакции")
            
            FinancialMetrics.getInstance().recalculateStats()
            Timber.d("[РЕПОЗИТОРИЙ] Финансовые метрики пересчитаны")
            
            internalNotifyDataChanged(transaction.id) // Уведомляем об изменении
            Timber.i("[РЕПОЗИТОРИЙ] Отправлено уведомление об изменении данных для транзакции: ID=${transaction.id}")

            Timber.i("[РЕПОЗИТОРИЙ-ОТЛАДКА] 🧪 Проверка наличия сохраненной транзакции в БД...")
            val savedTransaction = dao.getTransactionByIdString(transaction.id)
            if (savedTransaction != null) {
                Timber.i("[РЕПОЗИТОРИЙ] Транзакция сохранена в базу данных: ID=${transaction.id}")
            } else {
                Timber.e("[РЕПОЗИТОРИЙ] ❌ ОШИБКА: Транзакция НЕ найдена в базе после сохранения: ID=${transaction.id}")
            }

            return@withContext transaction.id
        } catch (e: Exception) {
            Timber.e(e, "[РЕПОЗИТОРИЙ] ❌ Ошибка при добавлении транзакции: ${e.message}")
            Timber.e("[РЕПОЗИТОРИЙ-ОТЛАДКА] 🔍 Детали транзакции с ошибкой: ID=${transaction.id}, amount=${transaction.amount}, date=${transaction.date}, category=${transaction.category}, title=${transaction.title}")
            Timber.e("[РЕПОЗИТОРИЙ-ОТЛАДКА] 🔍 Стек вызовов: ${e.stackTraceToString()}")
            throw e // Пробрасываем исключение для обработки выше
        }
    }
    
    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    override suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        try {
            // Максимально подробное логирование
            Timber.d("===== НАЧАЛО ОБНОВЛЕНИЯ ТРАНЗАКЦИИ =====")
            Timber.d("ID: ${transaction.id}")
            Timber.d("Сумма: ${transaction.amount}")
            Timber.d("Категория: ${transaction.category}")
            Timber.d("Источник: ${transaction.source}")
            Timber.d("Дата: ${transaction.date}")

            // Проверяем, является ли ID числовым
            try {
                if (transaction.id.all { it.isDigit() }) {
                    Timber.d("Числовой ID: ${transaction.id}")
                } else {
                    Timber.d("Нечисловой ID: ${transaction.id}")
                }
            } catch (e: NumberFormatException) {
                Timber.e(e, "Ошибка при проверке ID: ${transaction.id}")
            }

            // Ищем существующую транзакцию
            val existingTransaction = dao.getTransactionByIdString(transaction.id)
            
            if (existingTransaction == null) {
                Timber.e("Не найдена транзакция с ID=${transaction.id} для обновления")
                throw Exception("Транзакция с ID=${transaction.id} не найдена для обновления")
            }

            // Используем существующий ID из базы данных, но обновляем все остальные поля
            val entity = TransactionEntity(
                id = existingTransaction.id, // Используем ID из существующей записи
                idString = transaction.id,
                amount = transaction.amount,
                category = transaction.category,
                date = transaction.date,
                isExpense = transaction.isExpense,
                note = transaction.note,
                source = transaction.source,
                sourceColor = transaction.sourceColor,
                isTransfer = transaction.isTransfer,
                categoryId = transaction.categoryId,
                title = transaction.title,
                walletIds = transaction.walletIds
            )

            Timber.d("Сущность создана: id=${entity.id}, idString=${entity.idString}")

            // Выполняем обновление транзакции
            Timber.d("Обновляем существующую транзакцию с ID=${entity.idString}")
            dao.updateTransaction(entity)

            // Инвалидируем основной кэш
            try {
                invalidateMainCache()
                Timber.d("Основной кэш инвалидирован")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при инвалидации основного кэша: ${e.message}")
                // Продолжаем выполнение
            }

            // Инвалидируем метрики
            try {
                FinancialMetrics.getInstance().recalculateStats()
                Timber.d("Метрики сброшены")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при сбросе метрик: ${e.message}")
                // Продолжаем выполнение даже при ошибке метрик
            }

            // Отправляем уведомление об изменении
            try {
                internalNotifyDataChanged(transaction.id)
                Timber.d("Отправлено уведомление об изменении данных")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при отправке уведомления: ${e.message}")
                // Продолжаем выполнение даже при ошибке уведомления
            }

            Timber.d("===== ЗАВЕРШЕНО ОБНОВЛЕНИЕ ТРАНЗАКЦИИ ID=${transaction.id} =====")
        } catch (e: Exception) {
            Timber.e(e, "===== ОШИБКА ОБНОВЛЕНИЯ ТРАНЗАКЦИИ ID=${transaction.id} =====")
            Timber.e(e, "Сообщение: ${e.message}")
            throw e // Пробрасываем исключение для обработки в UseCase
        }
    }
    
    /**
     * Удаляет транзакцию.
     * @param transaction Транзакция для удаления.
     */
    override suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        try {
            Timber.d("Удаление транзакции с ID=${transaction.id}")
            // Вместо создания сущности, просто используем метод удаления по ID
            deleteTransaction(transaction.id)
            Timber.d("Транзакция удалена: ID=${transaction.id}")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при удалении транзакции: ${e.message}")
            throw e // Пробрасываем исключение для обработки в UseCase
        }
    }

    /**
     * Удаляет транзакцию по идентификатору.
     * @param id Идентификатор транзакции для удаления.
     */
    override suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
        try {
            dao.deleteTransactionById(id)
            invalidateMainCache() // Инвалидируем основной кэш
            FinancialMetrics.getInstance().recalculateStats()
            internalNotifyDataChanged(id) // Уведомляем об изменении
            Timber.d("Транзакция удалена по ID: $id")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при удалении транзакции по ID: $id - ${e.message}")
            throw e // Пробрасываем исключение для обработки в UseCase
        }
    }

    /**
     * Реализация метода getTransactionsByDateRange для java.util.Date
     */
    override suspend fun getTransactionsByDateRange(
        startDate: Date,
        endDate: Date
    ): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Получение транзакций по диапазону дат java.util.Date")
            val transactions = dao.getTransactionsByDateRange(startDate, endDate)
                .map { mapEntityToDomain(it) }
            
            Timber.d("Загружено ${transactions.size} транзакций за период")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении транзакций по диапазону дат: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Получает транзакции за указанный период (метод из ITransactionRepository).
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Flow со списком транзакций.
     */
    override suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>> = flow {
        // Используем реализованный метод getTransactionsByDateRange
        val transactions = getTransactionsByDateRange(startDate, endDate)
        emit(transactions)
    }
    
    /**
     * Преобразует сущность в доменную модель.
     * @param entity Сущность транзакции.
     * @return Доменная модель транзакции.
     */
    private fun mapEntityToDomain(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.idString,
            amount = entity.amount,
            category = entity.category,
            date = entity.date,
            isExpense = entity.isExpense,
            note = entity.note,
            source = entity.source,
            sourceColor = entity.sourceColor,
            isTransfer = entity.isTransfer,
            categoryId = entity.categoryId,
            title = entity.title,
            walletIds = entity.walletIds
        )
    }
    
    /**
     * Преобразует доменную модель в сущность.
     * @param domain Доменная модель транзакции.
     * @return Сущность транзакции.
     */
    private fun mapDomainToEntity(domain: Transaction): TransactionEntity {
        Timber.d("МАППИНГ В СУЩНОСТЬ: Начало преобразования Transaction -> TransactionEntity")
        Timber.d("Исходная транзакция: id=${domain.id}, сумма=${domain.amount}, категория=${domain.category}")

        // Обработка ID: Transaction.id (String) -> TransactionEntity.id (Long) и TransactionEntity.idString (String)
        var longId = 0L
        val domainId = domain.id

        if (domainId.isNotEmpty() && domainId != "0") {
            // Пытаемся преобразовать строковый ID в Long, только если он полностью состоит из цифр
            try {
                if (domainId.all { it.isDigit() }) {
                    longId = domainId.toLong()
                    Timber.d("ID '$domainId' успешно конвертирован в Long: $longId")
                } else {
                    // Это не числовой ID (вероятно UUID), оставляем longId = 0L
                    Timber.d("ID '$domainId' не является числовым, используется entity.id=0L")
                }
            } catch (e: NumberFormatException) {
                Timber.e(e, "Ошибка: ID транзакции '$domainId' не является валидным числом! Используется entity.id=0L.")
            }
        }

        val entity = TransactionEntity(
            id = longId, // Используем вычисленный Long ID только если он полностью числовой
            idString = domainId, // Всегда сохраняем оригинальный строковый ID
            amount = domain.amount,
            category = domain.category,
            date = domain.date,
            isExpense = domain.isExpense,
            note = domain.note,
            source = domain.source,
            sourceColor = domain.sourceColor,
            isTransfer = domain.isTransfer,
            categoryId = domain.categoryId,
            title = domain.title,
            walletIds = domain.walletIds
        )

        Timber.d("Создана сущность: id=${entity.id}, idString=${entity.idString}")
        return entity
    }

    /**
     * Получает транзакции с поддержкой пагинации.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации.
     */
    override suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<Transaction> = withContext(Dispatchers.IO) {
        // Если кэш полностью загружен, используем его для пагинации
        if (transactionCache.isValid(ALL_TRANSACTIONS_KEY)) {
            val endIndex = (offset + limit).coerceAtMost(transactionCache.get(ALL_TRANSACTIONS_KEY)?.size ?: 0)
            if (offset < endIndex) {
                val result = transactionCache.get(ALL_TRANSACTIONS_KEY)?.subList(offset, endIndex) ?: emptyList()
                Timber.d("Используем кэшированные транзакции для пагинации (размер=${result.size})")
                return@withContext result
            }
        }
        
        // Если кэш не актуален или нужны данные за его пределами, запрашиваем из БД
        val result = dao.getTransactionsPaginated(limit, offset).map { mapEntityToDomain(it) }
        Timber.d("Загружено ${result.size} транзакций из БД с пагинацией (лимит=$limit, смещение=$offset)")
        return@withContext result
    }
    
    /**
     * Загружает транзакции с пагинацией (метод из ITransactionRepository)
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации
     */
    override suspend fun loadTransactionsPaginated(limit: Int, offset: Int): List<Transaction> {
        // Делегируем вызов методу getTransactionsPaginated
        return getTransactionsPaginated(limit, offset)
    }

    /**
     * Получает список транзакций за указанный период (не Flow).
     * Реализует метод из TransactionRepository.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций.
     */
    override suspend fun getTransactionsByDateRangeList(
        startDate: Date,
        endDate: Date
    ): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Timber.d("РЕПОЗИТОРИЙ: Загрузка списка транзакций по диапазону дат из DAO")
            val entities = dao.getTransactionsByDateRange(startDate, endDate)
            val transactions = entities.map { mapEntityToDomain(it) }
            Timber.d("РЕПОЗИТОРИЙ: Загружено ${transactions.size} транзакций из DAO по диапазону дат")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при загрузке списка транзакций по диапазону дат: ${e.message}")
            throw e // Перебрасываем исключение для обработки выше
        }
    }

    /**
     * Реализация метода getTransactionsByDateRange для kotlinx.datetime.LocalDate
     */
    override suspend fun getTransactionsByDateRange(
        startDate: kotlinx.datetime.LocalDate,
        endDate: kotlinx.datetime.LocalDate
    ): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Получение транзакций по диапазону дат с использованием kotlinx.datetime.LocalDate")
            
            // Преобразуем kotlinx.datetime.LocalDate в java.util.Date для работы с DAO
            val startJavaLocalDate = startDate.toJavaLocalDate()
            val endJavaLocalDate = endDate.toJavaLocalDate().plusDays(1)
            
            val startDateUtilDate = Date.from(startJavaLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val endDateUtilDate = Date.from(endJavaLocalDate.atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant())
            
            // Используем существующий метод с java.util.Date
            val transactions = dao.getTransactionsByDateRange(startDateUtilDate, endDateUtilDate)
                .map { mapEntityToDomain(it) }
            
            Timber.d("Загружено ${transactions.size} транзакций за период с $startDate по $endDate")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении транзакций по диапазону дат с kotlinx.datetime.LocalDate: ${e.message}")
            emptyList()
        }
    }
} 