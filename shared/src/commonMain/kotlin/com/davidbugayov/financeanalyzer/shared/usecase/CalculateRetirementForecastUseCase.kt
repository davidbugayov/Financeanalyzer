package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.shared.model.RetirementForecast
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class CalculateRetirementForecastUseCase {
    operator fun invoke(
        transactions: List<Transaction>,
        currentAge: Int = 30,
        retirementAge: Int = 65,
        currentSavings: Money = Money.zero(),
        desiredMonthlyPension: Money? = null,
    ): RetirementForecast {
        val yearsToRetirement = max(0, retirementAge - currentAge)
        if (yearsToRetirement == 0) return createImmediateRetirementForecast(currentSavings)

        val averageMonthlySavings = calculateAverageMonthlySavings(transactions)

        val recommendedRetirementAmount = calculateRecommendedRetirementAmount(desiredMonthlyPension, transactions)

        val projectedAmount = calculateProjectedSavings(
            currentSavings = currentSavings,
            monthlySavings = averageMonthlySavings,
            years = yearsToRetirement,
            annualReturn = 0.07,
            inflationRate = 0.03,
        )

        val goalProgress = if (recommendedRetirementAmount.minor > 0) {
            (projectedAmount.minor.toDouble() / recommendedRetirementAmount.minor.toDouble()) * 100
        } else 100.0

        val requiredMonthlySavings = calculateRequiredMonthlySavings(
            currentSavings = currentSavings,
            targetAmount = recommendedRetirementAmount,
            years = yearsToRetirement,
            annualReturn = 0.07,
        )

        val yearsToGoal = calculateYearsToGoal(
            currentSavings = currentSavings,
            monthlySavings = averageMonthlySavings,
            targetAmount = recommendedRetirementAmount,
            annualReturn = 0.07,
        )

        return RetirementForecast(
            retirementAge = retirementAge,
            currentSavings = currentSavings,
            recommendedRetirementAmount = recommendedRetirementAmount,
            projectedRetirementAmount = projectedAmount,
            retirementGoalProgress = max(0.0, min(200.0, goalProgress)),
            requiredMonthlySavings = requiredMonthlySavings,
            yearsToGoal = yearsToGoal,
        )
    }

    private fun createImmediateRetirementForecast(currentSavings: Money): RetirementForecast =
        RetirementForecast(
            retirementAge = 65,
            currentSavings = currentSavings,
            recommendedRetirementAmount = currentSavings,
            projectedRetirementAmount = currentSavings,
            retirementGoalProgress = 100.0,
            requiredMonthlySavings = Money.zero(),
            yearsToGoal = 0.0,
        )

    private fun calculateAverageMonthlySavings(transactions: List<Transaction>): Money {
        if (transactions.isEmpty()) return Money.zero()
        val byMonth = transactions.groupBy { t -> "${t.date.year}-${t.date.month}" }
        val monthlySavings = byMonth.values.map { monthTxs ->
            val income = monthTxs.filter { !it.isExpense }.sumOf { it.amount.minor }
            val expense = monthTxs.filter { it.isExpense }.sumOf { it.amount.minor }
            (income - expense).coerceAtLeast(0L)
        }
        return if (monthlySavings.isNotEmpty()) Money(monthlySavings.sum() / monthlySavings.size) else Money.zero()
    }

    private fun calculateRecommendedRetirementAmount(desiredMonthlyPension: Money?, transactions: List<Transaction>): Money {
        desiredMonthlyPension?.let { monthly ->
            val annual = monthly.minor * 12
            val total = annual * 25
            return Money(total)
        }
        val averageMonthlyExpense = calculateAverageMonthlyExpense(transactions)
        val recommendedMonthly = (averageMonthlyExpense.minor * 80) / 100
        val annual = recommendedMonthly * 12
        val total = annual * 25
        return Money(total)
    }

    private fun calculateAverageMonthlyExpense(transactions: List<Transaction>): Money {
        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return Money(50_000_00L)
        val byMonth = expenses.groupBy { t -> "${t.date.year}-${t.date.month}" }
        val monthly = byMonth.values.map { txs -> txs.sumOf { it.amount.minor } }
        return if (monthly.isNotEmpty()) Money(monthly.sum() / monthly.size) else Money(50_000_00L)
    }

    private fun calculateProjectedSavings(
        currentSavings: Money,
        monthlySavings: Money,
        years: Int,
        annualReturn: Double,
        inflationRate: Double,
    ): Money {
        val realReturn = (1 + annualReturn) / (1 + inflationRate) - 1
        val monthlyReturn = realReturn / 12
        val totalMonths = years * 12
        val futureCurrent = currentSavings.minor.toDouble() * (1 + realReturn).pow(years)
        val futureMonthly = if (monthlyReturn > 0) {
            monthlySavings.minor.toDouble() * (((1 + monthlyReturn).pow(totalMonths) - 1) / monthlyReturn)
        } else monthlySavings.minor.toDouble() * totalMonths
        return Money(futureCurrent.plus(futureMonthly).toLong())
    }

    private fun calculateRequiredMonthlySavings(
        currentSavings: Money,
        targetAmount: Money,
        years: Int,
        annualReturn: Double,
    ): Money {
        if (years <= 0) return Money.zero()
        val monthlyReturn = annualReturn / 12
        val totalMonths = years * 12
        val futureCurrent = currentSavings.minor.toDouble() * (1 + annualReturn).pow(years)
        val additionalNeeded = targetAmount.minor.toDouble() - futureCurrent
        if (additionalNeeded <= 0) return Money.zero()
        val required = if (monthlyReturn > 0) {
            additionalNeeded * monthlyReturn / ((1 + monthlyReturn).pow(totalMonths) - 1)
        } else additionalNeeded / totalMonths
        return Money(kotlin.math.max(0.0, required).toLong())
    }

    private fun calculateYearsToGoal(
        currentSavings: Money,
        monthlySavings: Money,
        targetAmount: Money,
        annualReturn: Double,
    ): Double {
        if (targetAmount.minor <= currentSavings.minor) return 0.0
        if (monthlySavings.minor <= 0L) return Double.POSITIVE_INFINITY
        val monthlyReturn = annualReturn / 12
        val target = targetAmount.minor.toDouble()
        val current = currentSavings.minor.toDouble()
        val monthly = monthlySavings.minor.toDouble()
        if (monthlyReturn > 0) {
            var months = 1.0
            var value = current
            while (value < target && months < 12 * 100) {
                value = current * (1 + monthlyReturn).pow(months) +
                    monthly * (((1 + monthlyReturn).pow(months) - 1) / monthlyReturn)
                if (value >= target) break
                months += 1
            }
            return if (months >= 1200) Double.POSITIVE_INFINITY else months / 12
        } else {
            val monthsNeeded = (target - current) / monthly
            return if (monthsNeeded > 0) monthsNeeded / 12 else Double.POSITIVE_INFINITY
        }
    }
}


