package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.LineChartPoint
import com.davidbugayov.financeanalyzer.presentation.chart.ChartDataPoint
import kotlin.math.hypot

private const val TAG = "LineChartUtils"

/**
 * Функция для рисования сетки графика
 *
 * @param width Ширина холста
 * @param height Высота холста
 */
fun DrawScope.drawGridLines(width: Float, height: Float) {
    val gridColor = Color(0x20000000) // Серый цвет с прозрачностью
    val gridStrokeWidth = 1.dp.toPx()

    // Горизонтальные линии (5 линий)
    val horizontalLines = 4
    for (i in 0..horizontalLines) {
        val y = height * i / horizontalLines
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = gridStrokeWidth
        )
    }

    // Вертикальные линии (5 линий)
    val verticalLines = 4
    for (i in 0..verticalLines) {
        val x = width * i / verticalLines
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = gridStrokeWidth
        )
    }
}

/**
 * Функция для рисования линейного графика
 *
 * @param points Точки для графика
 * @param startDate Начальная дата (в миллисекундах)
 * @param endDate Конечная дата (в миллисекундах)
 * @param minValue Минимальное значение для масштабирования
 * @param maxValue Максимальное значение для масштабирования
 * @param lineColor Цвет линии
 * @param fillColor Цвет заполнения под линией
 * @param animatedProgress Прогресс анимации (0.0-1.0)
 * @param selectedPoint Выбранная точка на графике
 */
fun DrawScope.drawLineChart(
    points: List<LineChartPoint>,
    startDate: Long,
    endDate: Long,
    minValue: Float,
    maxValue: Float,
    lineColor: Color,
    fillColor: Color,
    animatedProgress: Float,
    selectedPoint: LineChartPoint?
) {
    if (points.isEmpty()) return

    val width = size.width
    val height = size.height
    
    // Используем размеры из ресурсов
    val lineStrokeWidth = 2.dp.toPx()
    val pointRadius = 4.dp.toPx()
    val selectedPointRadius = 6.dp.toPx()
    val selectedPointStrokeWidth = 4.dp.toPx()

    // Создаем путь для линии
    val linePath = Path()
    val fillPath = Path()

    // Нормализуем данные и создаем путь
    var isFirst = true

    points.forEachIndexed { index, point ->
        // Нормализуем координаты точки
        val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
        val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)

        // Масштабируем согласно анимации и размеру холста
        val scaledX = normalizedX * width * animatedProgress
        val scaledY = normalizedY * height

        // Добавляем точку к пути
        if (isFirst) {
            linePath.moveTo(0f, scaledY)
            fillPath.moveTo(0f, height)
            fillPath.lineTo(0f, scaledY)
            isFirst = false
        }

        linePath.lineTo(scaledX, scaledY)
        fillPath.lineTo(scaledX, scaledY)

        // Если это последняя точка
        if (index == points.size - 1 && animatedProgress >= 1f) {
            fillPath.lineTo(scaledX, height)
            fillPath.lineTo(0f, height)
            fillPath.close()
        }
    }

    // Рисуем заполнение под линией
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                fillColor,
                fillColor.copy(alpha = 0.1f)
            ),
            startY = 0f,
            endY = height
        )
    )

    // Рисуем саму линию
    drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(
            width = lineStrokeWidth,
            cap = StrokeCap.Round
        )
    )

    // Рисуем точки на линии
    points.forEach { point ->
        val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
        val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)
        
        val scaledX = normalizedX * width * animatedProgress
        val scaledY = normalizedY * height
        
        // Проверяем, является ли текущая точка выбранной
        val isSelected = selectedPoint?.date?.time == point.date.time
        
        if (isSelected) {
            // Рисуем внешний круг для выделенной точки
            drawCircle(
                color = lineColor,
                radius = selectedPointRadius,
                center = Offset(scaledX, scaledY)
            )
            
            // Рисуем внутренний круг для выделенной точки
            drawCircle(
                color = Color.White,
                radius = selectedPointRadius - selectedPointStrokeWidth / 2,
                center = Offset(scaledX, scaledY)
            )
        } else {
            // Рисуем точку
            drawCircle(
                color = lineColor,
                radius = pointRadius,
                center = Offset(scaledX, scaledY)
            )
        }
    }
}

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
    fun <T : ChartDataPoint> findNearestPoint(
        points: List<T>,
        startDate: Long,
        endDate: Long,
        minValue: Float,
        maxValue: Float,
        tapPosition: Offset,
        chartWidth: Float,
        chartHeight: Float,
        threshold: Float
    ): T? {
        if (points.isEmpty()) return null
        
        var closestPoint: T? = null
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