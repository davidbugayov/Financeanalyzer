package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Возвращает список кодов советов по экономии на основе транзакций.
 * UI обязан маппить коды на строки и подставлять параметры.
 */
class GetSmartExpenseTipsUseCase {
    fun invoke(transactions: List<Transaction>): List<String> {
        val tips = mutableListOf<String>()
        if (transactions.isEmpty()) return tips

        val expenseByCategory = transactions
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount.toMajorDouble() } }
            .toList()
            .sortedByDescending { it.second }

        if (expenseByCategory.isNotEmpty() && expenseByCategory.first().second > 0.0) {
            tips += "tip_top_category_spending"
        }

        val subscriptions = transactions.filter { it.isExpense && (
            (it.note?.contains("подписк", ignoreCase = true) == true) ||
                it.category.contains("подписк", ignoreCase = true) ||
                it.category.contains("subscription", ignoreCase = true)
            )
        }
        if (subscriptions.isNotEmpty()) tips += "tip_subscriptions_found"

        val largeExpenses = transactions.filter { it.isExpense && it.amount.toMajorDouble() > 5000 }
        if (largeExpenses.isNotEmpty()) tips += "tip_large_expenses"

        val smallExpenses = transactions.filter { it.isExpense && it.amount.toMajorDouble() < 300 }
        if (smallExpenses.size > 10) tips += "tip_small_expenses"

        val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() }
        val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount.toMajorDouble() }
        if (totalIncome > 0 && totalExpense >= totalIncome) tips += "tip_expenses_equal_income"

        return tips
    }
}


