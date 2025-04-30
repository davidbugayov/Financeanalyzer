package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils

import androidx.compose.ui.geometry.Offset
import com.davidbugayov.financeanalyzer.presentation.chart.ChartDataPoint
import kotlin.math.hypot

/**
 * Утилитные функции для линейных графиков
 */
object LineChartUtils {

    /**
     * Функция для поиска ближайшей точки к месту нажатия
     *
     * @param points Список точек для проверки
     * @param startDate Начальная дата (в миллисекундах)
     * @param endDate Конечная дата (в миллисекундах)
     * @param minValue Минимальное значение для масштабирования
     * @param maxValue Максимальное значение для масштабирования
     * @param tapPosition Позиция нажатия
     * @param chartWidth Ширина графика
     * @param chartHeight Высота графика
     * @param threshold Порог для определения близости (в пикселях)
     * @return Ближайшая точка или null, если нет точек в пределах порога
     */
    fun findNearestPoint(
        points: List<ChartDataPoint>,
        startDate: Long,
        endDate: Long,
        minValue: Float,
        maxValue: Float,
        tapPosition: Offset,
        chartWidth: Float,
        chartHeight: Float,
        threshold: Float
    ): ChartDataPoint? {
        if (points.isEmpty()) return null
        
        var closestPoint: ChartDataPoint? = null
        var minDistance = Float.MAX_VALUE
        
        points.forEach { point ->
            val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
            val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)
            
            val x = normalizedX * chartWidth
            val y = normalizedY * chartHeight
            
            val distance = hypot(x - tapPosition.x, y - tapPosition.y)
            
            if (distance < threshold && distance < minDistance) {
                minDistance = distance
                closestPoint = point
            }
        }
        
        return closestPoint
    }
} 