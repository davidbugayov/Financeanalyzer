package com.davidbugayov.financeanalyzer.domain.usecase.analytics

import com.davidbugayov.financeanalyzer.domain.model.BalanceMetrics
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.util.Date

class CalculateBalanceMetricsUseCase {

    operator fun invoke(transactions: List<Transaction>, startDate: Date? = null, endDate: Date? = null): BalanceMetrics {
        val income = transactions
            .filter { !it.isExpense }
            .sumOf { it.amount.amount }

        val expense = transactions
            .filter { it.isExpense }
            .sumOf { it.amount.amount }

        val balance = income - expense

        // Дополнительные метрики, которые могут использовать даты
        val savingsRate = if (income > BigDecimal.ZERO) {
            (income - expense).divide(income, 4, java.math.RoundingMode.HALF_UP).toDouble()
        } else {
            0.0
        }

        val averageDailyExpense = if (startDate != null && endDate != null) {
            val daysBetween = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
            Money(expense.divide(BigDecimal(daysBetween), 2, java.math.RoundingMode.HALF_UP))
        } else {
            Money(BigDecimal.ZERO)
        }

        val monthsOfSavings = if (averageDailyExpense.amount > BigDecimal.ZERO) {
            balance.divide(
                averageDailyExpense.amount.multiply(BigDecimal(30)),
                2,
                java.math.RoundingMode.HALF_UP,
            ).toDouble()
        } else {
            0.0
        }

        // Создаем Money объекты с правильным масштабированием
        val incomeWithScale = income.setScale(2, java.math.RoundingMode.HALF_UP)
        val expenseWithScale = expense.setScale(2, java.math.RoundingMode.HALF_UP)
        val balanceWithScale = balance.setScale(2, java.math.RoundingMode.HALF_UP)

        return BalanceMetrics(
            income = Money(incomeWithScale),
            expense = Money(expenseWithScale),
            balance = Money(balanceWithScale),
            savingsRate = savingsRate,
            monthsOfSavings = monthsOfSavings,
            averageDailyExpense = averageDailyExpense,
        )
    }
}
