package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model

import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.ChartDataPoint
import java.util.Date

/**
 * Точка данных для линейного графика
 *
 * @param date Дата точки
 * @param value Значение (сумма) точки
 * @param description Опциональное описание точки
 */
data class LineChartPoint(
    override val date: Date,
    override val value: Money,
    val description: String = ""
) : ChartDataPoint 