package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Модель данных для точки на линейном графике
 */
data class LineChartPoint(
    val date: Date,
    val value: Money,
    val label: String = ""
)

/**
 * Улучшенный линейный график для отображения динамики доходов/расходов
 *
 * @param incomeData Точки данных для линии доходов
 * @param expenseData Точки данных для линии расходов
 * @param showIncome Флаг отображения доходов
 * @param showExpense Флаг отображения расходов
 * @param title Заголовок графика
 * @param subtitle Подзаголовок графика
 * @param period Текстовое описание периода
 * @param onPointSelected Колбэк при выборе точки на графике
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun EnhancedLineChart(
    incomeData: List<LineChartPoint>,
    expenseData: List<LineChartPoint>,
    showIncome: Boolean = true,
    showExpense: Boolean = true,
    title: String = "Динамика",
    subtitle: String = "",
    period: String = "",
    onPointSelected: (LineChartPoint) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Проверяем, есть ли данные для отображения
    val hasIncomeData = incomeData.isNotEmpty() && showIncome
    val hasExpenseData = expenseData.isNotEmpty() && showExpense

    // Если нет данных для отображения, показываем сообщение
    if (!hasIncomeData && !hasExpenseData) {
        Box(modifier = modifier.fillMaxWidth()) {
            EmptyContent()
        }
        return
    }

    // Состояния для анимации и выбранной точки
    var selectedIncomePoint by remember { mutableStateOf<LineChartPoint?>(null) }
    var selectedExpensePoint by remember { mutableStateOf<LineChartPoint?>(null) }
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )

    // Найдем минимальные и максимальные значения для масштабирования
    val allPoints = mutableListOf<LineChartPoint>().apply {
        if (hasIncomeData) addAll(incomeData)
        if (hasExpenseData) addAll(expenseData)
    }

    // Если список пуст, возвращаемся
    if (allPoints.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth()) {
            EmptyContent()
        }
        return
    }

    // Convert to float for consistent use in drawing functions
    val maxValueFloat = allPoints.maxOf { it.value.amount.toDouble() }.toFloat()
    val minValueFloat = allPoints.minOf { it.value.amount.toDouble() }.toFloat()

    // Получаем крайние даты для оси X
    val startDate = allPoints.minOf { it.date.time }
    val endDate = allPoints.maxOf { it.date.time }

    // Сохраняем цвета поверхности для использования в функциях рисования
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    // Создаем карточку с графиком
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                // При клике на пустую область сбрасываем выбор
                selectedIncomePoint = null
                selectedExpensePoint = null
                // Вызываем колбэк для сброса выбора
                // Создаем временную точку с пустыми данными для сброса выбора
                onPointSelected(LineChartPoint(Date(), Money.zero(), ""))
            },
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок графика
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Подзаголовок и период
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (period.isNotEmpty()) {
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Отображение выбранной точки
            val selectedPoint = selectedIncomePoint ?: selectedExpensePoint
            if (selectedPoint != null) {
                val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateFormatter.format(selectedPoint.date),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = selectedPoint.value.format(true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPoint == selectedIncomePoint) Color(0xFF66BB6A) else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Область с графиком
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceVariantColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                // Отрисовка графика на холсте
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(incomeData, expenseData) {
                            detectTapGestures { offset ->
                                // Обработка нажатия на точки графика
                                val chartWidth = size.width
                                val chartHeight = size.height

                                // Функция для поиска ближайшей точки
                                fun findNearestPoint(
                                    points: List<LineChartPoint>,
                                    startDate: Long,
                                    endDate: Long,
                                    minValue: Float,
                                    maxValue: Float,
                                    tapPosition: Offset,
                                    chartWidth: Float,
                                    chartHeight: Float,
                                    threshold: Float = 20f
                                ): LineChartPoint? {
                                    if (points.isEmpty()) return null

                                    var nearestPoint: LineChartPoint? = null
                                    var nearestDistance = Float.MAX_VALUE

                                    points.forEach { point ->
                                        val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
                                        val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)

                                        val pointX = normalizedX * chartWidth
                                        val pointY = normalizedY * chartHeight

                                        val distance = kotlin.math.hypot(pointX - tapPosition.x, pointY - tapPosition.y)

                                        if (distance < nearestDistance && distance < threshold) {
                                            nearestDistance = distance
                                            nearestPoint = point
                                        }
                                    }

                                    return nearestPoint
                                }

                                // Ищем ближайшие точки на обеих линиях
                                val incomePoint = if (hasIncomeData) findNearestPoint(
                                    points = incomeData,
                                    startDate = startDate,
                                    endDate = endDate,
                                    minValue = minValueFloat,
                                    maxValue = maxValueFloat,
                                    tapPosition = offset,
                                    chartWidth = chartWidth.toFloat(),
                                    chartHeight = chartHeight.toFloat(),
                                    threshold = 30.dp.toPx() // Увеличиваем порог для более удобного выбора
                                ) else null

                                val expensePoint = if (hasExpenseData) findNearestPoint(
                                    points = expenseData,
                                    startDate = startDate,
                                    endDate = endDate,
                                    minValue = minValueFloat,
                                    maxValue = maxValueFloat,
                                    tapPosition = offset,
                                    chartWidth = chartWidth.toFloat(),
                                    chartHeight = chartHeight.toFloat(),
                                    threshold = 30.dp.toPx() // Увеличиваем порог для более удобного выбора
                                ) else null

                                // Выбираем ближайшую из всех найденных
                                if (incomePoint != null && expensePoint != null) {
                                    // Если найдены точки на обеих линиях, выбираем ближайшую
                                    val incomeX = (incomePoint.date.time - startDate).toFloat() / (endDate - startDate).toFloat() * chartWidth
                                    val incomeY =
                                        (1f - (incomePoint.value.amount.toFloat() - minValueFloat) / (maxValueFloat - minValueFloat)) * chartHeight

                                    val expenseX = (expensePoint.date.time - startDate).toFloat() / (endDate - startDate).toFloat() * chartWidth
                                    val expenseY =
                                        (1f - (expensePoint.value.amount.toFloat() - minValueFloat) / (maxValueFloat - minValueFloat)) * chartHeight

                                    val incomeDistance = kotlin.math.hypot(incomeX - offset.x, incomeY - offset.y)
                                    val expenseDistance = kotlin.math.hypot(expenseX - offset.x, expenseY - offset.y)

                                    if (incomeDistance < expenseDistance) {
                                        selectedIncomePoint = incomePoint
                                        selectedExpensePoint = null
                                        onPointSelected(incomePoint)
                                    } else {
                                        selectedIncomePoint = null
                                        selectedExpensePoint = expensePoint
                                        onPointSelected(expensePoint)
                                    }
                                } else if (incomePoint != null) {
                                    selectedIncomePoint = incomePoint
                                    selectedExpensePoint = null
                                    onPointSelected(incomePoint)
                                } else if (expensePoint != null) {
                                    selectedIncomePoint = null
                                    selectedExpensePoint = expensePoint
                                    onPointSelected(expensePoint)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // Отрисовка сетки
                    drawGridLines(width, height)

                    // Отрисовка линии доходов
                    if (hasIncomeData && incomeData.size > 1) {
                        drawLineChart(
                            points = incomeData,
                            startDate = startDate,
                            endDate = endDate,
                            minValue = minValueFloat,
                            maxValue = maxValueFloat,
                            lineColor = Color(0xFF66BB6A),
                            fillColor = Color(0xFF66BB6A).copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedIncomePoint
                        )
                    }

                    // Отрисовка линии расходов
                    if (hasExpenseData && expenseData.size > 1) {
                        drawLineChart(
                            points = expenseData,
                            startDate = startDate,
                            endDate = endDate,
                            minValue = minValueFloat,
                            maxValue = maxValueFloat,
                            lineColor = errorColor,
                            fillColor = errorColor.copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedExpensePoint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Легенда для графика
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIncome) {
                    ChartLegendItem(
                        color = Color(0xFF66BB6A),
                        text = "Доходы"
                    )

                    if (showExpense) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }

                if (showExpense) {
                    ChartLegendItem(
                        color = MaterialTheme.colorScheme.error,
                        text = "Расходы"
                    )
                }
            }
        }
    }
}

/**
 * Легенда для графика
 */
@Composable
private fun ChartLegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(4.dp)
                .background(color, RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Функция расширения для рисования сетки графика
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridLines(
    width: Float,
    height: Float
) {
    // Рисуем горизонтальные линии сетки
    val horizontalLineCount = 4
    val dashPattern = floatArrayOf(10f, 10f)

    repeat(horizontalLineCount) { i ->
        val y = height * i / (horizontalLineCount - 1)

        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(dashPattern)
        )
    }

    // Рисуем вертикальные линии сетки
    val verticalLineCount = 5

    repeat(verticalLineCount) { i ->
        val x = width * i / (verticalLineCount - 1)

        drawLine(
            color = Color.Gray.copy(alpha = 0.2f),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(dashPattern)
        )
    }
}

/**
 * Функция расширения для рисования линейного графика
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLineChart(
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
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // Рисуем точки на линии, если нужно показать все точки
    points.forEach { point ->
        val normalizedX = (point.date.time - startDate).toFloat() / (endDate - startDate).toFloat()
        val normalizedY = 1f - (point.value.amount.toFloat() - minValue) / (maxValue - minValue)

        val scaledX = normalizedX * width * animatedProgress
        val scaledY = normalizedY * height

        // Если это выбранная точка, рисуем ее крупнее
        if (point == selectedPoint) {
            // Рисуем внешний круг
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(scaledX, scaledY)
            )

            // Рисуем внутренний круг
            drawCircle(
                color = lineColor,
                radius = 8f,
                center = Offset(scaledX, scaledY)
            )
        }
    }
} 