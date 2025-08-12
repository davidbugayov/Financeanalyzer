package com.davidbugayov.financeanalyzer.shared.usecase.transaction

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository

class UpdateTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.updateTransaction(transaction)
}


