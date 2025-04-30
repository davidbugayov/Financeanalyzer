package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model

import androidx.compose.ui.geometry.Offset

/**
 * Класс для хранения информации о секторе пирографа
 *
 * @property category Название категории для сектора
 * @property center Центр сектора
 * @property outerRadius Внешний радиус сектора
 * @property innerRadius Внутренний радиус сектора
 * @property startAngle Начальный угол сектора в градусах
 * @property sweepAngle Угол сектора в градусах
 */
data class PieSectorInfo(
    val category: String,
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val startAngle: Float,
    val sweepAngle: Float
) 