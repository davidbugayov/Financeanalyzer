package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Репозиторий для работы с транзакциями.
 * Следует принципам Clean Architecture.
 */
interface TransactionRepository {
    /**
     * Получает все транзакции.
     * @return Список всех транзакций.
     */
    suspend fun getAllTransactions(): List<Transaction>

    /**
     * Получает транзакции с пагинацией.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации.
     */
    suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<Transaction>

    /**
     * Получает общее количество транзакций.
     * @return Общее количество транзакций в базе данных.
     */
    suspend fun getTransactionsCount(): Int
    
    /**
     * Получает транзакцию по идентификатору.
     * @param id Идентификатор транзакции.
     * @return Транзакция или null, если транзакция не найдена.
     */
    suspend fun getTransactionById(id: String): Transaction?
    
    /**
     * Добавляет новую транзакцию.
     * @param transaction Транзакция для добавления.
     */
    suspend fun addTransaction(transaction: Transaction)
    
    /**
     * Обновляет существующую транзакцию.
     * @param transaction Транзакция для обновления.
     */
    suspend fun updateTransaction(transaction: Transaction)
    
    /**
     * Удаляет транзакцию.
     * @param id Идентификатор транзакции для удаления.
     */
    suspend fun deleteTransaction(id: String)

    /**
     * Получает транзакции за указанный период.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Flow со списком транзакций.
     */
    suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>

    /**
     * Получает транзакции за указанный период с пагинацией.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @param limit Количество транзакций для загрузки.
     * @param offset Смещение (количество пропускаемых транзакций).
     * @return Список транзакций с учетом пагинации и диапазона дат.
     */
    suspend fun getTransactionsByDateRangePaginated(
        startDate: Date, 
        endDate: Date,
        limit: Int,
        offset: Int
    ): List<Transaction>

    /**
     * Получает общее количество транзакций в указанном диапазоне дат.
     * @param startDate Начальная дата периода.
     * @param endDate Конечная дата периода.
     * @return Количество транзакций в указанном диапазоне дат.
     */
    suspend fun getTransactionsCountByDateRange(startDate: Date, endDate: Date): Int
} 