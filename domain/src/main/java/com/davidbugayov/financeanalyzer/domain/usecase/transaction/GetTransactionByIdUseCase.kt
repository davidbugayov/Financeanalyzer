package com.davidbugayov.financeanalyzer.domain.usecase.transaction

import com.davidbugayov.financeanalyzer.domain.model.AppException
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository,
) {

    suspend operator fun invoke(id: String): Result<Transaction> {
        return try {
            val transaction = transactionRepository.getTransactionById(id)
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.error(AppException.Data.NotFound("Транзакция с id=$id не найдена"))
            }
        } catch (e: Exception) {
            Result.error(AppException.mapException(e))
        }
    }
} 