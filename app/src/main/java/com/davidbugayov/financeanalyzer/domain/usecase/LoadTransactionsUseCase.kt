package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository

/**
 * Use case для загрузки транзакций.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class LoadTransactionsUseCase(private val repository: ITransactionRepository) {
    /**
     * Загружает все транзакции из репозитория
     * @return Список транзакций
     */
    suspend operator fun invoke(): List<Transaction> {
        return repository.loadTransactions()
    }
}