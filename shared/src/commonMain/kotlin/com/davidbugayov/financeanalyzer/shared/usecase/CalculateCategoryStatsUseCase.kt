package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.CategoryStats
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

class CalculateCategoryStatsUseCase {
    operator fun invoke(transactions: List<Transaction>): Triple<List<CategoryStats>, Money, Money> {
        val filtered = transactions.filter { it.category.isNotBlank() }

        val totalExpense = filtered.filter { it.isExpense }.sumOf { it.amount.amount }
        val totalIncome = filtered.filter { !it.isExpense }.sumOf { it.amount.amount }

        val stats = filtered
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount.amount }
                val isExpense = txs.first().isExpense
                val percent = when {
                    isExpense && totalExpense > 0.0 -> (amount / totalExpense) * 100.0
                    !isExpense && totalIncome > 0.0 -> (amount / totalIncome) * 100.0
                    else -> 0.0
                }
                CategoryStats(
                    category = category,
                    amount = Money(amount, txs.first().amount.currency),
                    percentage = percent,
                    count = txs.size,
                    isExpense = isExpense,
                )
            }
            .sortedByDescending { it.amount.amount }

        return Triple(stats, Money(totalIncome, filtered.firstOrNull()?.amount?.currency ?: Currency.RUB), Money(totalExpense, filtered.firstOrNull()?.amount?.currency ?: Currency.RUB))
    }
}


