package com.davidbugayov.financeanalyzer.shared.usecase.transaction

import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String) = repository.deleteTransaction(id)
}


