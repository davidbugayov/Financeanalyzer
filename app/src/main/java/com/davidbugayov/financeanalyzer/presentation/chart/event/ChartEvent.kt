package com.davidbugayov.financeanalyzer.presentation.chart.event

/**
 * События для экрана с графиками.
 * Следует принципу открытости/закрытости (OCP) из SOLID.
 */
sealed class ChartEvent {

    data object LoadTransactions : ChartEvent()
    data class SetDaysForExpensesChart(val days: Int) : ChartEvent()
} 