package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * KMP-интерфейс репозитория транзакций. Реализуется на платформах.
 */
interface TransactionRepository {
    suspend fun loadTransactions(): List<Transaction>
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
}


