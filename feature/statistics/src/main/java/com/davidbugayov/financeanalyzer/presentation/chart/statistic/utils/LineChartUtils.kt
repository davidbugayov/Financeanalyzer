package com.davidbugayov.financeanalyzer.presentation.chart.statistic.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint
import kotlin.math.hypot
import timber.log.Timber

/**
 * Функция для рисования сетки графика
 *
 * @param width Ширина холста
 * @param height Высота холста
 * @param color Цвет линий сетки
 * @param pathEffect Эффект для линий сетки (например, пунктир)
 */
fun DrawScope.drawGridLines(
    width: Float,
    height: Float,
    color: Color,
    pathEffect: PathEffect?,
) {
    val gridStrokeWidth = 0.5.dp.toPx()

    // Горизонтальные линии (5 линий)
    val horizontalLines = 4
    for (i in 0..horizontalLines) {
        val y = height * i / horizontalLines
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = gridStrokeWidth,
            pathEffect = pathEffect,
        )
    }

    // Вертикальные линии (5 линий)
    val verticalLines = 4
    for (i in 0..verticalLines) {
        val x = width * i / verticalLines
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = gridStrokeWidth,
            pathEffect = pathEffect,
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
    selectedPoint: LineChartPoint?,
) {
    if (points.isEmpty()) {
        Timber.d("drawLineChart: список точек пуст, не рисуем график")
        return
    }

    // Логируем точки для отладки
    Timber.d("drawLineChart: рисуем график с ${points.size} точками, цвет: $lineColor")
    Timber.d(
        "drawLineChart: первая точка: дата=${points.first().date}, значение=${points.first().value.amount}",
    )
    Timber.d(
        "drawLineChart: последняя точка: дата=${points.last().date}, значение=${points.last().value.amount}",
    )

    // Логируем метки времени для сравнения
    Timber.d(
        "drawLineChart: startDate=$startDate, endDate=$endDate, minValue=$minValue, maxValue=$maxValue",
    )

    // Отладочная информация: показываем первую точку в виде координат для проверки масштабирования
    val firstPoint = points.first()
    val firstNormalizedX = (firstPoint.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
    val firstNormalizedY = 1f - (firstPoint.value.amount.toFloat() - minValue) / (maxValue - minValue)
    val firstX = firstNormalizedX * size.width * animatedProgress
    val firstY = firstNormalizedY * size.height
    Timber.d(
        "drawLineChart: первая точка ($firstNormalizedX, $firstNormalizedY) -> ($firstX, $firstY)",
    )

    val width = size.width
    val height = size.height

    // Используем размеры из ресурсов
    val lineStrokeWidth = 3.dp.toPx()
    val pointRadius = 5.dp.toPx() // Увеличен радиус точек
    val selectedPointRadius = 10.dp.toPx() // Увеличен радиус выбранной точки
    val selectedPointStrokeWidth = 3.dp.toPx()
    val glowRadius = 16.dp.toPx() // Увеличен радиус свечения

    // Создаем путь для линии с использованием кривой Безье для плавности
    val linePath = Path()
    val fillPath = Path()

    // Массивы для хранения координат точек
    val pointsX = FloatArray(points.size)
    val pointsY = FloatArray(points.size)

    // Подготавливаем координаты всех точек
    points.forEachIndexed { index, point ->
        // Нормализуем координаты точки
        val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
        val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)

        // Масштабируем согласно анимации и размеру холста
        pointsX[index] = normalizedX * width * animatedProgress
        pointsY[index] = normalizedY * height
    }

    // Строим путь линии с использованием кубической кривой Безье для плавности
    if (points.size > 1) {
        linePath.moveTo(pointsX[0], pointsY[0])
        fillPath.moveTo(pointsX[0], height)
        fillPath.lineTo(pointsX[0], pointsY[0])

        for (i in 1 until points.size) {
            if (i < points.size - 1) {
                // Вычисляем контрольные точки для кривой Безье
                val controlX1 = (pointsX[i - 1] + pointsX[i]) / 2f
                val controlY1 = pointsY[i - 1]
                val controlX2 = (pointsX[i - 1] + pointsX[i]) / 2f
                val controlY2 = pointsY[i]

                // Добавляем кубическую кривую Безье для плавности
                linePath.cubicTo(
                    controlX1,
                    controlY1,
                    controlX2,
                    controlY2,
                    pointsX[i],
                    pointsY[i],
                )
                fillPath.cubicTo(
                    controlX1,
                    controlY1,
                    controlX2,
                    controlY2,
                    pointsX[i],
                    pointsY[i],
                )
            } else {
                // Для последней точки используем простую линию
                linePath.lineTo(pointsX[i], pointsY[i])
                fillPath.lineTo(pointsX[i], pointsY[i])
            }
        }

        // Завершаем путь заполнения
        fillPath.lineTo(pointsX[points.size - 1], height)
        fillPath.lineTo(pointsX[0], height)
        fillPath.close()

        // Рисуем заполнение под линией с улучшенным градиентом
        drawPath(
            path = fillPath,
            brush =
                Brush.verticalGradient(
                    colors =
                        listOf(
                            fillColor.copy(alpha = 0.4f),
                            fillColor.copy(alpha = 0.15f),
                            fillColor.copy(alpha = 0.05f),
                        ),
                    startY = 0f,
                    endY = height,
                ),
            style = Fill,
        )

        // Рисуем саму линию с улучшенным стилем
        drawPath(
            path = linePath,
            color = lineColor,
            style =
                Stroke(
                    width = lineStrokeWidth,
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round,
                ),
            alpha = 0.9f, // Небольшая прозрачность для красоты
        )
    }

    // Рисуем точки на линии с улучшенной визуализацией
    points.forEachIndexed { index, point ->
        val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
        val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)

        val x = normalizedX * width * animatedProgress
        val y = normalizedY * height

        // Логируем координаты отрисовки точки
        Timber.d(
            "drawLineChart [Point $index Draw]: Date=${point.date}, Drawing at Coords=($x, $y)",
        )

        // Проверяем, является ли текущая точка выбранной
        val isSelected = selectedPoint?.date?.time == point.date.time

        if (isSelected) {
            // Рисуем эффект свечения для выделенной точки
            drawCircle(
                color = lineColor.copy(alpha = 0.2f),
                radius = glowRadius,
                center = Offset(x, y),
            )

            // Рисуем дополнительное кольцо для усиления эффекта
            drawCircle(
                color = lineColor.copy(alpha = 0.4f),
                radius = selectedPointRadius + 4.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 2.dp.toPx()),
            )

            // Рисуем внешний круг для выделенной точки
            drawCircle(
                color = lineColor,
                radius = selectedPointRadius,
                center = Offset(x, y),
            )

            // Рисуем внутренний круг для выделенной точки
            drawCircle(
                color = Color.White,
                radius = selectedPointRadius - selectedPointStrokeWidth / 2,
                center = Offset(x, y),
            )
        } else {
            // Для одиночных точек и точек на графике рисуем увеличенный круг
            val actualPointRadius = if (points.size == 1) pointRadius * 1.5f else pointRadius

            // Улучшенное отображение обычных точек
            // Рисуем небольшое свечение вокруг точки
            drawCircle(
                color = lineColor.copy(alpha = 0.1f),
                radius = actualPointRadius + 3.dp.toPx(),
                center = Offset(x, y),
            )

            // Рисуем внешний круг с обводкой
            drawCircle(
                color = lineColor.copy(alpha = 0.5f),
                radius = actualPointRadius + 1.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 1.dp.toPx()),
            )

            // Рисуем внешний круг для лучшей видимости
            drawCircle(
                color = Color.White,
                radius = actualPointRadius,
                center = Offset(x, y),
            )

            // Рисуем внутренний круг с цветом линии
            drawCircle(
                color = lineColor,
                radius = actualPointRadius - 1.5f,
                center = Offset(x, y),
            )
        }

        // Рисуем пунктирные линии от точки к осям
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
        val dashLineColor = lineColor.copy(alpha = 0.4f)
        val dashStrokeWidth = 1.dp.toPx()

        // Вертикальная линия до оси X
        drawLine(
            color = dashLineColor,
            start = Offset(x, y),
            end = Offset(x, height),
            strokeWidth = dashStrokeWidth,
            pathEffect = dashEffect,
        )

        // Горизонтальная линия до оси Y
        drawLine(
            color = dashLineColor,
            start = Offset(x, y),
            end = Offset(0f, y),
            strokeWidth = dashStrokeWidth,
            pathEffect = dashEffect,
        )
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
     * @param animatedProgress Текущий прогресс анимации (для корректного расчета X)
     * @return Ближайшая точка или null, если нет точек в пределах порога
     */
    fun findNearestPoint(
        points: List<LineChartPoint>,
        startDate: Long,
        endDate: Long,
        minValue: Float,
        maxValue: Float,
        tapPosition: Offset,
        chartWidth: Float,
        chartHeight: Float,
        threshold: Float,
        animatedProgress: Float = 1f,
    ): LineChartPoint? {
        if (points.isEmpty()) return null

        var closestPoint: LineChartPoint? = null
        var minDistance = Float.MAX_VALUE

        points.forEachIndexed { index, point ->
            val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
            val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)

            val x = normalizedX * chartWidth * animatedProgress
            val y = normalizedY * chartHeight

            val distance = hypot(x - tapPosition.x, y - tapPosition.y)

            if (distance < threshold && distance < minDistance) {
                minDistance = distance
                closestPoint = point
            } else if (distance < threshold) {
                // No Timber.d here
            } else {
                // No Timber.d here
            }
        }

        return closestPoint
    }
}
