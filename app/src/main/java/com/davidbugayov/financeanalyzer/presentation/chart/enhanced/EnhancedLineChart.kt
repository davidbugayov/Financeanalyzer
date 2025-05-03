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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.ChartLegendItem
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.LineChartPoint
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils.drawGridLines
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils.drawLineChart
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.utils.LineChartUtils.findNearestPoint
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt
import timber.log.Timber

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
 * @param onPointSelected Колбэк при выборе точки на графике
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun EnhancedLineChart(
    incomeData: List<LineChartPoint>,
    expenseData: List<LineChartPoint>,
    showIncome: Boolean = true,
    showExpense: Boolean = true,
    title: String = stringResource(id = R.string.chart_title_dynamics),
    subtitle: String = "",
    period: String = "",
    formatYValue: (Float) -> String = { value ->
        when {
            value >= 1000000 -> String.format("%.1fM", value / 1000000)
            value >= 1000 -> String.format("%.1fK", value / 1000)
            else -> value.roundToInt().toString()
        }
    },
    onPointSelected: (LineChartPoint) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Проверяем, есть ли данные для отображения
    val hasIncomeData = incomeData.isNotEmpty() && showIncome
    val hasExpenseData = expenseData.isNotEmpty() && showExpense

    // Если нет данных для отображения, показываем сообщение
    if (!hasIncomeData && !hasExpenseData) {
        Box(modifier = modifier.fillMaxWidth()) {
            EmptyContent(message = stringResource(id = R.string.chart_empty_line_data))
        }
        return
    }

    // Состояния для анимации и выбранной точки
    var selectedIncomePoint by remember { mutableStateOf<LineChartPoint?>(null) }
    var selectedExpensePoint by remember { mutableStateOf<LineChartPoint?>(null) }

    // Константа для порогового значения выбора точки
    val selectionThreshold = 25.dp

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )

    // Преобразуем selectionThreshold в пиксели заранее
    val thresholdPx = with(LocalDensity.current) { selectionThreshold.toPx() }

    // Найдем минимальные и максимальные значения для масштабирования
    val allPoints = mutableListOf<LineChartPoint>().apply {
        if (hasIncomeData) addAll(incomeData)
        if (hasExpenseData) addAll(expenseData)
    }

    // Если список пуст, возвращаемся
    if (allPoints.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth()) {
            EmptyContent(message = stringResource(id = R.string.chart_empty_line_data))
        }
        return
    }

    // Вычисляем максимальные и минимальные значения для каждого типа данных
    var maxIncomeValue = 0f
    var maxExpenseValue = 0f
    
    if (hasIncomeData) {
        maxIncomeValue = incomeData.maxOf { it.value.amount.toDouble() }.toFloat()
    }
    
    if (hasExpenseData) {
        maxExpenseValue = expenseData.maxOf { it.value.amount.toDouble() }.toFloat()
    }
    
    // Берем максимальное из всех значений для оси Y с запасом 10%
    val maxValue = Math.max(maxIncomeValue, maxExpenseValue) * 1.1f
    
    // Устанавливаем минимальное значение близко к нулю для лучшего восприятия
    val minValue = 0f

    // Логируем значения для отладки масштабирования
    Timber.d("EnhancedLineChart: диапазон значений по Y: $minValue - $maxValue")
    Timber.d("EnhancedLineChart: доходы - ${if (hasIncomeData) "${incomeData.size} точек" else "нет данных"}")
    Timber.d("EnhancedLineChart: расходы - ${if (hasExpenseData) "${expenseData.size} точек" else "нет данных"}")
    
    if (hasIncomeData) {
        val incomeMin = incomeData.minOf { it.value.amount.toDouble() }
        val incomeMax = incomeData.maxOf { it.value.amount.toDouble() }
        Timber.d("EnhancedLineChart: диапазон доходов: $incomeMin - $incomeMax")
        Timber.d("EnhancedLineChart: максимальное значение для доходов: $maxIncomeValue")
    }
    
    if (hasExpenseData) {
        val expenseMin = expenseData.minOf { it.value.amount.toDouble() }
        val expenseMax = expenseData.maxOf { it.value.amount.toDouble() }
        Timber.d("EnhancedLineChart: диапазон расходов: $expenseMin - $expenseMax")
        Timber.d("EnhancedLineChart: максимальное значение для расходов: $maxExpenseValue")
    }

    // Получаем крайние даты для оси X
    val startDate = allPoints.minOf { it.date.time }
    val endDate = allPoints.maxOf { it.date.time }

    // Сохраняем цвета поверхности для использования в функциях рисования
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val expenseColor = MaterialTheme.colorScheme.error
    val incomeColor = colorResource(id = R.color.income_primary)
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val axisLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    
    // Текстовый измеритель для осей
    val textMeasurer = rememberTextMeasurer()
    
    // Форматтер для дат
    val dateFormatter = SimpleDateFormat("dd MMM", Locale("ru"))
    
    // Получаем размеры из ресурсов
    val cardCornerRadius = dimensionResource(id = R.dimen.chart_card_corner_radius)
    val cardElevation = dimensionResource(id = R.dimen.chart_card_elevation)
    val chartCornerRadius = dimensionResource(id = R.dimen.chart_corner_radius)
    val chartHeight = dimensionResource(id = R.dimen.chart_height)
    val spacingNormal = dimensionResource(id = R.dimen.chart_spacing_normal)
    val spacingMedium = dimensionResource(id = R.dimen.chart_spacing_medium)

    // Создаем карточку с графиком
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardCornerRadius))
            .clickable {
                // При клике на пустую область сбрасываем выбор
                selectedIncomePoint = null
                selectedExpensePoint = null
                // Вызываем колбэк для сброса выбора
                onPointSelected(LineChartPoint(java.util.Date(), Money.zero(), ""))
            },
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacingNormal)
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

            Spacer(modifier = Modifier.height(spacingNormal))

            // Отображение выбранной точки
            val selectedPoint = selectedIncomePoint ?: selectedExpensePoint
            if (selectedPoint != null) {
                val dateFormatterFull = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacingMedium),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateFormatterFull.format(selectedPoint.date),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = selectedPoint.value.format(true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPoint == selectedIncomePoint) incomeColor else expenseColor
                    )
                }
            }

            // Область с графиком и осями
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight + 40.dp) // Увеличиваем высоту для осей и меток
                    .clip(RoundedCornerShape(chartCornerRadius))
                    .background(surfaceVariantColor.copy(alpha = 0.1f))
                    .padding(start = 40.dp, end = 10.dp, top = 20.dp, bottom = 25.dp) // Отступы для осей
            ) {
                // Отрисовка графика на холсте
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Обработка нажатия на точки графика
                                val chartWidth = size.width
                                val chartHeight = size.height

                                // Ищем ближайшие точки на обеих линиях
                                val incomePoint = if (hasIncomeData) findNearestPoint(
                                    points = incomeData,
                                    startDate = startDate,
                                    endDate = endDate,
                                    minValue = minValue,
                                    maxValue = maxValue,
                                    tapPosition = offset,
                                    chartWidth = chartWidth.toFloat(),
                                    chartHeight = chartHeight.toFloat(),
                                    threshold = thresholdPx
                                ) else null

                                val expensePoint = if (hasExpenseData) findNearestPoint(
                                    points = expenseData,
                                    startDate = startDate,
                                    endDate = endDate,
                                    minValue = minValue,
                                    maxValue = maxValue,
                                    tapPosition = offset,
                                    chartWidth = chartWidth.toFloat(),
                                    chartHeight = chartHeight.toFloat(),
                                    threshold = thresholdPx
                                ) else null

                                // Выбираем ближайшую из всех найденных
                                if (incomePoint != null && expensePoint != null) {
                                    // Если найдены точки на обеих линиях, выбираем ближайшую
                                    val incomeX = (incomePoint.date.time - startDate).toFloat() / (endDate - startDate).toFloat() * chartWidth
                                    val incomeY =
                                        (1f - (incomePoint.value.amount.toFloat() - minValue) / (maxValue - minValue).toFloat()) * chartHeight.toFloat()

                                    val expenseX = (expensePoint.date.time - startDate).toFloat() / (endDate - startDate).toFloat() * chartWidth
                                    val expenseY =
                                        (1f - (expensePoint.value.amount.toFloat() - minValue) / (maxValue - minValue).toFloat()) * chartHeight.toFloat()

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
                        .drawBehind {
                            // Рисуем оси X и Y с большей толщиной линий
                            drawLine(
                                color = axisColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2.5f
                            )
                            
                            drawLine(
                                color = axisColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 2.5f
                            )
                            
                            // Рисуем горизонтальные линии и метки на оси Y
                            val ySteps = 5
                            val valueRange = maxValue - minValue
                            
                            for (i in 0..ySteps) {
                                val y = size.height - (size.height / ySteps.toFloat() * i)
                                val value = minValue + (valueRange / ySteps.toFloat() * i)
                                
                                // Рисуем горизонтальную линию (более заметную)
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.0f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                                )
                                
                                // Форматируем значение для отображения
                                val formattedValue = formatYValue(value)
                                
                                // Рисуем метку значения (с лучшим контрастом)
                                val labelStyle = TextStyle(
                                    fontSize = 10.sp,
                                    color = axisLabelColor.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Рисуем текст слева от оси Y
                                val textLayoutResult = textMeasurer.measure(
                                    text = formattedValue,
                                    style = labelStyle
                                )
                                
                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(-textLayoutResult.size.width - 5f, y - textLayoutResult.size.height / 2)
                                )
                                
                                // Добавляем маленькие отметки на оси Y
                                drawLine(
                                    color = axisColor,
                                    start = Offset(-4f, y),
                                    end = Offset(0f, y),
                                    strokeWidth = 1.5f
                                )
                            }
                            
                            // Рисуем метки на оси X (даты) с улучшенной презентацией
                            val xSteps = 4
                            
                            for (i in 0..xSteps) {
                                val ratio = i.toFloat() / xSteps.toFloat()
                                val x = size.width * ratio
                                val date = java.util.Date(startDate + ((endDate - startDate) * ratio).toLong())
                                
                                // Форматируем дату
                                val formattedDate = dateFormatter.format(date)
                                
                                // Рисуем вертикальную линию (более заметную)
                                if (i > 0 && i < xSteps) { // Не рисуем для крайних точек
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = 1.0f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                                    )
                                }
                                
                                // Рисуем метку даты внизу с улучшенным стилем
                                val labelStyle = TextStyle(
                                    fontSize = 10.sp,
                                    color = axisLabelColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                val textLayoutResult = textMeasurer.measure(
                                    text = formattedDate,
                                    style = labelStyle
                                )
                                
                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(x - textLayoutResult.size.width / 2, size.height + 5f)
                                )
                                
                                // Добавляем маленькие отметки на оси X
                                drawLine(
                                    color = axisColor,
                                    start = Offset(x, size.height),
                                    end = Offset(x, size.height + 4f),
                                    strokeWidth = 1.5f
                                )
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // Отрисовка сетки
                    drawGridLines(width, height)

                    // Отрисовка линии доходов
                    if (hasIncomeData) {
                        drawLineChart(
                            points = incomeData,
                            startDate = startDate,
                            endDate = endDate,
                            minValue = minValue,
                            maxValue = maxValue,
                            lineColor = incomeColor,
                            fillColor = incomeColor.copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedIncomePoint
                        )
                    }

                    // Отрисовка линии расходов
                    if (hasExpenseData) {
                        drawLineChart(
                            points = expenseData,
                            startDate = startDate,
                            endDate = endDate,
                            minValue = minValue,
                            maxValue = maxValue,
                            lineColor = expenseColor,
                            fillColor = expenseColor.copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedExpensePoint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacingMedium))

            // Легенда для графика
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacingMedium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIncome) {
                    ChartLegendItem(
                        color = incomeColor,
                        text = stringResource(id = R.string.chart_title_income)
                    )

                    if (showExpense) {
                        Spacer(modifier = Modifier.width(spacingNormal))
                    }
                }

                if (showExpense) {
                    ChartLegendItem(
                        color = expenseColor,
                        text = stringResource(id = R.string.chart_title_expense)
                    )
                }
            }
        }
    }
} 