package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.entity.TransactionEntity
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import timber.log.Timber
import com.davidbugayov.financeanalyzer.utils.FinancialMetrics
import java.util.Collections
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует Room DAO для доступа к данным.
 * Реализует оба интерфейса репозитория для обеспечения совместимости.
 * @param dao DAO для работы с транзакциями.
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository, ITransactionRepository {
    
    // Кэш для транзакций с временем последнего обновления
    private val transactionsCache = Collections.synchronizedList<Transaction>(mutableListOf())
    private var cacheLastUpdated = 0L
    private val CACHE_TTL = 5 * 60 * 1000L // 5 минут
    private val cacheLock = Any() // Для синхронизации доступа к кэшу
    
    // Кэши для различных типов группировки
    private val monthlyTransactionsCache = Collections.synchronizedMap<String, List<Transaction>>(mutableMapOf())
    private val weeklyTransactionsCache = Collections.synchronizedMap<String, List<Transaction>>(mutableMapOf())
    
    /**
     * Получает все транзакции.
     * @return Список всех транзакций.
     */
    override suspend fun getAllTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Запрос ВСЕХ транзакций из базы данных")
            
            // Проверяем актуальность кэша вне критической секции
            var cachedData: List<Transaction>? = null
            synchronized(cacheLock) {
                if (transactionsCache.isNotEmpty() && System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
                    cachedData = ArrayList(transactionsCache) // Создаем копию в синхронизированном блоке
                }
            }
            
            // Если кэш актуален, возвращаем данные
            if (cachedData != null) {
                Timber.d("Возвращаем кэшированные транзакции (${cachedData!!.size} шт.)")
                return@withContext cachedData!!
            }
            
            // Кэш устарел или пуст, загружаем из базы данных
            Timber.d("Загружаем все транзакции из базы данных")
            val transactionEntities = dao.getAllTransactions()
            val transactions = transactionEntities.map { mapEntityToDomain(it) }
            
            // Обновляем кэш после загрузки
            synchronized(cacheLock) {
                transactionsCache.clear()
                transactionsCache.addAll(transactions)
                cacheLastUpdated = System.currentTimeMillis()
            }
            
            Timber.d("Загружено ${transactions.size} транзакций из базы данных и обновлен кэш")
            return@withContext transactions
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении всех транзакций: ${e.message}")
            emptyList()
        }
    }

    /**
     * Обновляет кэши транзакций по месяцам и неделям
     */
    private fun updateMonthlyAndWeeklyCache(transactions: List<Transaction>) {
        // Очищаем существующие кэши
        monthlyTransactionsCache.clear()
        weeklyTransactionsCache.clear()
        
        // Группируем транзакции по месяцам
        val groupedByMonth = transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1 // +1 т.к. месяцы в Calendar начинаются с 0
            "$year-${month.toString().padStart(2, '0')}" // Формат YYYY-MM
        }
        
        // Сохраняем группировку по месяцам в кэш
        monthlyTransactionsCache.putAll(groupedByMonth)
        
        // Группируем транзакции по неделям
        val groupedByWeek = transactions.groupBy { transaction ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = transaction.date
            val year = calendar.get(java.util.Calendar.YEAR)
            val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            "$year-W${week.toString().padStart(2, '0')}" // Формат YYYY-WXX
        }
        
        // Сохраняем группировку по неделям в кэш
        weeklyTransactionsCache.putAll(groupedByWeek)
        
        Timber.d("Кэши обновлены: ${monthlyTransactionsCache.size} месяцев, ${weeklyTransactionsCache.size} недель")
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
            
            // Проверяем кэш вне критической секции
            var cachedMonthData: List<Transaction>? = null
            synchronized(cacheLock) {
                val cachedData = monthlyTransactionsCache[monthKey]
                if (cachedData != null) {
                    cachedMonthData = ArrayList(cachedData) // Создаем копию в синхронизированном блоке
                }
            }
            
            // Используем кэшированные данные, если они есть
            if (cachedMonthData != null) {
                Timber.d("Используем кэшированные транзакции за месяц $monthKey (размер=${cachedMonthData!!.size})")
                return@withContext cachedMonthData!!
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
            val endDate = calendar.time
            
            // Получаем транзакции за указанный период дат
            Timber.d("Загружаем транзакции за месяц $monthKey из базы данных (диапазон: $startDate - $endDate)")
            val transactions = dao.getTransactionsByDateRangePaginated(startDate, endDate, 10000, 0).map { mapEntityToDomain(it) }
            
            // Обновляем кэш
            synchronized(cacheLock) {
                monthlyTransactionsCache[monthKey] = transactions
            }
            
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
        
        // Проверяем, есть ли данные в кэше
        if (transactionsCache.isNotEmpty() && System.currentTimeMillis() - cacheLastUpdated < CACHE_TTL) {
            // Если общий кэш актуален, проверяем кэш по неделям
            weeklyTransactionsCache[weekKey]?.let {
                Timber.d("Используем кэшированные транзакции за неделю $weekKey (размер=${it.size})")
                return@withContext it
            }
        }
        
        // Если нет в кэше или кэш устарел, загружаем из базы данных
        try {
            // Создаем граничные даты для запроса
            val calendar = java.util.Calendar.getInstance()
            calendar.clear()
            calendar.set(java.util.Calendar.YEAR, year)
            calendar.set(java.util.Calendar.WEEK_OF_YEAR, week)
            calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek())
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startDate = calendar.time
            
            calendar.add(java.util.Calendar.DAY_OF_WEEK, 6)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val endDate = calendar.time
            
            // Запрашиваем транзакции за неделю
            val transactions = dao.getTransactionsByDateRangePaginated(startDate, endDate, 1000, 0)
                .map { mapEntityToDomain(it) }
            
            // Если в кэше есть данные, обновляем только эту неделю
            if (transactionsCache.isNotEmpty()) {
                synchronized(cacheLock) {
                    weeklyTransactionsCache[weekKey] = transactions
                }
            } else {
                // Иначе загружаем все транзакции и обновляем все кэши
                getAllTransactions()
            }
            
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
                it.date in startDate..endDate 
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
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
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
            val count = transactionsCache.count { it.date in startDate..endDate }
            Timber.d("Получено количество транзакций в диапазоне дат из кэша: $count")
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
    override suspend fun getTransactionById(id: String): Transaction? {
        return try {
            val transactionId = if (id.toLongOrNull() != null) {
                // Если ID числовой, используем его как Long
                id.toLong()
            } else {
                // Иначе используем хэш от строки в качестве Long ID
                id.hashCode().toLong()
            }
            
            Timber.d("РЕПОЗИТОРИЙ: Получение транзакции по ID: $id (преобразовано в $transactionId)")
            val entity = dao.getTransactionById(transactionId)
            
            if (entity != null) {
                Timber.d("РЕПОЗИТОРИЙ: Транзакция найдена")
                mapEntityToDomain(entity)
            } else {
                Timber.d("РЕПОЗИТОРИЙ: Транзакция не найдена")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при получении транзакции по ID '$id': ${e.message}")
            null
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
            // Добавляем транзакцию в базу данных
            val id = dao.insertTransaction(mapDomainToEntity(transaction))
            
            // Создаем копию транзакции с правильным ID
            val addedTransaction = transaction.copy(id = id.toString())
            
            // Логируем информацию о добавленной транзакции
            Timber.d("Добавлена транзакция: ID=$id, дата=${transaction.date}, сумма=${transaction.amount}")
            
            // Обновляем кэш, если он не пустой
            synchronized(cacheLock) {
                if (transactionsCache.isNotEmpty()) {
                    transactionsCache.add(0, addedTransaction) // Добавляем в начало списка
                    Timber.d("Транзакция добавлена в кэш, новый размер=${transactionsCache.size}")
                    
                    // Определяем месяц и неделю для обновления соответствующих кэшей
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = transaction.date
                    
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH) + 1
                    val monthKey = "$year-${month.toString().padStart(2, '0')}"
                    
                    // Обновляем кэш месяца
                    val monthTransactions = monthlyTransactionsCache[monthKey]?.toMutableList() ?: mutableListOf()
                    monthTransactions.add(0, addedTransaction)
                    monthlyTransactionsCache[monthKey] = monthTransactions
                    
                    // Определяем и обновляем кэш недели
                    val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
                    val weekKey = "$year-W${week.toString().padStart(2, '0')}"
                    
                    val weekTransactions = weeklyTransactionsCache[weekKey]?.toMutableList() ?: mutableListOf()
                    weekTransactions.add(0, addedTransaction)
                    weeklyTransactionsCache[weekKey] = weekTransactions
                }
            }
            
            // Обновляем финансовые метрики
            try {
                val financialMetrics = FinancialMetrics.getInstance()
                financialMetrics.updateAfterAdd(addedTransaction)
                Timber.d("Финансовые метрики обновлены после добавления транзакции")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении финансовых метрик: ${e.message}")
            }
            
            return@withContext id.toString()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при добавлении транзакции: ${e.message}")
            throw e
        }
    }
    
    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    override suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        try {
            // Сначала получаем старую версию транзакции для правильного обновления метрик
            val oldTransactionEntity = transaction.id.toLongOrNull()?.let { dao.getTransactionById(it) }
            val oldTransaction = oldTransactionEntity?.let { mapEntityToDomain(it) }
            
            // Обновляем транзакцию в базе данных
            dao.updateTransaction(mapDomainToEntity(transaction))
            
            // Обновляем кэш транзакций
            synchronized(cacheLock) {
                if (transactionsCache.isNotEmpty()) {
                    // Находим и заменяем существующую транзакцию в кэше
                    val index = transactionsCache.indexOfFirst { it.id == transaction.id }
                    if (index >= 0) {
                        transactionsCache[index] = transaction
                        Timber.d("РЕПОЗИТОРИЙ: Транзакция обновлена в кэше по индексу $index")
                    } else {
                        Timber.w("РЕПОЗИТОРИЙ: Транзакция не найдена в кэше, добавляем")
                        transactionsCache.add(transaction)
                    }
                }
            }
            
            // Обновляем финансовые метрики, если старая транзакция найдена
            if (oldTransaction != null) {
                try {
                    val metrics = FinancialMetrics.getInstance()
                    
                    // Сначала удаляем эффект старой транзакции, потом добавляем новую
                    metrics.updateAfterDelete(oldTransaction)
                    metrics.updateAfterAdd(transaction)
                    
                    Timber.d("РЕПОЗИТОРИЙ: Финансовые метрики обновлены после обновления транзакции")
                } catch (e: Exception) {
                    Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при обновлении финансовых метрик: ${e.message}")
                }
            } else {
                Timber.w("РЕПОЗИТОРИЙ: Старая транзакция не найдена, пропуск обновления метрик")
            }
        } catch (e: Exception) {
            Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при обновлении транзакции: ${e.message}")
            throw e
        }
    }
    
    /**
     * Удаляет транзакцию по ID.
     * @param id ID транзакции для удаления.
     */
    override suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
        try {
            // Получаем транзакцию по ID для обновления метрик
            val transactionId = if (id.toLongOrNull() != null) {
                id.toLong()
            } else {
                id.hashCode().toLong()
            }
            
            val entity = dao.getTransactionById(transactionId)
            
            if (entity != null) {
                val transaction = mapEntityToDomain(entity)
                
                // Удаляем транзакцию из базы данных
                dao.deleteTransaction(entity)
                Timber.d("РЕПОЗИТОРИЙ: Транзакция с ID $id удалена из базы данных")
                
                // Обновляем кэши
                synchronized(cacheLock) {
                    if (transactionsCache.isNotEmpty()) {
                        // Удаляем из общего кэша
                        transactionsCache.removeIf { it.id == id }
                        Timber.d("РЕПОЗИТОРИЙ: Транзакция удалена из кэша")
                        
                        // Определяем месяц и неделю для обновления соответствующих кэшей
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = transaction.date
                        
                        val year = calendar.get(java.util.Calendar.YEAR)
                        val month = calendar.get(java.util.Calendar.MONTH) + 1
                        val monthKey = "$year-${month.toString().padStart(2, '0')}"
                        
                        // Обновляем кэш месяца
                        monthlyTransactionsCache[monthKey]?.let { transactions ->
                            monthlyTransactionsCache[monthKey] = transactions.filter { it.id != id }
                        }
                        
                        // Определяем и обновляем кэш недели
                        val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
                        val weekKey = "$year-W${week.toString().padStart(2, '0')}"
                        
                        weeklyTransactionsCache[weekKey]?.let { transactions ->
                            weeklyTransactionsCache[weekKey] = transactions.filter { it.id != id }
                        }
                    }
                }
                
                // Обновляем финансовые метрики
                try {
                    val financialMetrics = FinancialMetrics.getInstance()
                    financialMetrics.updateAfterDelete(transaction)
                    Timber.d("РЕПОЗИТОРИЙ: Финансовые метрики обновлены после удаления транзакции")
                } catch (e: Exception) {
                    Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при обновлении финансовых метрик: ${e.message}")
                }
            }
        } catch (e: NumberFormatException) {
            Timber.w("РЕПОЗИТОРИЙ: ID транзакции не является числом: $id")
        } catch (e: Exception) {
            Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при удалении транзакции: ${e.message}")
            throw e
        }
    }

    /**
     * Удаляет транзакцию (метод из ITransactionRepository)
     * @param transaction Транзакция для удаления
     */
    override suspend fun deleteTransaction(transaction: Transaction) {
        // Делегируем вызов методу deleteTransaction по ID
        deleteTransaction(transaction.id)
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
     * Преобразует сущность в доменную модель.
     * @param entity Сущность транзакции.
     * @return Доменная модель транзакции.
     */
    private fun mapEntityToDomain(entity: TransactionEntity): Transaction {
        return Transaction(
            id = entity.id.toString(),
            amount = entity.amount.toDouble(),
            category = entity.category,
            date = entity.date,
            isExpense = entity.isExpense,
            note = entity.note,
            source = entity.source
        )
    }
    
    /**
     * Преобразует доменную модель в сущность.
     * @param domain Доменная модель транзакции.
     * @return Сущность транзакции.
     */
    private fun mapDomainToEntity(domain: Transaction): TransactionEntity {
        val id = try {
            domain.id.toLong()
        } catch (e: NumberFormatException) {
            0L
        }
        
        return TransactionEntity(
            id = id,
            amount = domain.amount.toString(),
            category = domain.category,
            date = domain.date,
            isExpense = domain.isExpense,
            note = domain.note,
            source = domain.source ?: "Наличные"
        )
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
} 