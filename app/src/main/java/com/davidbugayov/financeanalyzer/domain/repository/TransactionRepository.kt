package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionStats
import com.davidbugayov.financeanalyzer.data.local.model.CategoryTotal
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    fun getTransactionsByTags(tags: List<String>): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun importTransactions(transactions: List<Transaction>)
    suspend fun exportTransactions(startDate: Date, endDate: Date): String // Returns file path
    fun getTransactionStats(startDate: Date, endDate: Date): Flow<TransactionStats>
    suspend fun syncTransactions() // For cloud sync
    fun getTransactionsByCategories(categories: List<String>, startDate: Date, endDate: Date): Flow<List<Transaction>>
    fun getTotalByType(isExpense: Boolean, startDate: Date, endDate: Date): Flow<Double?>
    fun getCategoryTotals(startDate: Date, endDate: Date): Flow<List<CategoryTotal>>
} 