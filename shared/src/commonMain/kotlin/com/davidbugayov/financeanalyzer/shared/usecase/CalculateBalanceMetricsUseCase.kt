package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import java.math.BigDecimal

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
        val incomeAmount = transactions.filter { !it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        val expenseAmount = transactions.filter { it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount.abs()) }
        val balanceAmount = incomeAmount.subtract(expenseAmount)

        val savingsRate = if (incomeAmount > BigDecimal.ZERO) {
            ((balanceAmount.toDouble() / incomeAmount.toDouble()) * 100.0).coerceAtLeast(0.0)
        } else 0.0

        val daysBetween = if (startDate != null && endDate != null) {
            daysDiff(startDate, endDate)
        } else 0

        val averageDailyExpense = if (daysBetween > 0) {
            Money(expenseAmount.divide(BigDecimal.valueOf(daysBetween.toDouble()), 10, java.math.RoundingMode.HALF_EVEN), currency)
        } else Money.zero(currency)

        val monthsOfSavings = if (averageDailyExpense.amount > BigDecimal.ZERO) {
            balanceAmount.toDouble() / (averageDailyExpense.amount.toDouble() * 30.0)
        } else 0.0

        return BalanceMetrics(
            income = Money(incomeAmount, currency),
            expense = Money(expenseAmount, currency),
            balance = Money(balanceAmount, currency),
            savingsRate = savingsRate,
            monthsOfSavings = monthsOfSavings,
            averageDailyExpense = averageDailyExpense,
        )
    }
}

private fun daysDiff(start: LocalDate, end: LocalDate): Int =
    start.daysUntil(end).coerceAtLeast(1)


