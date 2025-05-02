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
} 