package com.davidbugayov.financeanalyzer.presentation.chart.state

import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.util.Date

sealed class ChartIntent {
    object LoadTransactions : ChartIntent()
    data class UpdateStartDate(val date: Date) : ChartIntent()
    data class UpdateEndDate(val date: Date) : ChartIntent()
    data class UpdateDateRange(val startDate: Date, val endDate: Date) : ChartIntent()
    data class ToggleExpenseView(val showExpenses: Boolean) : ChartIntent()
    data class SetPeriodType(val periodType: PeriodType) : ChartIntent()
    object ShowPeriodDialog : ChartIntent()
    object HidePeriodDialog : ChartIntent()
    object ShowStartDatePicker : ChartIntent()
    object HideStartDatePicker : ChartIntent()
    object ShowEndDatePicker : ChartIntent()
    object HideEndDatePicker : ChartIntent()
    object ResetDateFilter : ChartIntent()
} 