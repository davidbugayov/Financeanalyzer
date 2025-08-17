package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.ProfileAnalytics
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import java.math.BigDecimal

class GetProfileAnalyticsUseCase {
    operator fun invoke(transactions: List<Transaction>): ProfileAnalytics {
        if (transactions.isEmpty()) return ProfileAnalytics()

        val totalIncome = transactions.filter { !it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        val totalExpense = transactions.filter { it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        val netWorth = totalIncome.subtract(totalExpense)

        val averageTransactionAmount = transactions.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }.divide(BigDecimal.valueOf(transactions.size.toDouble()), 10, java.math.RoundingMode.HALF_EVEN)

        val savingsRate = if (totalIncome > BigDecimal.ZERO) {
            ((netWorth.toDouble() / totalIncome.toDouble()) * 100.0).coerceAtLeast(0.0)
        } else 0.0

        val expenseToIncomeRatio = if (totalIncome > BigDecimal.ZERO) {
            (totalExpense.toDouble() / totalIncome.toDouble()) * 100.0
        } else 0.0

        return ProfileAnalytics(
            totalIncome = Money(totalIncome),
            totalExpense = Money(totalExpense),
            netWorth = Money(netWorth),
            averageTransactionAmount = Money(averageTransactionAmount),
            savingsRate = savingsRate,
            expenseToIncomeRatio = expenseToIncomeRatio,
            totalTransactions = transactions.size,
        )
    }
}


