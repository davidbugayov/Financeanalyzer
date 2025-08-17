package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.shared.model.Money
import java.util.Date

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
    val dateRange: Pair<Date, Date>? = null,
    val totalWallets: Int = 0
)
