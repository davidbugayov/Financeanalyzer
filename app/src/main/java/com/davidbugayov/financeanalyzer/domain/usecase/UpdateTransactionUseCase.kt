package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.safeCall

/**
 * Use case для обновления транзакции.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class UpdateTransactionUseCase(
    private val repository: ITransactionRepository
) {
    /**
     * Обновляет существующую транзакцию в репозитории
     * @param transaction Обновленная транзакция
     * @return Результат операции
     */
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return safeCall {
            repository.updateTransaction(transaction)
        }
    }
} 