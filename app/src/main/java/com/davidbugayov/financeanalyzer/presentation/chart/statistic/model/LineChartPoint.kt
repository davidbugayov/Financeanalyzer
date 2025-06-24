package com.davidbugayov.financeanalyzer.presentation.chart.statistic.model

import com.davidbugayov.financeanalyzer.core.model.Money
import java.util.Date

/**
 * Точка данных для линейного графика
 *
 * @param date Дата точки
 * @param value Значение (сумма) точки
 * @param description Опциональное описание точки
 */
data class LineChartPoint(
    val date: Date,
    val value: Money,
    val description: String = "",
)
