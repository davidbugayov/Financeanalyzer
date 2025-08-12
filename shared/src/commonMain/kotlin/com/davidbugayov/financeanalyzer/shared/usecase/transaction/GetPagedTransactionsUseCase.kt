package com.davidbugayov.financeanalyzer.shared.usecase.transaction

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * UseCase для получения пагинированных транзакций.
 * Упрощенная версия без androidx.paging для KMP.
 */
class GetPagedTransactionsUseCase(
    private val repository: TransactionRepository,
) {
    
    /**
     * Получает транзакции с пагинацией (упрощенно - берем первые pageSize элементов).
     */
    suspend operator fun invoke(pageSize: Int): List<Transaction> {
        val allTransactions = repository.loadTransactions()
        return allTransactions.take(pageSize)
    }

    /**
     * Получает транзакции за период с пагинацией.
     */
    suspend fun byPeriod(start: LocalDate, end: LocalDate, pageSize: Int): List<Transaction> {
        val allTransactions = repository.loadTransactions()
        return allTransactions.filter { transaction ->
            transaction.date >= start && transaction.date <= end
        }.take(pageSize)
    }
}
