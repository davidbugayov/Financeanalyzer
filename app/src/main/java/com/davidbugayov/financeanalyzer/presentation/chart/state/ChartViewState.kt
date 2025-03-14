package com.davidbugayov.financeanalyzer.presentation.chart.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Calendar
import java.util.Date

data class ChartViewState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showExpenses: Boolean = true,
    val startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
    val endDate: Date = Calendar.getInstance().time
) 