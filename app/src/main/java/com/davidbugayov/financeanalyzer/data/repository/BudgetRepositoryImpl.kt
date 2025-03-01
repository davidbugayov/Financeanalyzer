package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.BudgetDao
import com.davidbugayov.financeanalyzer.domain.model.Budget
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import com.davidbugayov.financeanalyzer.data.local.model.BudgetWithSpending
import kotlinx.coroutines.flow.Flow
import java.util.Date

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {
    override fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAllBudgets()

    override fun getActiveBudgets(): Flow<List<Budget>> =
        budgetDao.getActiveBudgets()

    override fun getBudgetsByCategory(category: String): Flow<List<Budget>> =
        budgetDao.getBudgetsByCategory(category)

    override suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insertBudget(budget)

    override suspend fun updateBudget(budget: Budget) =
        budgetDao.updateBudget(budget)

    override suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget)

    override fun getCurrentBudgets(date: Date): Flow<List<Budget>> =
        budgetDao.getCurrentBudgets(date)

    override fun getBudgetsWithSpending(currentDate: Date): Flow<List<BudgetWithSpending>> =
        budgetDao.getBudgetsWithSpending(currentDate)
} 