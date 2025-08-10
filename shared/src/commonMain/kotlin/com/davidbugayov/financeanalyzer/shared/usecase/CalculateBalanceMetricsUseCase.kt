package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * KMP-реализация расчёта метрик баланса без JVM API.
 */
class CalculateBalanceMetricsUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        currency: Currency,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): BalanceMetrics {
        val incomeMinor = transactions.filter { !it.isExpense }.sumOf { it.amount.minor }
        val expenseMinor = transactions.filter { it.isExpense }.sumOf { kotlin.math.abs(it.amount.minor) }
        val balanceMinor = incomeMinor - expenseMinor

        val savingsRate = if (incomeMinor > 0) {
            ((balanceMinor.toDouble() / incomeMinor.toDouble()) * 100.0).coerceAtLeast(0.0)
        } else 0.0

        val daysBetween = if (startDate != null && endDate != null) {
            daysDiff(startDate, endDate)
        } else 0

        val averageDailyExpense = if (daysBetween > 0) {
            Money(expenseMinor / daysBetween, currency)
        } else Money.zero(currency)

        val monthsOfSavings = if (averageDailyExpense.minor > 0) {
            balanceMinor.toDouble() / (averageDailyExpense.minor.toDouble() * 30.0)
        } else 0.0

        return BalanceMetrics(
            income = Money(incomeMinor, currency),
            expense = Money(expenseMinor, currency),
            balance = Money(balanceMinor, currency),
            savingsRate = savingsRate,
            monthsOfSavings = monthsOfSavings,
            averageDailyExpense = averageDailyExpense,
        )
    }
}

private fun daysDiff(start: LocalDate, end: LocalDate): Int =
    start.daysUntil(end).coerceAtLeast(1)


