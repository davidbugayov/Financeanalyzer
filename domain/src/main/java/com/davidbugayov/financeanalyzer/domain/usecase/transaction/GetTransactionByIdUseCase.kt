package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.util.Result
import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext

/**
 * UseCase для получения транзакции по ID.
 * Возвращает транзакцию с указанным ID или ошибку, если транзакция не найдена.
 *
 * @param transactionRepository Репозиторий транзакций.
 */
class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: String): Result<Transaction> {
        return try {
            val transaction = transactionRepository.getTransactionById(id)
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.error(
                    AppException.Data.NotFound(
                        GlobalContext.get().get<ResourceProvider>().getStringByName("log_transaction_not_found")
                    )
                )
            }
        } catch (e: Exception) {
            Result.error(AppException.mapException(e))
        }
    }
}
