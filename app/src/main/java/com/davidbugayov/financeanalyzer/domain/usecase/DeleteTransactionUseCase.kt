package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.safeCall

/**
 * Use case для удаления транзакции.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class DeleteTransactionUseCase(private val repository: ITransactionRepository) {
    /**
     * Удаляет транзакцию из репозитория
     * @param transaction Транзакция для удаления
     * @return Результат операции
     */
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return safeCall {
            repository.deleteTransaction(transaction)
        }
    }
} 