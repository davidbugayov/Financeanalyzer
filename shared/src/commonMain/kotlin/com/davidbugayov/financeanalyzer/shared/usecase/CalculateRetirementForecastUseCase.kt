package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.RetirementForecast
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

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

        val requiredRetirementSavings = monthlyExpense.amount.multiply(BigDecimal.valueOf(12.0 * retirementYears))
        val projectedSavings = calculateProjectedSavings(monthlyIncome, monthlyExpense, yearsToRetirement, currentSavings)
        val savingsGap = requiredRetirementSavings.subtract(projectedSavings.amount)
        val monthlySavingsNeeded = if (savingsGap > BigDecimal.ZERO) {
            savingsGap.divide(BigDecimal.valueOf(yearsToRetirement * 12.0), 10, java.math.RoundingMode.HALF_EVEN)
        } else BigDecimal.ZERO

        val riskLevel = calculateRiskLevel(projectedSavings.amount, requiredRetirementSavings)
        val recommendations = generateRecommendations(savingsGap, monthlySavingsNeeded, riskLevel)

        return RetirementForecast(
            requiredSavings = Money(requiredRetirementSavings),
            projectedSavings = projectedSavings,
            savingsGap = Money(savingsGap),
            monthlySavingsNeeded = Money(monthlySavingsNeeded),
            riskLevel = riskLevel,
            recommendations = recommendations,
            yearsToRetirement = yearsToRetirement,
            retirementYears = retirementYears,
        )
    }

    private fun calculateAverageMonthlyIncome(transactions: List<Transaction>): Money {
        val monthly = transactions.filter { !it.isExpense }
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) } }
        if (monthly.isEmpty()) return Money.zero()
        val avg = monthly.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }.divide(BigDecimal.valueOf(monthly.size.toDouble()), 10, java.math.RoundingMode.HALF_EVEN)
        return Money(avg)
    }

    private fun calculateAverageMonthlyExpense(transactions: List<Transaction>): Money {
        val monthly = transactions.filter { it.isExpense }
            .groupBy { t -> "${t.date.year}-${t.date.month}" }
            .mapValues { (_, txs) -> txs.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) } }
        if (monthly.isEmpty()) return Money.zero()
        val avg = monthly.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }.divide(BigDecimal.valueOf(monthly.size.toDouble()), 10, java.math.RoundingMode.HALF_EVEN)
        return Money(avg)
    }

    private fun calculateCurrentSavings(transactions: List<Transaction>): Money {
        val income = transactions.filter { !it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        val expense = transactions.filter { it.isExpense }.fold(BigDecimal.ZERO) { acc, transaction -> acc.add(transaction.amount.amount) }
        return Money(income.subtract(expense))
    }

    private fun calculateProjectedSavings(
        monthlyIncome: Money,
        monthlyExpense: Money,
        years: Int,
        currentSavings: Money,
    ): Money {
        val monthlySavings = monthlyIncome.amount.subtract(monthlyExpense.amount)
        val annualSavings = monthlySavings.multiply(BigDecimal.valueOf(12.0))
        val totalProjectedSavings = annualSavings.multiply(BigDecimal.valueOf(years.toDouble())).add(currentSavings.amount)
        return Money(totalProjectedSavings)
    }

    private fun calculateRiskLevel(projected: BigDecimal, required: BigDecimal): String = when {
        projected >= required.multiply(BigDecimal.valueOf(1.2)) -> "LOW"
        projected >= required -> "MEDIUM"
        projected >= required.multiply(BigDecimal.valueOf(0.8)) -> "HIGH"
        else -> "CRITICAL"
    }

    private fun generateRecommendations(savingsGap: BigDecimal, monthlyNeeded: BigDecimal, riskLevel: String): List<String> {
        val recommendations = mutableListOf<String>()
        
        when (riskLevel) {
            "CRITICAL" -> {
                recommendations.add("Увеличьте ежемесячные сбережения на ${monthlyNeeded.toPlainString()}")
                recommendations.add("Рассмотрите возможность дополнительного дохода")
                recommendations.add("Пересмотрите пенсионный возраст")
            }
            "HIGH" -> {
                recommendations.add("Увеличьте сбережения на ${monthlyNeeded.toPlainString()} в месяц")
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


