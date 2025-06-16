package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.core.util.safeCall
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import timber.log.Timber

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
    suspend operator fun invoke(transaction: Transaction): CoreResult<Unit> {
        return safeCall {
            Timber.d("Удаление транзакции: id=${transaction.id}, сумма=${transaction.amount}")
            repository.deleteTransaction(transaction)
        }
    }
}
