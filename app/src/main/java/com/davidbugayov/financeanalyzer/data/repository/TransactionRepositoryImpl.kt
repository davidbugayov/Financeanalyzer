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
import timber.log.Timber
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует Room DAO для доступа к данным.
 * Реализует оба интерфейса репозитория для обеспечения совместимости.
 * @param dao DAO для работы с транзакциями.
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository, ITransactionRepository {
    
    // Область корутин для репозитория
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Кэш для транзакций с временем последнего обновления
    private val transactionsCache = Collections.synchronizedList<Transaction>(mutableListOf())
    private var cacheLastUpdated = 0L
    private val CACHE_TTL = 5 * 60 * 1000L // 5 минут
    private val cacheLock = ReentrantReadWriteLock()
    
    // Кэши для различных типов группировки
    private val monthlyTransactionsCache = ConcurrentHashMap<String, List<Transaction>>()
    private val weeklyTransactionsCache = ConcurrentHashMap<String, List<Transaction>>()
    
    // SharedFlow для уведомления об изменениях данных
    private val _dataChangeEvents = MutableSharedFlow<DataChangeEvent>(replay = 0, extraBufferCapacity = 1)
    override val dataChangeEvents: SharedFlow<DataChangeEvent> = _dataChangeEvents.asSharedFlow()

    /**
     * Очищает все кэши.
     */
    private fun clearCaches() {
        cacheLock.writeLock().lock()
        try {
            transactionsCache.clear()
            monthlyTransactionsCache.clear()
            weeklyTransactionsCache.clear()
            cacheLastUpdated = 0L
            Timber.d("Все кэши репозитория очищены")
        } finally {
            cacheLock.writeLock().unlock()
        }
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
            var cachedData: List<Transaction>? = null
            cacheLock.readLock().lock()
            try {
                if (transactionsCache.isNotEmpty() && System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
                    cachedData = ArrayList(transactionsCache)
                }
            } finally {
                cacheLock.readLock().unlock()
            }
            if (cachedData != null) {
                Timber.d("Возвращаем кэшированные транзакции (${cachedData.size} шт.)")
                return@withContext cachedData
            }
            Timber.d("Загружаем все транзакции из базы данных")
            val transactionEntities = dao.getAllTransactions()
            val transactions = transactionEntities.map { mapEntityToDomain(it) }
            cacheLock.writeLock().lock()
            try {
                updateCache(transactions)
            } finally {
                cacheLock.writeLock().unlock()
            }
            Timber.d("Загружено ${transactions.size} транзакций из базы данных и обновлен кэш")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении всех транзакций: ${e.message}")
            emptyList()
        }
    }

    private fun updateCache(transactions: List<Transaction>) {
        transactionsCache.clear()
        transactionsCache.addAll(transactions)
        cacheLastUpdated = System.currentTimeMillis()
    }

    /**
     * Обновляет кэши транзакций по месяцам и неделям
     */
    private fun updateMonthlyAndWeeklyCache(transactions: List<Transaction>) {
        cacheLock.writeLock().lock()
        try {
            monthlyTransactionsCache.clear()
            weeklyTransactionsCache.clear()
            val groupedByMonth = transactions.groupBy { transaction ->
                val calendar = Calendar.getInstance()
                calendar.time = transaction.date
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                "$year-${month.toString().padStart(2, '0')}"
            }
            monthlyTransactionsCache.putAll(groupedByMonth)
            val groupedByWeek = transactions.groupBy { transaction ->
                val calendar = Calendar.getInstance()
                calendar.time = transaction.date
                val year = calendar.get(Calendar.YEAR)
                val week = calendar.get(Calendar.WEEK_OF_YEAR)
                "$year-W${week.toString().padStart(2, '0')}"
            }
            weeklyTransactionsCache.putAll(groupedByWeek)
            Timber.d("Кэши обновлены: ${monthlyTransactionsCache.size} месяцев, ${weeklyTransactionsCache.size} недель")
        } finally {
            cacheLock.writeLock().unlock()
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
            
            // Проверяем кэш (ConcurrentHashMap безопасен для чтения)
            monthlyTransactionsCache[monthKey]?.let {
                Timber.d("Используем кэшированные транзакции за месяц $monthKey (размер=${it.size})")
                return@withContext it // ConcurrentHashMap возвращает актуальные данные
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
            
            // Обновляем кэш (ConcurrentHashMap безопасен для записи)
            monthlyTransactionsCache[monthKey] = transactions
            
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
        
        // Проверяем кэш по неделям (ConcurrentHashMap безопасен для чтения)
        weeklyTransactionsCache[weekKey]?.let {
            // Проверяем актуальность основного кэша, чтобы решить, можно ли доверять недельному
            if (System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
                Timber.d("Используем кэшированные транзакции за неделю $weekKey (размер=${it.size})")
                return@withContext it
            }
        }
        
        // Если нет в кэше или основной кэш устарел, загружаем из базы данных
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
            
            // Обновляем кэш недели (ConcurrentHashMap безопасен для записи)
            weeklyTransactionsCache[weekKey] = transactions
            
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
        if (transactionsCache.isNotEmpty() && System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
            // Фильтруем кэшированные транзакции по диапазону дат
            val filteredTransactions = transactionsCache.filter {
                it.date >= startDate && it.date <= endDate
            }
            
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
        if (transactionsCache.isNotEmpty() && System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
            val count = transactionsCache.count { it.date >= startDate && it.date <= endDate }
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
            val cachedTransaction = cacheLock.readLock().let { lock ->
                lock.lock()
                try {
                    transactionsCache.find { it.id == id }
                } finally {
                    lock.unlock()
                }
            }
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
            val entity = mapDomainToEntity(transaction)
            val id = dao.insertTransaction(entity)
            invalidateMainCache() // Инвалидируем основной кэш вместо полной очистки
            FinancialMetrics.getInstance().invalidateMetrics()
            internalNotifyDataChanged(transaction.id) // Уведомляем об изменении
            Timber.d("Транзакция добавлена: ID=$id")
            return@withContext id.toString()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при добавлении транзакции: ${e.message}")
            throw e // Пробрасываем исключение для обработки в UseCase
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

            // Проверяем, существует ли транзакция с таким ID
            val existingTransaction = transaction.id.let { id ->
                if (id.isNotEmpty()) {
                    dao.getTransactionByIdString(id)
                } else {
                    null
                }
            }

            // Создаем сущность для обновления
            val entity = mapDomainToEntity(transaction)
            Timber.d("Сущность создана: id=${entity.id}, idString=${entity.idString}")

            if (existingTransaction != null) {
                // Если транзакция существует, обновляем ее
                Timber.d("Обновляем существующую транзакцию с ID=${entity.idString}")
                dao.updateTransaction(entity)
            } else {
                // Если транзакции не существует, вставляем как новую
                Timber.d("Транзакция с ID=${entity.idString} не найдена, вставляем новую")
                dao.insertTransaction(entity)
            }

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
                FinancialMetrics.getInstance().invalidateMetrics()
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
            val entity = mapDomainToEntity(transaction)
            dao.deleteTransaction(entity)
            invalidateMainCache() // Инвалидируем основной кэш
            FinancialMetrics.getInstance().invalidateMetrics()
            internalNotifyDataChanged(transaction.id) // Уведомляем об изменении
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
            FinancialMetrics.getInstance().invalidateMetrics()
            internalNotifyDataChanged(id) // Уведомляем об изменении
            Timber.d("Транзакция удалена по ID: $id")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при удалении транзакции по ID: $id - ${e.message}")
            throw e // Пробрасываем исключение для обработки в UseCase
        }
    }

    /**
     * Получает транзакции за указанный период.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Flow со списком транзакций.
     */
    override suspend fun getTransactionsByDateRange(
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>> = flow {
        // Получаем все транзакции и фильтруем по дате
        val transactions = getAllTransactions()
            .filter { it.date >= startDate && it.date <= endDate }
        emit(transactions)
    }

    /**
     * Получает транзакции за указанный период (метод из ITransactionRepository).
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Flow со списком транзакций.
     */
    override suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        // Делегируем вызов методу getTransactionsByDateRange
        return getTransactionsByDateRange(startDate, endDate)
    }
    
    /**
     * Создает доменную модель Transaction из сущности базы данных.
     * @param entity Сущность базы данных.
     * @return Доменная модель транзакции.
     */
    private fun mapEntityToDomain(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.idString,
            amount = entity.amount.toDouble(),
            category = entity.category,
            date = entity.date,
            isExpense = entity.isExpense,
            note = entity.note,
            source = entity.source,
            sourceColor = entity.sourceColor
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

        var longId = 0L
        val domainId = domain.id // Use a local val for clarity

        if (domainId.isNotEmpty() && domainId != "0") {
            try {
                longId = domainId.toLong() // Try direct conversion
                Timber.d("ID '$domainId' успешно конвертирован в Long: $longId")
            } catch (e: NumberFormatException) {
                // If ID is not empty, not "0", but not a number - this is a data error.
                // Logging the error. Returning 0L might lead to creating a new record instead of updating.
                // Consider throwing an exception or using a specific value.
                // Keeping 0L for now, but with a warning.
                Timber.e(e, "Ошибка: ID транзакции '$domainId' не является валидным числом! Используется ID=0.")
                // longId remains 0L by default
            }
        } else {
            Timber.d("ID пустой или '0', используется ID=0 для новой транзакции.")
            // longId remains 0L
        }

        val entity = TransactionEntity(
            id = longId,
            idString = domainId, // Always save the original ID
            amount = domain.amount.toString(),
            category = domain.category,
            date = domain.date,
            isExpense = domain.isExpense,
            note = domain.note,
            source = domain.source,
            sourceColor = domain.sourceColor
        )

        Timber.d("РЕЗУЛЬТАТ МАППИНГА: id=${entity.id}, idString=${entity.idString}, сумма=${entity.amount}")
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
        if (transactionsCache.isNotEmpty() && System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
            val endIndex = (offset + limit).coerceAtMost(transactionsCache.size)
            if (offset < transactionsCache.size) {
                val result = transactionsCache.subList(offset, endIndex)
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
     * Инвалидирует основной кэш транзакций.
     */
    private fun invalidateMainCache() {
        cacheLock.writeLock().lock()
        try {
            transactionsCache.clear()
            cacheLastUpdated = 0L
            Timber.d("Основной кэш транзакций инвалидирован")
        } finally {
            cacheLock.writeLock().unlock()
        }
    }
} 