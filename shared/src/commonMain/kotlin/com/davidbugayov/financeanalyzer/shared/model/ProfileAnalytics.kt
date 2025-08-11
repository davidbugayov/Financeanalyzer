package com.davidbugayov.financeanalyzer.shared.model

import kotlinx.datetime.LocalDate

/**
 * Профильная аналитика в KMP-формате.
 */
data class ProfileAnalytics(
    val totalIncome: Money = Money.zero(),
    val totalExpense: Money = Money.zero(),
    val balance: Money = Money.zero(),
    val savingsRate: Double = 0.0,
    val totalTransactions: Int = 0,
    val totalExpenseCategories: Int = 0,
    val totalIncomeCategories: Int = 0,
    val averageExpense: Money = Money.zero(),
    val totalSourcesUsed: Int = 0,
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val totalWallets: Int = 0,
)


