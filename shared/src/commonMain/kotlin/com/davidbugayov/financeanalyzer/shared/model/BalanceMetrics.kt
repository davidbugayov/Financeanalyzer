package com.davidbugayov.financeanalyzer.shared.model

/**
 * Метрики баланса в KMP-слое.
 */
data class BalanceMetrics(
    val income: Money,
    val expense: Money,
    val balance: Money,
    val savingsRate: Double = 0.0,
    val monthsOfSavings: Double = 0.0,
    val averageDailyExpense: Money = Money.zero(),
)


