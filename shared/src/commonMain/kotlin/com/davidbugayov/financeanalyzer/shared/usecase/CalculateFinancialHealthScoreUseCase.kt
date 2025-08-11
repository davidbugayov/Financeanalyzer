package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.HealthScoreBreakdown
import com.davidbugayov.financeanalyzer.shared.model.Transaction
import kotlinx.datetime.LocalDate
import kotlin.math.max
import kotlin.math.min

/**
 * Вычисляет коэффициент финансового здоровья и его декомпозицию.
 *
 * Правила близки к доменной реализации, адаптированы под KMP-модель `Transaction`.
 * Возвращает итоговый балл (0..100) и структуру `HealthScoreBreakdown`.
 */
class CalculateFinancialHealthScoreUseCase {

    /**
     * Рассчитывает итоговый балл и декомпозицию.
     *
     * @param transactions Список транзакций.
     * @param periodMonths Период анализа в месяцах (резерв на будущее, пока не используется явно).
     */
    operator fun invoke(
        transactions: List<Transaction>,
        periodMonths: Int = 6,
    ): Pair<Double, HealthScoreBreakdown> {
        if (transactions.isEmpty()) return 0.0 to HealthScoreBreakdown()

        val monthlyData = groupByMonth(transactions)

        val savingsScore = calculateSavingsRateScore(monthlyData)
        val stabilityScore = calculateIncomeStabilityScore(monthlyData)
        val controlScore = calculateExpenseControlScore(monthlyData)
        val diversificationScore = calculateDiversificationScore(transactions)

        val total = savingsScore + stabilityScore + controlScore + diversificationScore
        val breakdown = HealthScoreBreakdown(
            savingsRateScore = savingsScore,
            incomeStabilityScore = stabilityScore,
            expenseControlScore = controlScore,
            diversificationScore = diversificationScore,
        )
        return total to breakdown
    }

    private fun groupByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions.groupBy { tx ->
            val d: LocalDate = tx.date
            "${d.year}-${d.monthNumber}"
        }
    }

    private fun calculateSavingsRateScore(monthly: Map<String, List<Transaction>>): Double {
        if (monthly.isEmpty()) return 0.0

        val monthlyRates = monthly.map { (_, list) ->
            val income = list.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() }
            val expense = list.filter { it.isExpense }.sumOf { it.amount.toMajorDouble() }
            if (income > 0.0) max(0.0, (income - expense) / income) else 0.0
        }
        val avg = if (monthlyRates.isNotEmpty()) monthlyRates.average() else 0.0
        return when {
            avg >= 0.20 -> 25.0
            avg >= 0.10 -> 10.0 + (avg - 0.10) * 150.0
            avg >= 0.05 -> 5.0 + (avg - 0.05) * 100.0
            else -> avg * 100.0
        }
    }

    private fun calculateIncomeStabilityScore(monthly: Map<String, List<Transaction>>): Double {
        if (monthly.size < 2) return 12.5
        val incomes = monthly.map { (_, list) ->
            list.filter { !it.isExpense }.sumOf { it.amount.toMajorDouble() }
        }
        if (incomes.isEmpty() || incomes.all { it == 0.0 }) return 0.0
        val avg = incomes.average()
        if (avg == 0.0) return 0.0
        val variance = incomes.map { (it - avg) * (it - avg) }.average()
        val std = kotlin.math.sqrt(variance)
        val cv = std / avg
        return when {
            cv <= 0.1 -> 20.0 + (0.1 - cv) * 50.0
            cv <= 0.2 -> 15.0 + (0.2 - cv) * 50.0
            cv <= 0.4 -> 10.0 + (0.4 - cv) * 25.0
            else -> max(0.0, 10.0 - (cv - 0.4) * 20.0)
        }
    }

    private fun calculateExpenseControlScore(monthly: Map<String, List<Transaction>>): Double {
        if (monthly.size < 2) return 12.5
        val expenses = monthly.map { (_, list) ->
            list.filter { it.isExpense }.sumOf { it.amount.toMajorDouble() }
        }
        if (expenses.isEmpty() || expenses.all { it == 0.0 }) return 25.0
        val avg = expenses.average()
        if (avg == 0.0) return 25.0
        val variance = expenses.map { (it - avg) * (it - avg) }.average()
        val std = kotlin.math.sqrt(variance)
        val cv = std / avg
        val maxExpense = expenses.maxOrNull() ?: 0.0
        val minExpense = expenses.minOrNull() ?: 0.0
        val extremeRatio = if (minExpense > 0.0) maxExpense / minExpense else 10.0

        val stabilityScore = when {
            cv <= 0.15 -> 15.0
            cv <= 0.25 -> 12.0
            cv <= 0.4 -> 8.0
            else -> 4.0
        }
        val extremePenalty = when {
            extremeRatio <= 2.0 -> 0.0
            extremeRatio <= 3.0 -> 2.0
            extremeRatio <= 5.0 -> 5.0
            else -> 10.0
        }
        return max(0.0, 10.0 + stabilityScore - extremePenalty)
    }

    private fun calculateDiversificationScore(transactions: List<Transaction>): Double {
        val incomeSources = transactions.filter { !it.isExpense }.map { it.source }.distinct().size
        val incomeCategories = transactions.filter { !it.isExpense }.map { it.category }.distinct().size
        val expenseCategories = transactions.filter { it.isExpense }.map { it.category }.distinct().size

        val incomeSourceScore = min(10.0, incomeSources * 2.5)
        val incomeCategoryScore = min(5.0, incomeCategories * 1.25)
        val expenseCategoryScore = when {
            expenseCategories >= 8 -> 10.0
            expenseCategories >= 6 -> 8.0
            expenseCategories >= 4 -> 6.0
            expenseCategories >= 2 -> 3.0
            else -> 1.0
        }
        return incomeSourceScore + incomeCategoryScore + expenseCategoryScore
    }
}


