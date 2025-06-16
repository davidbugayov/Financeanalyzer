package com.davidbugayov.financeanalyzer.presentation.budget.model
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Wallet

/**
 * События для экрана бюджета
 */
sealed class BudgetEvent {
    // Базовые события для управления кошельками
    data class AddCategory(val name: String, val limit: Money) : BudgetEvent()
    data class UpdateCategory(val category: Wallet) : BudgetEvent()
    data class DeleteCategory(val category: Wallet) : BudgetEvent()
    data object LoadCategories : BudgetEvent()
    data object ClearError : BudgetEvent()
    data class SetError(val message: String) : BudgetEvent()

    // События для работы с кошельками
    data class DistributeIncome(val amount: Money) : BudgetEvent()
    data class AddFundsToWallet(val categoryId: String, val amount: Money) : BudgetEvent()
    data class SpendFromWallet(val categoryId: String, val amount: Money) : BudgetEvent()
    data class TransferBetweenWallets(
        val fromCategoryId: String,
        val toCategoryId: String,
        val amount: Money,
    ) : BudgetEvent()

    // События для настройки периодов
    data class SetPeriodDuration(val days: Int) : BudgetEvent()
    data class ResetPeriod(val categoryId: String) : BudgetEvent()
    data object ResetAllPeriods : BudgetEvent()
}
