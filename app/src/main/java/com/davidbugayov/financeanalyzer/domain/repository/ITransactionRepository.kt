package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Интерфейс репозитория для работы с транзакциями.
 * Следует принципу инверсии зависимостей (Dependency Inversion Principle).
 * Высокоуровневые модули не должны зависеть от низкоуровневых модулей.
 * Оба должны зависеть от абстракций.
 */
interface ITransactionRepository {
    /**
     * Загружает все транзакции
     * @return Список транзакций
     */
    suspend fun loadTransactions(): List<Transaction>
    
    /**
     * Добавляет новую транзакцию
     * @param transaction Транзакция для добавления
     */
    suspend fun addTransaction(transaction: Transaction)
    
    /**
     * Удаляет транзакцию
     * @param transaction Транзакция для удаления
     */
    suspend fun deleteTransaction(transaction: Transaction)
    
    /**
     * Обновляет существующую транзакцию
     * @param transaction Обновленная транзакция
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Получает транзакции за указанный период
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Flow со списком транзакций
     */
    suspend fun getTransactions(startDate: Date, endDate: Date): Flow<List<Transaction>>
} 