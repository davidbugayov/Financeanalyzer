package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.annotation.StringRes
import com.davidbugayov.financeanalyzer.R

/**
 * Режимы отображения для линейного графика
 */
enum class LineChartDisplayMode(
    @StringRes val titleResId: Int
) {
    INCOME(R.string.chart_title_income),
    EXPENSE(R.string.chart_title_expense),
    BOTH(R.string.chart_title_both);
    
    companion object {
        /**
         * Возвращает заголовок графика на основе выбранного режима
         */
        @StringRes
        fun getTitleResId(mode: LineChartDisplayMode): Int {
            return when(mode) {
                INCOME -> R.string.chart_title_income_dynamics
                EXPENSE -> R.string.chart_title_expense_dynamics
                BOTH -> R.string.chart_title_income_expense_dynamics
            }
        }
    }
} 