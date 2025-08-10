package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.CategoryStats
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction

class CalculateCategoryStatsUseCase {
    operator fun invoke(transactions: List<Transaction>): Triple<List<CategoryStats>, Money, Money> {
        val filtered = transactions.filter { it.category.isNotBlank() }

        val totalExpense = filtered.filter { it.isExpense }.sumOf { it.amount.minor }
        val totalIncome = filtered.filter { !it.isExpense }.sumOf { it.amount.minor }

        val stats = filtered
            .groupBy { it.category }
            .map { (category, txs) ->
                val minor = txs.sumOf { it.amount.minor }
                val isExpense = txs.first().isExpense
                val percent = when {
                    isExpense && totalExpense > 0 -> (minor.toDouble() / totalExpense.toDouble()) * 100.0
                    !isExpense && totalIncome > 0 -> (minor.toDouble() / totalIncome.toDouble()) * 100.0
                    else -> 0.0
                }
                CategoryStats(
                    category = category,
                    amount = Money(minor, txs.first().amount.currency),
                    percentage = percent,
                    count = txs.size,
                    isExpense = isExpense,
                )
            }
            .sortedByDescending { it.amount.minor }

        return Triple(stats, Money(totalIncome), Money(totalExpense))
    }
}


