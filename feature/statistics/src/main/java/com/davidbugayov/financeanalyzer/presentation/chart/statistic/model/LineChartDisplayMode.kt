package com.davidbugayov.financeanalyzer.presentation.chart.statistic.model

import androidx.annotation.StringRes
import com.davidbugayov.financeanalyzer.feature.statistics.R

/**
 * Режимы отображения для линейного графика
 */
enum class LineChartDisplayMode(
    @StringRes val titleResId: Int,
) {
    INCOME(R.string.chart_title_income) {
        override val showIncome: Boolean get() = true
        override val showExpense: Boolean get() = false
    },
    EXPENSE(R.string.chart_title_expense) {
        override val showIncome: Boolean get() = false
        override val showExpense: Boolean get() = true
    },
    BOTH(R.string.chart_title_both) {
        override val showIncome: Boolean get() = true
        override val showExpense: Boolean get() = true
    }, ;

    abstract val showIncome: Boolean
    abstract val showExpense: Boolean
}
