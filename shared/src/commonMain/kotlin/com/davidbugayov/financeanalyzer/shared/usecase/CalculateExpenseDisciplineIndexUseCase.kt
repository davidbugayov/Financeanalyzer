package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlin.math.max
import kotlin.math.min
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

class CalculateExpenseDisciplineIndexUseCase {
    operator fun invoke(transactions: List<Transaction>, periodMonths: Int = 6): Double {
        if (transactions.isEmpty()) return 0.0
        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return 100.0

        val weeklyData = expenses.groupBy { it.date.weekOfYearKey() }
        val regularityScore = calculateRegularityScore(weeklyData) * 0.30
        val impulsiveScore = calculateImpulsiveControlScore(expenses) * 0.25
        val budgetScore = calculateBudgetAdherenceScore(expenses) * 0.25
        val planningScore = calculatePlanningScore(expenses) * 0.20

        val totalScore = (regularityScore + impulsiveScore + budgetScore + planningScore) * 100
        return max(0.0, min(100.0, totalScore))
    }

    private fun calculateRegularityScore(weeklyData: Map<String, List<Transaction>>): Double {
        if (weeklyData.size < 4) return 0.5
        val weeklyTotals = weeklyData.values.map { txs -> txs.sumOf { it.amount.minor }.toDouble() }
        if (weeklyTotals.isEmpty()) return 1.0
        val avg = weeklyTotals.average()
        if (avg == 0.0) return 1.0
        val variance = weeklyTotals.map { (it - avg) * (it - avg) }.average()
        val std = kotlin.math.sqrt(variance)
        val cv = std / avg
        return when {
            cv <= 0.2 -> 1.0
            cv <= 0.4 -> 0.8
            cv <= 0.6 -> 0.6
            cv <= 0.8 -> 0.4
            else -> 0.2
        }
    }

    private fun calculateImpulsiveControlScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        val amounts = transactions.map { it.amount.minor.toDouble() }
        val avg = amounts.average()
        val median = amounts.sorted().let { if (it.size % 2 == 0) (it[it.size/2 - 1] + it[it.size/2]) / 2 else it[it.size/2] }
        val threshold = median * 3
        val impulsives = amounts.filter { it > threshold }
        val ratio = impulsives.size.toDouble() / amounts.size
        val impulsiveAmountRatio = if (amounts.sum() > 0) impulsives.sum() / amounts.sum() else 0.0
        val penalty = ratio * 0.5 + impulsiveAmountRatio * 0.5
        return max(0.0, 1.0 - penalty * 2.0)
    }

    private fun calculateBudgetAdherenceScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        val categoryTotals = transactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount.minor }.toDouble() }
        if (categoryTotals.isEmpty()) return 1.0
        val total = categoryTotals.values.sum()
        if (total == 0.0) return 1.0
        val ratios = categoryTotals.values.map { it / total }.sortedDescending()
        val hhi = ratios.sumOf { it * it }
        val dominant = ratios.count { it > 0.4 }
        val concentrationPenalty = when {
            hhi <= 0.2 -> 0.0
            hhi <= 0.3 -> 0.1
            hhi <= 0.5 -> 0.3
            else -> 0.5
        }
        val dominancePenalty = dominant * 0.2
        return max(0.0, 1.0 - concentrationPenalty - dominancePenalty)
    }

    private fun calculatePlanningScore(transactions: List<Transaction>): Double {
        if (transactions.isEmpty()) return 1.0
        val amounts = transactions.map { it.amount.minor.toDouble() }
        val median = amounts.sorted().let { if (it.size % 2 == 0) (it[it.size/2 - 1] + it[it.size/2]) / 2 else it[it.size/2] }
        val largeThreshold = median * 5
        val large = transactions.filter { it.amount.minor.toDouble() > largeThreshold }
        if (large.isEmpty()) return 1.0
        val byWeek = large.groupBy { it.date.weekOfYearKey() }
        val weeksWithMultiple = byWeek.values.count { it.size > 1 }
        val clustering = if (byWeek.isNotEmpty()) weeksWithMultiple.toDouble() / byWeek.size else 0.0
        return max(0.0, 1.0 - clustering)
    }
}

private fun kotlinx.datetime.LocalDate.weekOfYearKey(): String {
    // Approximate: use ISO week by counting from first day of year
    val first = kotlinx.datetime.LocalDate(this.year, 1, 1)
    var d = first
    var week = 1
    while (d <= this) {
        d = d + DatePeriod(days = 7)
        week++
    }
    return "${this.year}-W$week"
}


