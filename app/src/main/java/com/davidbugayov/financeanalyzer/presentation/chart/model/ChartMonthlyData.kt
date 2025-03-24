package com.davidbugayov.financeanalyzer.presentation.chart.model

import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Представляет данные о финансах за один день.
 */
data class DailyData(
    val income: Money,
    val expense: Money,
    val categoryBreakdown: Map<String, Money>
)

/**
 * Представляет агрегированные данные о финансах за месяц.
 */
data class ChartMonthlyData(
    val totalIncome: Money,
    val totalExpense: Money,
    val categoryBreakdown: Map<String, Money>
)

/**
 * Конвертирует DailyData в ChartMonthlyData.
 */
fun DailyData.toChartMonthlyData() = ChartMonthlyData(
    totalIncome = income,
    totalExpense = expense,
    categoryBreakdown = categoryBreakdown
) 