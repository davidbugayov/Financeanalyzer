package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.safeCall

/**
 * Use case для добавления новой транзакции.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class AddTransactionUseCase(
    private val repository: ITransactionRepository
) {
    /**
     * Добавляет новую транзакцию в репозиторий
     * @param transaction Транзакция для добавления
     * @return Результат операции
     */
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return safeCall {
            repository.addTransaction(transaction)
        }
    }
}