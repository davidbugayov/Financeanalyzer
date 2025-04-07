package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.BudgetCategoryDao
import com.davidbugayov.financeanalyzer.data.local.entity.BudgetCategoryEntity
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Реализация репозитория для работы с бюджетными категориями
 */
class BudgetRepositoryImpl(
    private val budgetCategoryDao: BudgetCategoryDao
) : BudgetRepository {

    override suspend fun getAllBudgetCategories(): List<BudgetCategory> {
        return budgetCategoryDao.getAllBudgetCategories().map { it.toDomain() }
    }

    override suspend fun getBudgetCategoryById(id: String): BudgetCategory? {
        return budgetCategoryDao.getBudgetCategoryById(id)?.toDomain()
    }

    override suspend fun addBudgetCategory(budgetCategory: BudgetCategory) {
        budgetCategoryDao.insertBudgetCategory(BudgetCategoryEntity.fromDomain(budgetCategory))
    }

    override suspend fun updateBudgetCategory(budgetCategory: BudgetCategory) {
        budgetCategoryDao.updateBudgetCategory(BudgetCategoryEntity.fromDomain(budgetCategory))
    }

    override suspend fun deleteBudgetCategory(budgetCategory: BudgetCategory) {
        budgetCategoryDao.deleteBudgetCategory(BudgetCategoryEntity.fromDomain(budgetCategory))
    }

    override suspend fun deleteBudgetCategoryById(id: String) {
        budgetCategoryDao.deleteBudgetCategoryById(id)
    }

    override suspend fun deleteAllBudgetCategories() {
        budgetCategoryDao.deleteAllBudgetCategories()
    }

    override suspend fun updateSpentAmount(id: String, spent: Double) {
        budgetCategoryDao.updateSpentAmount(id, spent)
    }

    override suspend fun hasBudgetCategories(): Boolean {
        return budgetCategoryDao.hasBudgetCategories()
    }
} 