package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.core.util.safeCall
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import timber.log.Timber

/**
 * Use case для обновления существующей транзакции.
 */
class UpdateTransactionUseCase(
    private val repository: TransactionRepository,
) {
    /**
     * Обновляет транзакцию в базе данных.
     *
     * @param transaction Обновленная транзакция
     * @return Результат операции в виде Result<Unit>
     */
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return safeCall {
            Timber.d("Обновление транзакции: $transaction")
            repository.updateTransaction(transaction)
        }
    }
}
