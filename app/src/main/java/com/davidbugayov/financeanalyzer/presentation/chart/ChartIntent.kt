package com.davidbugayov.financeanalyzer.presentation.chart

import java.util.Date

sealed interface ChartIntent {
    data class UpdateStartDate(val date: Date) : ChartIntent
    data class UpdateEndDate(val date: Date) : ChartIntent
    data class UpdateDateRange(val startDate: Date, val endDate: Date) : ChartIntent
    data class ToggleExpenseView(val showExpenses: Boolean) : ChartIntent
    object LoadTransactions : ChartIntent
} 