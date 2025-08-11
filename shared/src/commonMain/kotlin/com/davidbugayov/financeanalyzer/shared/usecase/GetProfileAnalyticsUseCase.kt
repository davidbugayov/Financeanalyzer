package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.ProfileAnalytics
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Собирает профильную аналитику по переданному списку транзакций.
 * Источники/кошельки не подтягиваем — totalWallets передаётся параметром при необходимости.
 */
class GetProfileAnalyticsUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        currency: Currency,
        totalWallets: Int = 0,
    ): ProfileAnalytics {
        var totalIncome = Money.zero(currency)
        var totalExpense = Money.zero(currency)

        val incomeCategories = mutableSetOf<String>()
        val expenseCategories = mutableSetOf<String>()
        val sources = mutableSetOf<String>()

        transactions.forEach { tx ->
            if (!tx.isExpense) {
                totalIncome = totalIncome + Money(tx.amount.minor, currency)
                incomeCategories.add(tx.category)
            } else {
                totalExpense = totalExpense + tx.amount.abs()
                expenseCategories.add(tx.category)
            }
            sources.add(tx.source)
        }

        val balance = totalIncome - totalExpense
        val savingsRate = if (!totalIncome.isZero()) {
            balance.toMajorDouble() / totalIncome.toMajorDouble() * 100.0
        } else 0.0

        val averageExpense = if (expenseCategories.isNotEmpty()) {
            // Грубая средняя по категориям
            Money((totalExpense.minor / expenseCategories.size.toLong()), currency)
        } else Money.zero(currency)

        val maxDate: LocalDate? = transactions.maxByOrNull { it.date }?.date
        val today: LocalDate? = maxDate
        val yearAgo: LocalDate? = today?.minus(DatePeriod(years = 1))

        return ProfileAnalytics(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            savingsRate = savingsRate,
            totalTransactions = transactions.size,
            totalExpenseCategories = expenseCategories.size,
            totalIncomeCategories = incomeCategories.size,
            averageExpense = averageExpense,
            totalSourcesUsed = sources.size,
            dateRange = if (today != null && yearAgo != null) yearAgo to today else null,
            totalWallets = totalWallets,
        )
    }
}


