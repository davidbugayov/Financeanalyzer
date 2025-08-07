package com.davidbugayov.financeanalyzer.presentation.chart.statistic.model

/**
 * Режимы отображения для линейного графика
 */
enum class LineChartDisplayMode {

    INCOME {
        override val showIncome: Boolean get() = true
        override val showExpense: Boolean get() = false
    },
    EXPENSE {
        override val showIncome: Boolean get() = false
        override val showExpense: Boolean get() = true
    },
    BOTH {
        override val showIncome: Boolean get() = true
        override val showExpense: Boolean get() = true
    };

    abstract val showIncome: Boolean
    abstract val showExpense: Boolean
}
