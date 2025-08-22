package com.davidbugayov.financeanalyzer.presentation.chart.statistic.components
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.utils.LineChartUtils.findNearestPoint
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.utils.drawGridLines
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.utils.drawLineChart

import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.EmptyContent
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.hypot

// --- Constants for Dimensions ---
private val DefaultSelectionThreshold: Dp = 30.dp
private val DefaultChartHeight: Dp = 200.dp // Default height if not provided
private val AxisPaddingStart: Dp = 40.dp
private val AxisPaddingEnd: Dp = 10.dp
private val AxisPaddingTop: Dp = 20.dp
private val AxisPaddingBottom: Dp = 25.dp
private val TotalVerticalPadding: Dp = AxisPaddingTop + AxisPaddingBottom
private val AxisTickLength: Dp = 4.dp
private val AxisStrokeWidth: Float = 2.5f
private val GridStrokeWidth: Float = 1.0f
private val SelectedPointLineStrokeWidth: Float = 1.5f
private val YLabelOffset: Dp = 5.dp
private val XLabelOffset: Dp = 5.dp
private val AxisLabelFontSize = 10.sp
private const val ANIMATION_DURATION = 1500
private const val Y_AXIS_STEPS = 5
private const val X_AXIS_STEPS = 4

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
 * @param formatYValue Функция для форматирования значений на оси Y
 * @param onPointSelected Колбэк при выборе точки на графике (null если выбор сброшен)
 * @param modifier Модификатор для настройки внешнего вида
 * @param chartHeight Высота самого графика (без отступов и легенды)
 * @param selectionThresholdDp Порог выбора точки в dp
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
    formatYValue: (Float) -> String = { value ->
        when {
            value >= 1_000_000_000 ->
                String.format(
                    Locale.getDefault(),
                    "%.1fB",
                    value / 1_000_000_000,
                )
            value >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", value / 1_000)
            else -> String.format(Locale.getDefault(), "%.0f", value)
        }
    },
    modifier: Modifier = Modifier,
    onPointSelected: (LineChartPoint?) -> Unit = {},
    chartHeight: Dp = DefaultChartHeight,
    selectionThresholdDp: Dp = DefaultSelectionThreshold,
) {
    // Проверяем, есть ли данные для отображения
    val hasIncomeData = incomeData.isNotEmpty() && showIncome
    val hasExpenseData = expenseData.isNotEmpty() && showExpense

    // Если нет данных для отображения, показываем сообщение
    if (!hasIncomeData && !hasExpenseData) {
        Box(modifier = modifier.fillMaxWidth()) {
            EmptyContent(message = stringResource(UiR.string.no_data_to_display))
        }
        return
    }

    // Состояния для анимации и выбранной точки
    var selectedIncomePoint by remember { mutableStateOf<LineChartPoint?>(null) }
    var selectedExpensePoint by remember { mutableStateOf<LineChartPoint?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing),
        label = "ChartAnimation",
    )

    // Преобразуем selectionThreshold в пиксели заранее
    val thresholdPx = with(LocalDensity.current) { selectionThresholdDp.toPx() }
    with(LocalDensity.current) { chartHeight.toPx() }
    val axisTickLengthPx = with(LocalDensity.current) { AxisTickLength.toPx() }
    val yLabelOffsetPx = with(LocalDensity.current) { YLabelOffset.toPx() }
    val xLabelOffsetPx = with(LocalDensity.current) { XLabelOffset.toPx() }

    // Найдем минимальные и максимальные значения для масштабирования
    val allPoints =
        remember(incomeData, expenseData, showIncome, showExpense) {
            mutableListOf<LineChartPoint>().apply {
                if (hasIncomeData) addAll(incomeData)
                if (hasExpenseData) addAll(expenseData)
            }
        }

    // Если список пуст после фильтрации, возвращаемся
    if (allPoints.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth()) {
            EmptyContent(message = stringResource(UiR.string.no_data_to_display))
        }
        return
    }

    // Вычисляем максимальные и минимальные значения для каждого типа данных
    val (minValue, maxValue) =
        remember(allPoints) {
            val maxIncomeValue =
                if (hasIncomeData) {
                    incomeData.maxOfOrNull {
                        it.value.toMajorDouble()
                    }?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
                } else {
                    BigDecimal.ZERO
                }
            val maxExpenseValue =
                if (hasExpenseData) {
                    expenseData.maxOfOrNull {
                        it.value.toMajorDouble()
                    }?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
                } else {
                    BigDecimal.ZERO
                }
            val effectiveMaxValue = maxOf(maxIncomeValue, maxExpenseValue)
            val calculatedMaxValue =
                if (effectiveMaxValue == BigDecimal.ZERO) {
                    BigDecimal.ONE
                } else {
                    effectiveMaxValue.multiply(
                        BigDecimal("1.1"),
                    ) // Ensure not zero, add padding
                }
            0f to calculatedMaxValue.toFloat()
        }

    // Получаем крайние даты для оси X, обрабатывая случай с одной точкой
    val (chartStartDate, chartEndDate) =
        remember(allPoints) {
            if (allPoints.isEmpty()) {
                System.currentTimeMillis() to System.currentTimeMillis()
            } else {
                val minTime = allPoints.minOf { it.date.toEpochDays().toLong() * 24 * 60 * 60 * 1000 }
                val maxTime = allPoints.maxOf { it.date.toEpochDays().toLong() * 24 * 60 * 60 * 1000 }
                if (minTime == maxTime) {
                    // Для одной точки создаем диапазон +/- 1 день
                    val calendar = Calendar.getInstance().apply { timeInMillis = minTime }
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, 2) // +1 день от исходного
                    val end = calendar.timeInMillis
                    start to end
                } else {
                    minTime to maxTime
                }
            }
        }

    // Сохраняем цвета для использования в функциях рисования
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val currentIncomeColor = LocalIncomeColor.current
    val currentExpenseColor = LocalExpenseColor.current
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val axisLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    // Текстовый измеритель для осей
    val textMeasurer = rememberTextMeasurer()

    // Форматтеры дат
    val shortDateFormatter = remember { SimpleDateFormat("dd MMM", Locale.forLanguageTag("ru-RU")) }
    val fullDateFormatter =
        remember {
            SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.forLanguageTag("ru-RU"),
            )
        }

    // Получаем стандартные размеры из ресурсов
    val cardCornerRadius = dimensionResource(id = UiR.dimen.chart_card_corner_radius)
    val cardElevation = dimensionResource(id = UiR.dimen.chart_card_elevation)
    val chartCornerRadius = dimensionResource(id = UiR.dimen.chart_corner_radius)
    val spacingNormal = dimensionResource(id = UiR.dimen.chart_spacing_normal)
    val spacingMedium = dimensionResource(id = UiR.dimen.chart_spacing_medium)

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(spacingNormal),
        ) {
            // Заголовок и подзаголовок
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle.isNotEmpty() || period.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (period.isNotEmpty()) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacingNormal))

            // Отображение выбранной точки
            val selectedPoint = selectedIncomePoint ?: selectedExpensePoint
            // Анимируем высоту контейнера для выбранной точки (простая анимация)
            val animatedSpacerHeight by animateFloatAsState(
                targetValue = if (selectedPoint != null) spacingMedium.value else 0f,
                label = "SelectedPointSpacerHeight",
            )
            if (animatedSpacerHeight > 0f) {
                Spacer(modifier = Modifier.height(animatedSpacerHeight.dp))
            }

            if (selectedPoint != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = fullDateFormatter.format(selectedPoint.date),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = selectedPoint.value.toPlainString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPoint == selectedIncomePoint) currentIncomeColor else currentExpenseColor,
                    )
                }
                Spacer(modifier = Modifier.height(spacingMedium)) // Add space after selected point info
            }

            // Область с графиком и осями
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(chartHeight + TotalVerticalPadding) // Общая высота = высота графика + отступы для осей
                        .clip(RoundedCornerShape(chartCornerRadius))
                        .background(surfaceVariantColor.copy(alpha = 0.1f))
                        .padding(
                            start = AxisPaddingStart,
                            end = AxisPaddingEnd,
                            top = AxisPaddingTop,
                            bottom = AxisPaddingBottom,
                        ),
            ) {
                Canvas(
                    modifier =
                        Modifier
                            .matchParentSize() // Canvas занимает все доступное место в Box после padding
                            .pointerInput(
                                hasIncomeData,
                                hasExpenseData,
                                chartStartDate,
                                chartEndDate,
                                minValue,
                                maxValue,
                                thresholdPx,
                                animatedProgress,
                            ) {
                                detectTapGestures { offset ->
                                    val canvasWidth = size.width.toFloat()
                                    val canvasHeight = size.height.toFloat()

                                    // Проверка: если высота или ширина 0, не ищем точки
                                    if (canvasWidth <= 0 || canvasHeight <= 0) return@detectTapGestures

                                    val incomePoint =
                                        if (hasIncomeData) {
                                            findNearestPoint(
                                                points = incomeData,
                                                startDate = chartStartDate,
                                                endDate = chartEndDate,
                                                minValue = minValue,
                                                maxValue = maxValue,
                                                tapPosition = offset,
                                                chartWidth = canvasWidth,
                                                chartHeight = canvasHeight,
                                                threshold = thresholdPx,
                                                animatedProgress = animatedProgress,
                                            )
                                        } else {
                                            null
                                        }

                                    val expensePoint =
                                        if (hasExpenseData) {
                                            findNearestPoint(
                                                points = expenseData,
                                                startDate = chartStartDate,
                                                endDate = chartEndDate,
                                                minValue = minValue,
                                                maxValue = maxValue,
                                                tapPosition = offset,
                                                chartWidth = canvasWidth,
                                                chartHeight = canvasHeight,
                                                threshold = thresholdPx,
                                                animatedProgress = animatedProgress,
                                            )
                                        } else {
                                            null
                                        }

                                    val currentlySelected = selectedIncomePoint ?: selectedExpensePoint

                                    // Логика выбора ближайшей точки
                                    val newSelection =
                                        when {
                                            incomePoint != null && expensePoint != null -> {
                                                val incomeDist =
                                                    calculateDistance(
                                                        incomePoint,
                                                        offset,
                                                        chartStartDate,
                                                        chartEndDate,
                                                        minValue,
                                                        maxValue,
                                                        canvasWidth,
                                                        canvasHeight,
                                                        animatedProgress,
                                                    )
                                                val expenseDist =
                                                    calculateDistance(
                                                        expensePoint,
                                                        offset,
                                                        chartStartDate,
                                                        chartEndDate,
                                                        minValue,
                                                        maxValue,
                                                        canvasWidth,
                                                        canvasHeight,
                                                        animatedProgress,
                                                    )
                                                if (incomeDist <= expenseDist) incomePoint else expensePoint
                                            }
                                            incomePoint != null -> incomePoint
                                            expensePoint != null -> expensePoint
                                            else -> null
                                        }

                                    // Обновляем состояние, если выбор изменился или сбросился
                                    if (newSelection != currentlySelected) {
                                        if (newSelection == null) {
                                            selectedIncomePoint = null
                                            selectedExpensePoint = null
                                            onPointSelected(null)
                                        } else {
                                            if (newSelection in incomeData) {
                                                selectedIncomePoint = newSelection
                                                selectedExpensePoint = null
                                                onPointSelected(newSelection)
                                            } else if (newSelection in expenseData) {
                                                selectedIncomePoint = null
                                                selectedExpensePoint = newSelection
                                                onPointSelected(newSelection)
                                            }
                                        }
                                    }
                                }
                            }.drawBehind {
                                val width = size.width
                                val height = size.height
                                val yLabelStyle =
                                    TextStyle(
                                        fontSize = AxisLabelFontSize,
                                        color = axisLabelColor.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium,
                                    )
                                val xLabelStyle = yLabelStyle.copy(textAlign = TextAlign.Center)
                                val gridDashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)

                                // Рисуем оси X и Y
                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, height),
                                    end = Offset(width, height),
                                    strokeWidth = AxisStrokeWidth,
                                )
                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, height),
                                    strokeWidth = AxisStrokeWidth,
                                )

                                // Рисуем горизонтальные линии и метки на оси Y
                                val valueRange = maxValue - minValue
                                if (valueRange > 0) { // Избегаем деления на ноль
                                    for (i in 0..Y_AXIS_STEPS) {
                                        val y = height - (height / Y_AXIS_STEPS.toFloat() * i)
                                        val value = minValue + (valueRange / Y_AXIS_STEPS.toFloat() * i)

                                        drawLine(
                                            color = gridColor,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = GridStrokeWidth,
                                            pathEffect = gridDashEffect,
                                        )

                                        val formattedValue = formatYValue(value)
                                        val textLayoutResult =
                                            textMeasurer.measure(
                                                text = formattedValue,
                                                style = yLabelStyle,
                                            )
                                        drawText(
                                            textLayoutResult = textLayoutResult,
                                            topLeft =
                                                Offset(
                                                    -textLayoutResult.size.width - yLabelOffsetPx,
                                                    y - textLayoutResult.size.height / 2,
                                                ),
                                        )
                                        drawLine(
                                            color = axisColor,
                                            start = Offset(-axisTickLengthPx, y),
                                            end = Offset(0f, y),
                                            strokeWidth = SelectedPointLineStrokeWidth,
                                        )
                                    }
                                }

                                // Рисуем метки на оси X (даты)
                                val timeRange = chartEndDate - chartStartDate
                                if (timeRange > 0) { // Избегаем деления на ноль
                                    for (i in 0..X_AXIS_STEPS) {
                                        val ratio = i.toFloat() / X_AXIS_STEPS.toFloat()
                                        val x = width * ratio
                                        val date = Date(chartStartDate + (timeRange * ratio).toLong())
                                        val formattedDate = shortDateFormatter.format(date)

                                        // Рисуем вертикальную линию сетки (не для крайних точек)
                                        if (i > 0 && i < X_AXIS_STEPS) {
                                            drawLine(
                                                color = gridColor,
                                                start = Offset(x, 0f),
                                                end = Offset(x, height),
                                                strokeWidth = GridStrokeWidth,
                                                pathEffect = gridDashEffect,
                                            )
                                        }

                                        val textLayoutResult =
                                            textMeasurer.measure(
                                                text = formattedDate,
                                                style = xLabelStyle,
                                            )
                                        drawText(
                                            textLayoutResult = textLayoutResult,
                                            topLeft =
                                                Offset(
                                                    x - textLayoutResult.size.width / 2,
                                                    height + xLabelOffsetPx,
                                                ),
                                        )
                                        drawLine(
                                            color = axisColor,
                                            start = Offset(x, height),
                                            end = Offset(x, height + axisTickLengthPx),
                                            strokeWidth = SelectedPointLineStrokeWidth,
                                        )
                                    }
                                }
                                drawGridLines(
                                    width,
                                    height,
                                    color = gridColor,
                                    pathEffect = gridDashEffect,
                                )
                            },
                ) {
                    // Отрисовка графиков
                    if (hasIncomeData) {
                        drawLineChart(
                            points = incomeData,
                            startDate = chartStartDate,
                            endDate = chartEndDate,
                            minValue = minValue,
                            maxValue = maxValue,
                            lineColor = currentIncomeColor,
                            fillColor = currentIncomeColor.copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedIncomePoint,
                        )
                    }
                    if (hasExpenseData) {
                        drawLineChart(
                            points = expenseData,
                            startDate = chartStartDate,
                            endDate = chartEndDate,
                            minValue = minValue,
                            maxValue = maxValue,
                            lineColor = currentExpenseColor,
                            fillColor = currentExpenseColor.copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedExpensePoint,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacingMedium))

            // Легенда
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = spacingMedium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showIncome) {
                    ChartLegendItem(
                        color = currentIncomeColor,
                        text = stringResource(UiR.string.income),
                    )
                    if (showExpense) {
                        Spacer(modifier = Modifier.width(spacingNormal))
                    }
                }
                if (showExpense) {
                    ChartLegendItem(
                        color = currentExpenseColor,
                        text = stringResource(UiR.string.expenses),
                    )
                }
            }
        }
    }

    // Отслеживаем изменения видимости и сбрасываем точки, если нужно
    LaunchedEffect(showIncome, showExpense) {
        if (!showIncome && selectedIncomePoint != null) {
            selectedIncomePoint = null
            if (selectedExpensePoint == null) onPointSelected(null)
        }
        if (!showExpense && selectedExpensePoint != null) {
            selectedExpensePoint = null
            if (selectedIncomePoint == null) onPointSelected(null)
        }
        // Ensure the callback reflects the current visible selection
        if (showIncome && selectedIncomePoint != null) {
            onPointSelected(selectedIncomePoint)
        } else if (showExpense && selectedExpensePoint != null) {
            onPointSelected(selectedExpensePoint)
        } else if (selectedIncomePoint == null && selectedExpensePoint == null) {
            onPointSelected(null)
        }
    }
}

// Helper function to calculate distance for tap handling
private fun calculateDistance(
    point: LineChartPoint,
    tapPosition: Offset,
    startDate: Long,
    endDate: Long,
    minValue: Float,
    maxValue: Float,
    chartWidth: Float,
    chartHeight: Float,
    animatedProgress: Float,
): Float {
    if (endDate <= startDate || maxValue <= minValue) return Float.MAX_VALUE // Avoid division by zero
    val normalizedX = (point.date.toEpochDays().toLong() * 24 * 60 * 60 * 1000 - startDate).toFloat() / (endDate - startDate).toFloat()
    val normalizedY = 1f - (point.value.toMajorDouble().toFloat() - minValue) / (maxValue - minValue)

    if (!normalizedX.isFinite() || !normalizedY.isFinite()) return Float.MAX_VALUE // Check for NaN/Infinity

    val x = normalizedX * chartWidth * animatedProgress
    val y = normalizedY * chartHeight
    return hypot(x - tapPosition.x, y - tapPosition.y)
}
