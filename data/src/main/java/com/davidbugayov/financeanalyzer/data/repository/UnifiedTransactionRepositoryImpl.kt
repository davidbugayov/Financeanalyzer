package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.DataChangeEvent
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.UnifiedTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Calendar
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map

/**
 * Реализация унифицированного репозитория транзакций.
 * Предоставляет единый интерфейс для работы с транзакциями.
 */
class UnifiedTransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val transactionMapper: TransactionMapper,
) : UnifiedTransactionRepository, TransactionRepository, ITransactionRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Кэш для хранения транзакций
    private val transactionsCache = mutableMapOf<String, Transaction>()

    // Поток событий изменения данных
    private val _dataChangeEvents = MutableSharedFlow<DataChangeEvent>()
    override val dataChangeEvents: SharedFlow<DataChangeEvent> = _dataChangeEvents

    /**
     * Принудительно отправляет событие изменения данных.
     */
    override suspend fun notifyDataChanged(transactionId: String?) {
        _dataChangeEvents.emit(DataChangeEvent.TransactionChanged(transactionId))
    }

    /**
     * Получает все транзакции.
     */
    override suspend fun getAllTransactions(): List<Transaction> {
        return loadTransactions()
    }

    /**
     * Получает транзакции с пагинацией.
     */
    override suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<Transaction> {
        return loadTransactionsPaginated(limit, offset)
    }

    /**
     * Получает транзакции за указанный период с пагинацией из ITransactionRepository.
     */
    override suspend fun getTransactionsPaginated(startDate: Date, endDate: Date, limit: Int, offset: Int): List<Transaction> {
        return getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
    }

    /**
     * Получает транзакции за указанный период с пагинацией.
     */
    override suspend fun getTransactionsByDateRangePaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int,
    ): List<Transaction> {
        val entities = transactionDao.getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
        return entities.map { transactionMapper.mapFromEntity(it) }
    }

    /**
     * Получает транзакции за указанный период.
     */
    override suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        // Создаем Flow, который будет возвращать транзакции за указанный период
        return flow {
            val transactions = getTransactionsByDateRange(startDate, endDate)
            emit(transactions)
        }
    }

    /**
     * Получает транзакции за указанный период.
     */
    override suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction> {
        val entities = transactionDao.getTransactionsByDateRange(startDate, endDate)
        return entities.map { transactionMapper.mapFromEntity(it) }
    }

    /**
     * Получает транзакции за указанный период с использованием LocalDate.
     */
    override suspend fun getTransactionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Transaction> {
        // Преобразование LocalDate в java.util.Date
        val startJavaDate = Date(startDate.toJavaLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        val endJavaDate =
            Date(endDate.toJavaLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1)

        return getTransactionsByDateRange(startJavaDate, endJavaDate)
    }

    /**
     * Получает список транзакций за указанный период (не Flow).
     */
    override suspend fun getTransactionsByDateRangeList(startDate: Date, endDate: Date): List<Transaction> {
        return getTransactionsByDateRange(startDate, endDate)
    }

    /**
     * Получает транзакции за указанный месяц.
     */
    override suspend fun getTransactionsByMonth(year: Int, month: Int): List<Transaction> {
        // Создаем начальную дату для указанного месяца
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH начинается с 0
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        // Создаем конечную дату для указанного месяца
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.time

        return getTransactionsByDateRange(startDate, endDate)
    }

    /**
     * Получает транзакции за указанную неделю.
     */
    override suspend fun getTransactionsByWeek(year: Int, week: Int): List<Transaction> {
        // Получаем первый день года
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.WEEK_OF_YEAR, week)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        // Последний день недели
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time

        return getTransactionsByDateRange(startDate, endDate)
    }

    /**
     * Получает общее количество транзакций.
     */
    override suspend fun getTransactionsCount(): Int {
        return transactionDao.getTransactionsCount()
    }

    /**
     * Получает общее количество транзакций в указанном диапазоне дат.
     */
    override suspend fun getTransactionsCountByDateRange(startDate: Date, endDate: Date): Int {
        return transactionDao.getTransactionsCountByDateRange(startDate, endDate)
    }

    /**
     * Загружает все транзакции.
     */
    override suspend fun loadTransactions(): List<Transaction> {
        val entities = transactionDao.getAllTransactions()
        return entities.map { transactionMapper.mapFromEntity(it) }
    }

    /**
     * Загружает транзакции с пагинацией.
     */
    override suspend fun loadTransactionsPaginated(limit: Int, offset: Int): List<Transaction> {
        val entities = transactionDao.getTransactionsPaginated(limit, offset)
        return entities.map { transactionMapper.mapFromEntity(it) }
    }

    /**
     * Добавляет новую транзакцию.
     */
    override suspend fun addTransaction(transaction: Transaction): String {
        val entity = transactionMapper.mapToEntity(transaction)
        transactionDao.insertTransaction(entity)

        // Обновляем кэш
        transactionsCache[transaction.id] = transaction

        // Уведомляем об изменении данных
        coroutineScope.launch {
            notifyDataChanged(transaction.id)
        }

        return transaction.id
    }

    /**
     * Обновляет существующую транзакцию.
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        val entity = transactionMapper.mapToEntity(transaction)
        // Use insert with REPLACE strategy to upsert based on unique id_string
        transactionDao.insertTransaction(entity)

        // Обновляем кэш
        transactionsCache[transaction.id] = transaction

        // Уведомляем об изменении данных
        coroutineScope.launch {
            notifyDataChanged(transaction.id)
        }
    }

    /**
     * Удаляет транзакцию.
     */
    override suspend fun deleteTransaction(transaction: Transaction) {
        // Удаляем по строковому id, чтобы избежать проблем с Room и первичным ключом
        transactionDao.deleteTransactionById(transaction.id)

        // Удаляем из кэша
        transactionsCache.remove(transaction.id)

        // Уведомляем об изменении данных
        coroutineScope.launch {
            notifyDataChanged(transaction.id)
        }
    }

    /**
     * Удаляет транзакцию по идентификатору.
     */
    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransactionById(id)

        // Удаляем из кэша
        transactionsCache.remove(id)

        // Уведомляем об изменении данных
        coroutineScope.launch {
            notifyDataChanged(id)
        }
    }

    /**
     * Получает транзакцию по идентификатору.
     */
    override suspend fun getTransactionById(id: String): Transaction? {
        // Проверяем кэш
        transactionsCache[id]?.let { return it }

        // Если в кэше нет, загружаем из базы
        val entity = transactionDao.getTransactionByIdString(id) ?: return null
        val transaction = transactionMapper.mapFromEntity(entity)

        // Сохраняем в кэш
        transactionsCache[id] = transaction

        return transaction
    }

    /**
     * Получает сущность по идентификатору.
     */
    override suspend fun getById(id: String): Transaction? {
        return getTransactionById(id)
    }

    /**
     * Получает поток всех сущностей.
     */
    override fun getAll(): Flow<List<Transaction>> {
        // Используем реактивный поток Room — новые данные прилетают автоматически
        return transactionDao.observeAllTransactions()
            .map { entities ->
                entities.map { transactionMapper.mapFromEntity(it) }
            }
    }

    /**
     * Добавляет новую сущность.
     */
    override suspend fun add(item: Transaction): String {
        return addTransaction(item)
    }

    /**
     * Обновляет существующую сущность.
     */
    override suspend fun update(item: Transaction) {
        updateTransaction(item)
    }

    /**
     * Удаляет сущность по идентификатору.
     */
    override suspend fun delete(id: String): Boolean {
        val transaction = getTransactionById(id) ?: return false
        deleteTransaction(transaction)
        return true
    }

    /**
     * Получает транзакции за указанный период.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Список транзакций за указанный период.
     */
    override suspend fun getTransactionsForPeriod(startDate: Date, endDate: Date): List<Transaction> {
        return getTransactionsByDateRange(startDate, endDate)
    }
    
    /**
     * Получает транзакции за указанный период с пагинацией.
     */
    override suspend fun getTransactionsByPeriodPaginated(
        startDate: Date,
        endDate: Date,
        limit: Int,
        offset: Int
    ): List<Transaction> {
        return getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
    }

    // ---------------- Paging API ----------------

    override fun getAllPaged(pageSize: Int): Flow<PagingData<Transaction>> = Pager(
        config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
        pagingSourceFactory = { transactionDao.pagingSourceAll() },
    ).flow.map { pagingData -> pagingData.map { transactionMapper.mapFromEntity(it) } }

    override fun getByPeriodPaged(startDate: Date, endDate: Date, pageSize: Int): Flow<PagingData<Transaction>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { transactionDao.pagingSourceByDateRange(startDate, endDate) },
        ).flow.map { pagingData -> pagingData.map { transactionMapper.mapFromEntity(it) } }
} 