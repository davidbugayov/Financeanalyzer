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

/**
 * Реализация репозитория для работы с транзакциями.
 * Использует Room DAO для доступа к данным.
 * Реализует оба интерфейса репозитория для обеспечения совместимости.
 * @param dao DAO для работы с транзакциями.
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository, ITransactionRepository {
    
    /**
     * Получает все транзакции.
     * @return Список всех транзакций.
     */
    override suspend fun getAllTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        dao.getAllTransactions().map { mapEntityToDomain(it) }
    }

    /**
     * Получает транзакции с поддержкой пагинации.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации.
     */
    override suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<Transaction> = withContext(Dispatchers.IO) {
        dao.getTransactionsPaginated(limit, offset).map { mapEntityToDomain(it) }
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
        dao.getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
            .map { mapEntityToDomain(it) }
    }

    /**
     * Загружает транзакции с пагинацией (метод из ITransactionRepository)
     * @param limit Количество транзакций для загрузки
     * @param offset Смещение (количество пропускаемых транзакций)
     * @return Список транзакций с учетом пагинации
     */
    override suspend fun loadTransactionsPaginated(limit: Int, offset: Int): List<Transaction> {
        return getTransactionsPaginated(limit, offset)
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
    ): List<Transaction> {
        return getTransactionsByDateRangePaginated(startDate, endDate, limit, offset)
    }

    /**
     * Получает общее количество транзакций
     * @return Общее количество транзакций в базе данных
     */
    override suspend fun getTransactionsCount(): Int = withContext(Dispatchers.IO) {
        dao.getTransactionsCount()
    }

    /**
     * Получает общее количество транзакций в указанном диапазоне дат
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Количество транзакций в указанном диапазоне дат
     */
    override suspend fun getTransactionsCountByDateRange(startDate: Date, endDate: Date): Int = withContext(Dispatchers.IO) {
        dao.getTransactionsCountByDateRange(startDate, endDate)
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
     * @param transaction Транзакция для добавления.
     */
    override suspend fun addTransaction(transaction: Transaction) {
        try {
            Timber.d("РЕПОЗИТОРИЙ: Добавление транзакции с id=${transaction.id}, дата=${transaction.date}")
            dao.insertTransaction(mapDomainToEntity(transaction))
            Timber.d("РЕПОЗИТОРИЙ: Транзакция успешно добавлена")
        } catch (e: Exception) {
            Timber.e(e, "РЕПОЗИТОРИЙ: Ошибка при добавлении транзакции: ${e.message}")
            throw e
        }
    }
    
    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    override suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        dao.updateTransaction(mapDomainToEntity(transaction))
    }
    
    /**
     * Удаляет транзакцию по ID.
     * @param id Идентификатор транзакции для удаления.
     */
    override suspend fun deleteTransaction(id: String): Unit = withContext(Dispatchers.IO) {
        try {
            val transactionId = id.toLong()
            dao.getTransactionById(transactionId)?.let {
                dao.deleteTransaction(it)
            }
        } catch (e: NumberFormatException) {
            // Игнорируем ошибку, если id не является числом
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
} 