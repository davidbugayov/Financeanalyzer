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
    private val cacheLock = Any() // Для синхронизации доступа к кэшу
    
    // Кэши для различных типов группировки
    private val monthlyTransactionsCache = Collections.synchronizedMap<String, List<Transaction>>(mutableMapOf())
    private val weeklyTransactionsCache = Collections.synchronizedMap<String, List<Transaction>>(mutableMapOf())
    
    // SharedFlow для уведомления об изменениях данных
    private val _dataChangeEvents = MutableSharedFlow<DataChangeEvent>(replay = 0, extraBufferCapacity = 1)
    override val dataChangeEvents: SharedFlow<DataChangeEvent> = _dataChangeEvents.asSharedFlow()

    /**
     * Очищает все кэши.
     */
    private fun clearCaches() {
        synchronized(cacheLock) {
            transactionsCache.clear()
            monthlyTransactionsCache.clear()
            weeklyTransactionsCache.clear()
            cacheLastUpdated = 0L
            Timber.d("Все кэши репозитория очищены")
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
            val calendar = Calendar.getInstance()
            calendar.time = transaction.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // +1 т.к. месяцы в Calendar начинаются с 0
            "$year-${month.toString().padStart(2, '0')}" // Формат YYYY-MM
        }
        
        // Сохраняем группировку по месяцам в кэш
        monthlyTransactionsCache.putAll(groupedByMonth)
        
        // Группируем транзакции по неделям
        val groupedByWeek = transactions.groupBy { transaction ->
            val calendar = Calendar.getInstance()
            calendar.time = transaction.date
            val year = calendar.get(Calendar.YEAR)
            val week = calendar.get(Calendar.WEEK_OF_YEAR)
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
            val transactions = dao.getTransactionsByDateRange(startDate, endDate).map { mapEntityToDomain(it) }
            
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
    override suspend fun getTransactionById(id: String): Transaction? {
        return try {
            // Попытка найти транзакцию напрямую в кэше
            val cachedTransaction = transactionsCache.find { it.id == id }
            if (cachedTransaction != null) {
                Timber.d("РЕПОЗИТОРИЙ: Транзакция найдена в кэше: ID=$id")
                return cachedTransaction
            }

            // Если в кэше не найдено, ищем все транзакции и находим нужную
            Timber.d("РЕПОЗИТОРИЙ: Поиск транзакции с ID=$id в базе данных")
            val allTransactions = dao.getAllTransactions()
            val entities = allTransactions.filter { it.idString == id }

            if (entities.isNotEmpty()) {
                val entity = entities.first()
                Timber.d("РЕПОЗИТОРИЙ: Транзакция найдена в базе: ID=$id")
                mapEntityToDomain(entity)
            } else {
                Timber.d("РЕПОЗИТОРИЙ: Транзакция не найдена: ID=$id")
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
            val entity = mapDomainToEntity(transaction)
            val id = dao.insertTransaction(entity)
            clearCaches() // Очищаем кэш после добавления
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

            // Очищаем все кэши
            try {
                clearCaches()
                Timber.d("Кэш очищен")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при очистке кэша: ${e.message}")
                // Продолжаем выполнение даже при ошибке кэша
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
            clearCaches() // Очищаем кэш после удаления
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
            clearCaches() // Очищаем кэш после удаления
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

        // КРИТИЧЕСКИ ВАЖНО: правильно обрабатываем ID
        var longId = 0L

        try {
            // Пытаемся преобразовать строковый ID в Long
            if (domain.id.isNotEmpty() && domain.id != "0") {
                // Если ID числовой, пробуем преобразовать напрямую
                longId = domain.id.toLongOrNull() ?: domain.id.hashCode().toLong()
                Timber.d("ID конвертирован в числовой формат: $longId")
            } else {
                // Для новых транзакций используем 0, чтобы Room сгенерировал новый ID
                Timber.d("Используем ID=0 для новой транзакции")
            }
        } catch (e: Exception) {
            // В случае ошибки конвертации используем хэш-код строки
            Timber.e(e, "Ошибка при конвертации ID, используем хэш-код: ${e.message}")
            longId = domain.id.hashCode().toLong()
        }

        val entity = TransactionEntity(
            id = longId,
            idString = domain.id,
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
} 