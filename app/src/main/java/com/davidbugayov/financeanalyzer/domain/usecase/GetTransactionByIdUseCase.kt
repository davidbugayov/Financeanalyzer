package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: String): Result<Transaction> {
        return try {
            val transaction = transactionRepository.getTransactionById(id)
            if (transaction != null) Result.success(transaction)
            else Result.failure(Exception("Транзакция не найдена"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 