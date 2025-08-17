package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.shared.model.Money

data class BalanceMetrics(
    val income: Money,
    val expense: Money,
    val balance: Money,
    val savingsRate: Double = 0.0,
    val monthsOfSavings: Double = 0.0,
    val averageDailyExpense: Money = Money.zero(),
)
