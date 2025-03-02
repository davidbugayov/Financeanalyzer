package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository

/**
 * Use case для добавления новой транзакции.
 * Следует принципу единственной ответственности (Single Responsibility Principle).
 */
class AddTransactionUseCase(private val repository: ITransactionRepository) {
    /**
     * Добавляет новую транзакцию в репозиторий
     * @param transaction Транзакция для добавления
     */
    suspend operator fun invoke(transaction: Transaction) {
        repository.addTransaction(transaction)
    }
}