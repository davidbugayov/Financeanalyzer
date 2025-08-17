package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.CategoryStats
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import java.math.BigDecimal

class CalculateCategoryStatsUseCase {
    operator fun invoke(transactions: List<Transaction>): Triple<List<CategoryStats>, Money, Money> {
        val filtered = transactions.filter { it.category.isNotBlank() }

        val totalExpense = filtered.filter { it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        val totalIncome = filtered.filter { !it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }

        val stats = filtered
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
                val isExpense = txs.first().isExpense
                val percent = when {
                    isExpense && totalExpense > BigDecimal.ZERO -> (amount.toDouble() / totalExpense.toDouble()) * 100.0
                    !isExpense && totalIncome > BigDecimal.ZERO -> (amount.toDouble() / totalIncome.toDouble()) * 100.0
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

        return Triple(stats, Money(totalIncome, filtered.firstOrNull()?.amount?.currency ?: com.davidbugayov.financeanalyzer.shared.model.Currency.RUB), Money(totalExpense, filtered.firstOrNull()?.amount?.currency ?: com.davidbugayov.financeanalyzer.shared.model.Currency.RUB))
    }
}


