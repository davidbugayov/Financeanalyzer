package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Универсальный use case для расчёта балансовых метрик по списку транзакций
 */
data class BalanceMetrics(
    val income: Money,
    val expense: Money,
    val savingsRate: Double,
    val averageDailyExpense: Money = Money.zero(),
    val monthsOfSavings: Double = 0.0
)

class CalculateBalanceMetricsUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        startDate: java.util.Date? = null,
        endDate: java.util.Date? = null
    ): BalanceMetrics {
        val income = transactions.filter { !it.isExpense }.fold(Money.zero()) { acc, t -> acc + t.amount }
        val expense = transactions.filter { it.isExpense }.fold(Money.zero()) { acc, t -> acc + t.amount.abs() }
        val savingsRate = if (!income.isZero()) {
            income.minus(expense).amount
                .divide(income.amount, 4, java.math.RoundingMode.HALF_EVEN)
                .multiply(java.math.BigDecimal(100))
        } else java.math.BigDecimal.ZERO
        val daysInPeriod = if (startDate != null && endDate != null) {
            ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24) + 1).toInt().coerceAtLeast(1)
        } else 1
        val averageDailyExpense = if (daysInPeriod > 0)
            expense / daysInPeriod.toBigDecimal() else Money.zero()
        val averageMonthlyExpense = averageDailyExpense * 30.toBigDecimal()
        val monthsOfSavings = if (!averageMonthlyExpense.isZero()) {
            income.minus(expense).amount
                .divide(averageMonthlyExpense.amount, 4, java.math.RoundingMode.HALF_EVEN)
        } else java.math.BigDecimal.ZERO
        return BalanceMetrics(
            income = income,
            expense = expense,
            savingsRate = savingsRate.toDouble(),
            averageDailyExpense = averageDailyExpense,
            monthsOfSavings = monthsOfSavings.toDouble()
        )
    }
} 