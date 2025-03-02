package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.*
import com.davidbugayov.financeanalyzer.domain.model.Budget
import com.davidbugayov.financeanalyzer.data.local.model.BudgetWithSpending
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY startDate DESC")
    fun getActiveBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category AND isActive = 1")
    fun getBudgetsByCategory(category: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("""
        SELECT * FROM budgets 
        WHERE startDate <= :date 
        AND (endDate IS NULL OR endDate >= :date)
        AND isActive = 1
    """)
    fun getCurrentBudgets(date: Date = Date()): Flow<List<Budget>>

    @Transaction
    @Query("""
        SELECT b.*, 
        (SELECT COALESCE(SUM(t.amount), 0) 
         FROM transactions t 
         WHERE t.category = b.category 
         AND t.date BETWEEN b.startDate AND COALESCE(b.endDate, :currentDate)
         AND t.isExpense = 1) as spent
        FROM budgets b
        WHERE b.isActive = 1
    """)
    fun getBudgetsWithSpending(currentDate: Date = Date()): Flow<List<BudgetWithSpending>>
} 