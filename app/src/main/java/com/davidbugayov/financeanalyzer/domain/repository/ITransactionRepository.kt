package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction

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
} 