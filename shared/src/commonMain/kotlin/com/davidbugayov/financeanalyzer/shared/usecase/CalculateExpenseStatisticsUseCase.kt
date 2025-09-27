package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Currency
import com.davidbugayov.financeanalyzer.shared.model.Transaction

/**
 * Данные о месячных расходах
 */
data class MonthlyExpenseData(
    val month: Int,
    val year: Int,
    val amount: Double,
    val monthIndex: Int,
)

/**
 * Use case для расчета статистики расходов
 *
 * Особенности:
 * - Анализирует расходы за последние 12 месяцев
 * - Рассчитывает средние, максимальные и минимальные значения
 * - Показывает тренд расходов
 * - Группирует по категориям
 */
class CalculateExpenseStatisticsUseCase {

    operator fun invoke(transactions: List<Transaction>): ExpenseStatistics {
        val expenses = transactions.filter { it.isExpense }
        if (expenses.isEmpty()) return ExpenseStatistics.empty()

        val monthlyData = prepareMonthlyData(expenses)
        val categoryData = prepareCategoryData(expenses)

        return ExpenseStatistics(
            totalExpenses = calculateTotalExpenses(expenses),
            averageMonthly = calculateAverageMonthly(monthlyData),
            maxMonthly = calculateMaxMonthly(monthlyData),
            minMonthly = calculateMinMonthly(monthlyData),
            trendDirection = calculateTrend(monthlyData),
            topCategories = getTopCategories(categoryData, 5),
            monthlyBreakdown = monthlyData,
            currency = expenses.firstOrNull()?.amount?.currency ?: Currency.RUB,
        )
    }

    /**
     * Подготавливает месячные данные для анализа
     */
    private fun prepareMonthlyData(expenses: List<Transaction>): List<MonthlyExpenseData> {
        val monthlyExpenses = expenses
            .groupBy { transaction ->
                "${transaction.date.year}-${transaction.date.monthNumber.toString().padStart(2, '0')}"
            }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount.amount }
            }
            .toList()
            .sortedBy { it.first }
            .takeLast(12)

        return monthlyExpenses.mapIndexed { index, (monthKey, amount) ->
            val parts = monthKey.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()

            MonthlyExpenseData(
                month = month,
                year = year,
                amount = amount,
                monthIndex = index,
            )
        }
    }

    /**
     * Подготавливает данные по категориям
     */
    private fun prepareCategoryData(expenses: List<Transaction>): Map<String, Double> {
        return expenses
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount.amount }
            }
    }

    /**
     * Рассчитывает общую сумму расходов
     */
    private fun calculateTotalExpenses(expenses: List<Transaction>): Double {
        return expenses.sumOf { it.amount.amount }
    }

    /**
     * Рассчитывает средние месячные расходы
     */
    private fun calculateAverageMonthly(monthlyData: List<MonthlyExpenseData>): Double {
        if (monthlyData.isEmpty()) return 0.0
        return monthlyData.map { it.amount }.average()
    }

    /**
     * Рассчитывает максимальные месячные расходы
     */
    private fun calculateMaxMonthly(monthlyData: List<MonthlyExpenseData>): Double {
        if (monthlyData.isEmpty()) return 0.0
        return monthlyData.maxOfOrNull { it.amount } ?: 0.0
    }

    /**
     * Рассчитывает минимальные месячные расходы
     */
    private fun calculateMinMonthly(monthlyData: List<MonthlyExpenseData>): Double {
        if (monthlyData.isEmpty()) return 0.0
        return monthlyData.minOfOrNull { it.amount } ?: 0.0
    }

    /**
     * Рассчитывает тренд расходов
     */
    private fun calculateTrend(monthlyData: List<MonthlyExpenseData>): TrendDirection {
        if (monthlyData.size < 2) return TrendDirection.STABLE

        val firstHalf = monthlyData.take(monthlyData.size / 2)
        val secondHalf = monthlyData.drop(monthlyData.size / 2)

        val firstAverage = firstHalf.map { it.amount }.average()
        val secondAverage = secondHalf.map { it.amount }.average()

        val changePercent = ((secondAverage - firstAverage) / firstAverage) * 100

        return when {
            changePercent > 10 -> TrendDirection.INCREASING
            changePercent < -10 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }
    }

    /**
     * Получает топ категории по расходам
     */
    private fun getTopCategories(categoryData: Map<String, Double>, limit: Int): List<CategoryStatistic> {
        return categoryData
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { (category, amount) ->
                CategoryStatistic(
                    category = category,
                    amount = amount,
                    percentage = (amount / categoryData.values.sum()) * 100,
                )
            }
    }
}

/**
 * Статистика расходов
 */
data class ExpenseStatistics(
    val totalExpenses: Double,
    val averageMonthly: Double,
    val maxMonthly: Double,
    val minMonthly: Double,
    val trendDirection: TrendDirection,
    val topCategories: List<CategoryStatistic>,
    val monthlyBreakdown: List<MonthlyExpenseData>,
    val currency: Currency,
) {

    companion object {

        fun empty() = ExpenseStatistics(
            totalExpenses = 0.0,
            averageMonthly = 0.0,
            maxMonthly = 0.0,
            minMonthly = 0.0,
            trendDirection = TrendDirection.STABLE,
            topCategories = emptyList(),
            monthlyBreakdown = emptyList(),
            currency = Currency.RUB,
        )
    }
}

/**
 * Направление тренда
 */
enum class TrendDirection {

    INCREASING, DECREASING, STABLE
}

/**
 * Статистика по категории
 */
data class CategoryStatistic(
    val category: String,
    val amount: Double,
    val percentage: Double,
)
