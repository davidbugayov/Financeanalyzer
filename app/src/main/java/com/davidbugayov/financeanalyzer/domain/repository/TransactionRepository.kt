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
} 