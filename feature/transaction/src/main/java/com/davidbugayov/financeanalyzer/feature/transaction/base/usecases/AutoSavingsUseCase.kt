package com.davidbugayov.financeanalyzer.feature.transaction.base.usecases

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.shared.model.Money

class AutoSavingsUseCase {
    fun calculateSavings(
        incomeTransaction: Transaction,
        savingsPercentage: Double,
        savingsCategory: String,
    ): Pair<Transaction, Transaction>? {
        if (incomeTransaction.isExpense) return null
        if (savingsPercentage <= 0.0 || savingsPercentage >= 100.0) return null
        if (savingsCategory.isBlank()) return null

        val originalAmount = incomeTransaction.amount.amount
        val savingsAmount = (originalAmount * savingsPercentage) / 100.0
        val remainingAmount = originalAmount - savingsAmount

        if (savingsAmount < 0.01) return null

        val modifiedIncome = incomeTransaction.copy(
            amount = Money(remainingAmount, incomeTransaction.amount.currency),
        )

        val savingsTransaction = incomeTransaction.copy(
            id = System.currentTimeMillis().toString(),
            amount = Money(savingsAmount, incomeTransaction.amount.currency),
            category = savingsCategory,
            note = "Автоматические сбережения ($savingsPercentage%)",
        )

        return Pair(modifiedIncome, savingsTransaction)
    }
}
