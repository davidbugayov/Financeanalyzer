package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

/**
 * Режимы отображения для линейного графика
 */
enum class LineChartDisplayMode(val title: String) {

    /** Отображать только доходы */
    INCOME("Доходы"),

    /** Отображать только расходы */
    EXPENSE("Расходы"),

    /** Отображать и доходы, и расходы */
    BOTH("Оба")
} 