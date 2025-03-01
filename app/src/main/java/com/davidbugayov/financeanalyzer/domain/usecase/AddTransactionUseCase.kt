package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.data.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.data.model.Transaction

class AddTransactionUseCase(private val repository: TransactionRepository) {
    operator fun invoke(transaction: Transaction) {
        repository.addTransaction(transaction)
    }
}