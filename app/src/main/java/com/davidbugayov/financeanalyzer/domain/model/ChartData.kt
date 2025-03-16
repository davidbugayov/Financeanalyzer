package com.davidbugayov.financeanalyzer.domain.model

data class DailyData(
    val income: Money,
    val expense: Money,
    val categoryBreakdown: Map<String, Money>
)
