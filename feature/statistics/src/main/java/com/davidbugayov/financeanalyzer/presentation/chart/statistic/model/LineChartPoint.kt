package com.davidbugayov.financeanalyzer.presentation.chart.statistic.model

import com.davidbugayov.financeanalyzer.shared.model.Money
import kotlinx.datetime.LocalDate

/**
 * Точка данных для линейного графика
 *
 * @param date Дата точки
 * @param value Значение (сумма) точки
 * @param description Опциональное описание точки
 */
data class LineChartPoint(
    val date: LocalDate,
    val value: Money,
    val description: String = "",
)
