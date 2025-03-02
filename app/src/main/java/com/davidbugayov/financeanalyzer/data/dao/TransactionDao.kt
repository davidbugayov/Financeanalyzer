package com.davidbugayov.financeanalyzer.data.dao

import androidx.room.*
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionStats
import com.davidbugayov.financeanalyzer.domain.model.TransactionTag
import com.davidbugayov.financeanalyzer.domain.model.TransactionWithTags
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Insert
    suspend fun insertTag(tag: TransactionTag)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transaction_tags WHERE transaction_id = :transactionId")
    suspend fun deleteTagsForTransaction(transactionId: Long)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionWithTags>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<TransactionWithTags>>

    @Transaction
    @Query("""
        SELECT DISTINCT t.* FROM transactions t
        INNER JOIN transaction_tags tt ON t.id = tt.transaction_id
        WHERE tt.tag IN (:tags)
        GROUP BY t.id
        HAVING COUNT(DISTINCT tt.tag) = :tagCount
        ORDER BY t.date DESC
    """)
    fun getTransactionsByTags(tags: List<String>, tagCount: Int): Flow<List<TransactionWithTags>>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN isExpense = 0 THEN amount ELSE 0 END), 0) as totalIncome,
            COALESCE(SUM(CASE WHEN isExpense = 1 THEN amount ELSE 0 END), 0) as totalExpenses,
            COALESCE(SUM(CASE WHEN isExpense = 0 THEN amount ELSE -amount END), 0) as netAmount,
            COUNT(*) as transactionCount,
            COALESCE(AVG(amount), 0) as averageTransactionAmount
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getTransactionStats(startDate: Date, endDate: Date): Flow<TransactionStats>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    @Query("SELECT DISTINCT category FROM transactions")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT tag FROM transaction_tags")
    fun getAllTags(): Flow<List<String>>
} 