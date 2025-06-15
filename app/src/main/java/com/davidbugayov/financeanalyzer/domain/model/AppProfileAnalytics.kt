package com.davidbugayov.financeanalyzer.domain.model

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.ProfileAnalytics
import java.util.Date

/**
 * Адаптер для ProfileAnalytics из domain модуля
 * Используется для совместимости с существующим кодом
 */
class AppProfileAnalytics(
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
) {
    companion object {
        fun fromDomainModel(profileAnalytics: ProfileAnalytics): AppProfileAnalytics {
            return AppProfileAnalytics(
                totalIncome = profileAnalytics.totalIncome,
                totalExpense = profileAnalytics.totalExpense,
                balance = profileAnalytics.balance,
                savingsRate = profileAnalytics.savingsRate,
                totalTransactions = profileAnalytics.totalTransactions,
                totalExpenseCategories = profileAnalytics.totalExpenseCategories,
                totalIncomeCategories = profileAnalytics.totalIncomeCategories,
                averageExpense = profileAnalytics.averageExpense,
                totalSourcesUsed = profileAnalytics.totalSourcesUsed,
                dateRange = profileAnalytics.dateRange,
                totalWallets = profileAnalytics.totalWallets
            )
        }
    }
} 