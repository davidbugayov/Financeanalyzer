package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.preferences.BudgetCategoryPreferences
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.repository.BudgetRepository

/**
 * Реализация репозитория для работы с бюджетными категориями
 */
class BudgetRepositoryImpl(
    private val budgetCategoryPreferences: BudgetCategoryPreferences
) : BudgetRepository {

    override suspend fun getAllBudgetCategories(): List<BudgetCategory> {
        return budgetCategoryPreferences.getBudgetCategories()
    }

    override suspend fun getBudgetCategoryById(id: String): BudgetCategory? {
        return budgetCategoryPreferences.getBudgetCategories().find { it.id == id }
    }

    override suspend fun addBudgetCategory(budgetCategory: BudgetCategory) {
        budgetCategoryPreferences.addBudgetCategory(budgetCategory)
    }

    override suspend fun updateBudgetCategory(budgetCategory: BudgetCategory) {
        budgetCategoryPreferences.updateBudgetCategory(budgetCategory)
    }

    override suspend fun deleteBudgetCategory(budgetCategory: BudgetCategory) {
        budgetCategoryPreferences.removeBudgetCategory(budgetCategory.id)
    }

    override suspend fun deleteBudgetCategoryById(id: String) {
        budgetCategoryPreferences.removeBudgetCategory(id)
    }

    override suspend fun deleteAllBudgetCategories() {
        budgetCategoryPreferences.saveBudgetCategories(emptyList())
    }

    override suspend fun updateSpentAmount(id: String, spent: Money) {
        val category = getBudgetCategoryById(id) ?: return
        val updatedCategory = category.copy(spent = spent)
        budgetCategoryPreferences.updateBudgetCategory(updatedCategory)
    }

    override suspend fun hasBudgetCategories(): Boolean {
        return budgetCategoryPreferences.getBudgetCategories().isNotEmpty()
    }
} 