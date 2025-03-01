package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Budget
import com.davidbugayov.financeanalyzer.data.local.model.BudgetWithSpending
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<Budget>>
    fun getActiveBudgets(): Flow<List<Budget>>
    fun getBudgetsByCategory(category: String): Flow<List<Budget>>
    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
    fun getCurrentBudgets(date: Date = Date()): Flow<List<Budget>>
    fun getBudgetsWithSpending(currentDate: Date = Date()): Flow<List<BudgetWithSpending>>
} 