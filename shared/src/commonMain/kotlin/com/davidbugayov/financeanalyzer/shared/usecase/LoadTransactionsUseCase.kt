package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository

/**
 * KMP-загрузка транзакций из абстрактного репозитория.
 */
class LoadTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(): List<Transaction> = repository.loadTransactions()
}


