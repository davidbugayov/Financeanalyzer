package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.util.safeCall

/**
 * Use case для загрузки транзакций.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class LoadTransactionsUseCase(
    private val repository: ITransactionRepository
) {
    /**
     * Загружает все транзакции из репозитория
     * @return Результат операции со списком транзакций
     */
    suspend operator fun invoke(): Result<List<Transaction>> {
        return safeCall {
            repository.loadTransactions()
        }
    }
}