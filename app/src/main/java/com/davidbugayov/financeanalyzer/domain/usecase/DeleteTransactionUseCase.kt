package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository

/**
 * Use case для удаления транзакции.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class DeleteTransactionUseCase(private val repository: ITransactionRepository) {
    /**
     * Удаляет транзакцию из репозитория
     * @param transaction Транзакция для удаления
     */
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
} 