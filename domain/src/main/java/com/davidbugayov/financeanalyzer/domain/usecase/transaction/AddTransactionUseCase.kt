package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.StringProvider
import timber.log.Timber

/**
 * UseCase для добавления новой транзакции.
 * Добавляет транзакцию в репозиторий и возвращает созданную транзакцию.
 *
 * @param transactionRepository Репозиторий транзакций.
 */
class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction): Result<String> {
        return try {
            Timber.d(
                StringProvider.logTransactionAdd(
                    transaction.amount.toString(),
                    transaction.category
                )
            )
            val transactionId = transactionRepository.addTransaction(transaction)
            Result.success(transactionId)
        } catch (e: Exception) {
            Result.error(AppException.mapException(e))
        }
    }
}
