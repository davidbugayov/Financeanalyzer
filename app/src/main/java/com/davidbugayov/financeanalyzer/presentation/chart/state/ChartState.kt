package com.davidbugayov.financeanalyzer.presentation.chart.state

import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Состояние экрана с графиками.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class ChartState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val incomeByCategory: Map<String, Double> = emptyMap(),
    val transactionsByMonth: Map<String, ChartMonthlyData> = emptyMap(),
    val expensesByDay: Map<String, ChartMonthlyData> = emptyMap()
)

/**
 * Данные о транзакциях за период (месяц или день)
 */
data class ChartMonthlyData(
    val totalIncome: Double,
    val totalExpense: Double,
    val categoryBreakdown: Map<String, Double>
) 