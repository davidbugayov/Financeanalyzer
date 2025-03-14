package com.davidbugayov.financeanalyzer.domain.model

data class ChartData(
    val categoryData: Map<String, Money>,
    val dailyData: Map<String, DailyData>
)

data class DailyData(
    val income: Money,
    val expense: Money,
    val categoryBreakdown: Map<String, Money>
)

data class CategoryStats(
    val category: String,
    val amount: Money,
    val percentage: Double
) 