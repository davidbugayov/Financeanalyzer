package com.davidbugayov.financeanalyzer.feature.statistics.model

import com.davidbugayov.financeanalyzer.core.model.Money

data class FinancialMetrics(
    val totalTransactions: Int = 0,
    val averageTransactionAmount: Money = Money.zero(),
    val savingsRate: Double = 0.0,
    val topExpenseCategories: List<ExpenseCategory> = emptyList(),
)

data class ExpenseCategory(
    val name: String,
    val amount: Money,
    val percentage: Double,
)
