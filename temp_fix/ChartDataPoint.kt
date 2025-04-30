package com.davidbugayov.financeanalyzer.presentation.chart

import com.davidbugayov.financeanalyzer.domain.model.Money
import java.util.Date

/**
 * Класс для хранения точки данных на графике
 *
 * @property date Дата точки
 * @property value Значение точки (деньги)
 */
data class ChartDataPoint(
    val date: Date,
    val value: Money
) 