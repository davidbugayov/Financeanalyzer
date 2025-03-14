package com.davidbugayov.financeanalyzer.presentation.chart.model

import com.davidbugayov.financeanalyzer.domain.model.DailyData
import com.davidbugayov.financeanalyzer.domain.model.Money

data class ChartMonthlyData(
    val totalIncome: Money,
    val totalExpense: Money,
    val categoryBreakdown: Map<String, Money>
)

fun DailyData.toChartMonthlyData() = ChartMonthlyData(
    totalIncome = income,
    totalExpense = expense,
    categoryBreakdown = categoryBreakdown
) 