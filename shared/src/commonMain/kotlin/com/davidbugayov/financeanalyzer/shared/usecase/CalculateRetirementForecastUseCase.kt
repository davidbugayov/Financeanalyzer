package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.RetirementForecast
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlin.math.abs

class CalculateRetirementForecastUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        targetRetirementAge: Int = 65,
        currentAge: Int = 30,
        lifeExpectancy: Int = 85,
    ): RetirementForecast {
        if (transactions.isEmpty()) return RetirementForecast()

        val monthlyIncome = calculateAverageMonthlyIncome(transactions)
        val monthlyExpense = calculateAverageMonthlyExpense(transactions)
        val currentSavings = calculateCurrentSavings(transactions)
        val yearsToRetirement = (targetRetirementAge - currentAge).coerceAtLeast(1)
        val retirementYears = (lifeExpectancy - targetRetirementAge).coerceAtLeast(1)

        val requiredRetirementSavings = monthlyExpense.amount * (12.0 * retirementYears)
        val projectedSavings = calculateProjectedSavings(monthlyIncome, monthlyExpense, yearsToRetirement, currentSavings)
        val savingsGap = requiredRetirementSavings - projectedSavings.amount
        val monthlySavingsNeeded = if (savingsGap > 0.0) {
            savingsGap / (yearsToRetirement * 12.0)
        } else 0.0

        val riskLevel = calculateRiskLevel(projectedSavings.amount, requiredRetirementSavings)
        val recommendations = generateRecommendations(savingsGap, monthlySavingsNeeded, riskLevel)
        val currency = transactions.firstOrNull()?.amount?.currency ?: Currency.RUB

        return RetirementForecast(
            requiredSavings = Money(requiredRetirementSavings, currency),
            projectedSavings = projectedSavings,
            savingsGap = Money(savingsGap, currency),
            monthlySavingsNeeded = Money(monthlySavingsNeeded, currency),
            riskLevel = riskLevel,
            recommendations = recommendations,
            yearsToRetirement = yearsToRetirement,
            retirementYears = retirementYears,
        )
    }

    private fun calculateAverageMonthlyIncome(transactions: List<Transaction>): Money {
        val monthly = transactions.filter { !it.isExpense }
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.amount } }
        if (monthly.isEmpty()) return Money.zero()
        val avg = monthly.values.average()
        return Money(avg, transactions.firstOrNull()?.amount?.currency ?: Currency.RUB)
    }

    private fun calculateAverageMonthlyExpense(transactions: List<Transaction>): Money {
        val monthly = transactions.filter { it.isExpense }
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.sumOf { abs(it.amount.amount) } }
        if (monthly.isEmpty()) return Money.zero()
        val avg = monthly.values.average()
        return Money(avg, transactions.firstOrNull()?.amount?.currency ?: Currency.RUB)
    }

    private fun calculateCurrentSavings(transactions: List<Transaction>): Money {
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount.amount }
        val expense = transactions.filter { it.isExpense }.sumOf { it.amount.amount }
        return Money(income - expense, transactions.firstOrNull()?.amount?.currency ?: Currency.RUB)
    }

    private fun calculateProjectedSavings(
        monthlyIncome: Money,
        monthlyExpense: Money,
        years: Int,
        currentSavings: Money,
    ): Money {
        val monthlySavings = monthlyIncome.amount - monthlyExpense.amount
        val annualSavings = monthlySavings * 12.0
        val totalProjectedSavings = (annualSavings * years) + currentSavings.amount
        return Money(totalProjectedSavings, monthlyIncome.currency)
    }

    private fun calculateRiskLevel(projected: Double, required: Double): String = when {
        projected >= required * 1.2 -> "LOW"
        projected >= required -> "MEDIUM"
        projected >= required * 0.8 -> "HIGH"
        else -> "CRITICAL"
    }

    private fun generateRecommendations(savingsGap: Double, monthlyNeeded: Double, riskLevel: String): List<String> {
        val recommendations = mutableListOf<String>()

        when (riskLevel) {
            "CRITICAL" -> {
                recommendations.add("Увеличьте ежемесячные сбережения на ${String.format("%.2f", monthlyNeeded)}")
                recommendations.add("Рассмотрите возможность дополнительного дохода")
                recommendations.add("Пересмотрите пенсионный возраст")
            }
            "HIGH" -> {
                recommendations.add("Увеличьте сбережения на ${String.format("%.2f", monthlyNeeded)} в месяц")
                recommendations.add("Оптимизируйте расходы")
            }
            "MEDIUM" -> {
                recommendations.add("Продолжайте текущую стратегию сбережений")
                recommendations.add("Рассмотрите инвестиционные возможности")
            }
            "LOW" -> {
                recommendations.add("Отличная работа! Рассмотрите ранний выход на пенсию")
                recommendations.add("Диверсифицируйте инвестиции")
            }
        }

        return recommendations
    }
}


