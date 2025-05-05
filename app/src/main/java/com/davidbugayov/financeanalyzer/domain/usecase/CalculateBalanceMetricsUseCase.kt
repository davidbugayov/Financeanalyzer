package com.davidbugayov.financeanalyzer.domain.usecase

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
            (income.minus(expense).amount.toDouble() / income.amount.toDouble()) * 100
        } else 0.0
        val daysInPeriod = if (startDate != null && endDate != null) {
            ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24) + 1).toInt().coerceAtLeast(1)
        } else 1
        val averageDailyExpense = if (daysInPeriod > 0)
            expense / daysInPeriod.toBigDecimal() else Money.zero()
        val averageMonthlyExpense = averageDailyExpense * 30.toBigDecimal()
        val monthsOfSavings = if (!averageMonthlyExpense.isZero()) {
            income.minus(expense).amount.toDouble() / averageMonthlyExpense.amount.toDouble()
        } else 0.0
        return BalanceMetrics(
            income = income,
            expense = expense,
            savingsRate = savingsRate,
            averageDailyExpense = averageDailyExpense,
            monthsOfSavings = monthsOfSavings
        )
    }
} 