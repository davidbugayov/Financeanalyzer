package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.*
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionStats
import com.davidbugayov.financeanalyzer.data.local.model.CategoryTotal
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE EXISTS (
            SELECT 1 FROM transaction_tags 
            WHERE transaction_id = transactions.id 
            AND tag IN (:tags)
        )
        ORDER BY date DESC
    """)
    fun getTransactionsByTags(tags: List<String>): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("""
        SELECT * FROM transactions 
        WHERE category IN (:categories) 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategories(categories: List<String>, startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE isExpense = :isExpense 
        AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalByType(isExpense: Boolean, startDate: Date, endDate: Date): Flow<Double?>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY category
    """)
    fun getCategoryTotals(startDate: Date, endDate: Date): Flow<List<CategoryTotal>>

    @Transaction
    @Query("""
        SELECT 
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isExpense = 0 AND date BETWEEN :startDate AND :endDate) as totalIncome,
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isExpense = 1 AND date BETWEEN :startDate AND :endDate) as totalExpenses,
            (SELECT COALESCE(SUM(CASE WHEN isExpense = 0 THEN amount ELSE -amount END), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate) as netAmount,
            (SELECT COUNT(*) FROM transactions WHERE date BETWEEN :startDate AND :endDate) as transactionCount,
            (SELECT COALESCE(AVG(amount), 0) FROM transactions WHERE date BETWEEN :startDate AND :endDate) as averageTransactionAmount
    """)
    fun getTransactionStats(startDate: Date, endDate: Date): Flow<TransactionStats>
} 