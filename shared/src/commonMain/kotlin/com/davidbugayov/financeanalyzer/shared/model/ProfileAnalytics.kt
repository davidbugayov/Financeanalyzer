package com.davidbugayov.financeanalyzer.shared.model

/**
 * Профильная аналитика в KMP-формате.
 */
data class ProfileAnalytics(
    val totalIncome: Money = Money.zero(),
    val totalExpense: Money = Money.zero(),
    val netWorth: Money = Money.zero(),
    val averageTransactionAmount: Money = Money.zero(),
    val savingsRate: Double = 0.0,
    val expenseToIncomeRatio: Double = 0.0,
    val totalTransactions: Int = 0,
)


