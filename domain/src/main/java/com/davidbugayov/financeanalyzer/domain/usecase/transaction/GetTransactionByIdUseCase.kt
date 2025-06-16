package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.core.model.AppException
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository,
) {

    suspend operator fun invoke(id: String): CoreResult<Transaction> {
        return try {
            val transaction = transactionRepository.getTransactionById(id)
            if (transaction != null) {
                CoreResult.success(transaction)
            } else {
                CoreResult.error(AppException.Data.NotFound("Транзакция с id=$id не найдена"))
            }
        } catch (e: Exception) {
            CoreResult.error(AppException.mapException(e))
        }
    }
}
