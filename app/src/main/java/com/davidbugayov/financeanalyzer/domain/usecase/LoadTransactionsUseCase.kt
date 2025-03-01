package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.data.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.data.model.Transaction

class LoadTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): List<Transaction> {
        return repository.loadTransactions()
    }
}