package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.StringProvider
import timber.log.Timber

/**
 * UseCase для обновления транзакции.
 * Обновляет существующую транзакцию в репозитории.
 *
 * @param transactionRepository Репозиторий транзакций.
 */
class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            Timber.d(StringProvider.logTransactionUpdate(transaction.toString()))
            transactionRepository.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.mapException(e))
        }
    }
}
