package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.CategoryStats
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.math.RoundingMode

class CalculateCategoryStatsUseCase {

    operator fun invoke(transactions: List<Transaction>): Triple<List<CategoryStats>, Money, Money> {
        val filteredTransactions = transactions.filter { it.category.isNotBlank() }

        val totalExpense = filteredTransactions
            .filter { it.isExpense }
            .sumOf { it.amount.amount }

        val totalIncome = filteredTransactions
            .filter { !it.isExpense }
            .sumOf { it.amount.amount }

        val stats = filteredTransactions
            .groupBy { it.category }
            .map { (category, txs) ->
                val amount = txs.sumOf { it.amount.amount }
                val percentage = if (txs.first().isExpense && totalExpense > BigDecimal.ZERO) {
                    amount.divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
                } else if (!txs.first().isExpense && totalIncome > BigDecimal.ZERO) {
                    amount.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
                } else {
                    BigDecimal.ZERO
                }

                CategoryStats(
                    category = category,
                    amount = Money(amount),
                    percentage = percentage,
                    count = txs.size,
                    isExpense = txs.first().isExpense,
                )
            }
            .sortedByDescending { it.amount.amount }

        return Triple(stats, Money(totalIncome), Money(totalExpense))
    }
} 
