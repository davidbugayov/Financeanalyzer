package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionStats
import com.davidbugayov.financeanalyzer.data.local.model.CategoryTotal
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    override fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    override fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(category)

    override fun getTransactionsByTags(tags: List<String>): Flow<List<Transaction>> =
        transactionDao.getTransactionsByTags(tags)

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction)

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    override suspend fun importTransactions(transactions: List<Transaction>) {
        transactions.forEach { transactionDao.insertTransaction(it) }
    }

    override suspend fun exportTransactions(startDate: Date, endDate: Date): String {
        // TODO: Implement CSV export logic
        return ""
    }

    override fun getTransactionStats(startDate: Date, endDate: Date): Flow<TransactionStats> =
        transactionDao.getTransactionStats(startDate, endDate)

    override suspend fun syncTransactions() {
        // TODO: Implement cloud sync logic
    }

    override fun getTransactionsByCategories(categories: List<String>, startDate: Date, endDate: Date): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategories(categories, startDate, endDate)

    override fun getTotalByType(isExpense: Boolean, startDate: Date, endDate: Date): Flow<Double?> =
        transactionDao.getTotalByType(isExpense, startDate, endDate)

    override fun getCategoryTotals(startDate: Date, endDate: Date): Flow<List<CategoryTotal>> =
        transactionDao.getCategoryTotals(startDate, endDate)
} 