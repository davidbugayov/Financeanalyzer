package com.davidbugayov.financeanalyzer.presentation.budget.model

import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory

/**
 * События для экрана бюджета
 */
sealed class BudgetEvent {
    // Базовые события для управления категориями
    data class AddCategory(val name: String, val limit: Double) : BudgetEvent()
    data class UpdateCategory(val category: BudgetCategory) : BudgetEvent()
    data class DeleteCategory(val category: BudgetCategory) : BudgetEvent()
    data object LoadCategories : BudgetEvent()
    data object ClearError : BudgetEvent()

    // События для работы с "виртуальными кошельками"
    data class DistributeIncome(val amount: Double) : BudgetEvent()
    data class AddFundsToWallet(val categoryId: String, val amount: Double) : BudgetEvent()
    data class SpendFromWallet(val categoryId: String, val amount: Double) : BudgetEvent()
    data class TransferBetweenWallets(
        val fromCategoryId: String,
        val toCategoryId: String,
        val amount: Double
    ) : BudgetEvent()

    // События для настройки периодов
    data class SetPeriodDuration(val days: Int) : BudgetEvent()
    data class ResetPeriod(val categoryId: String) : BudgetEvent()
    data object ResetAllPeriods : BudgetEvent()
} 