package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository

/**
 * Use case для обновления транзакции.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class UpdateTransactionUseCase(private val repository: ITransactionRepository) {
    /**
     * Обновляет существующую транзакцию в репозитории
     * @param transaction Обновленная транзакция
     */
    suspend operator fun invoke(transaction: Transaction) {
        repository.updateTransaction(transaction)
    }
} 