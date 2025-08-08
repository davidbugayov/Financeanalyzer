package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext
import com.davidbugayov.financeanalyzer.domain.R
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
            val rp: ResourceProvider = GlobalContext.get().get()
            Timber.d(rp.getString(R.string.log_transaction_update, transaction.toString()))
            transactionRepository.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(AppException.mapException(e))
        }
    }
}
