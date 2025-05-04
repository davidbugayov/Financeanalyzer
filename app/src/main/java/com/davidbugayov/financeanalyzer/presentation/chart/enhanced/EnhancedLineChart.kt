package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

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
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
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
 * @param onPointSelected Колбэк при выборе точки на графике (null если выбор сброшен)
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
    onPointSelected: (LineChartPoint?) -> Unit = {},
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
    val selectionThreshold = 30.dp

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

    // Получаем крайние даты для оси X, обрабатывая случай с одной точкой
    val (chartStartDate, chartEndDate) = remember(allPoints) {
        if (allPoints.isEmpty()) {
            System.currentTimeMillis() to System.currentTimeMillis()
        } else {
            val minTime = allPoints.minOf { it.date.time }
            val maxTime = allPoints.maxOf { it.date.time }
            if (minTime == maxTime) {
                // Для одной точки создаем диапазон +/- 1 день
                val calendar = Calendar.getInstance().apply { timeInMillis = minTime }
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 2) // +1 день от исходного
                val end = calendar.timeInMillis
                Timber.d("EnhancedLineChart: Рассчитан диапазон для одной точки: $start - $end")
                start to end
            } else {
                minTime to maxTime
            }
        }
    }

    // Сохраняем цвета поверхности для использования в функциях рисования
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    // Используем константы из ColorUtils
    val currentIncomeColor = Color(ColorUtils.INCOME_COLOR)
    val currentExpenseColor = Color(ColorUtils.EXPENSE_COLOR)
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val axisLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    
    // Находим последнюю видимую точку заранее в Composable контексте
    val lastVisiblePoint = remember(incomeData, expenseData, showIncome, showExpense) {
        val visiblePoints = mutableListOf<LineChartPoint>()
        if (showIncome && incomeData.isNotEmpty()) visiblePoints.addAll(incomeData)
        if (showExpense && expenseData.isNotEmpty()) visiblePoints.addAll(expenseData)
        visiblePoints.maxByOrNull { it.date.time }
    }

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
            .clip(RoundedCornerShape(cardCornerRadius)),
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
                        color = if (selectedPoint == selectedIncomePoint) currentIncomeColor else currentExpenseColor
                    )
                }
            }

            // Логируем состояние ПЕРЕД Box с Canvas
            Timber.d("Pre-Canvas: showIncome=$showIncome, showExpense=$showExpense")
            Timber.d("Pre-Canvas: hasIncomeData=$hasIncomeData, hasExpenseData=$hasExpenseData")
            Timber.d("Pre-Canvas: selectedIncomePoint=${selectedIncomePoint?.value?.amount}, selectedExpensePoint=${selectedExpensePoint?.value?.amount}")

            // Область с графиком и осями
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight + 40.dp) // Увеличиваем высоту для осей и меток
                    .clip(RoundedCornerShape(chartCornerRadius))
                    .background(surfaceVariantColor.copy(alpha = 0.1f))
                    .padding(start = 40.dp, end = 10.dp, top = 20.dp, bottom = 25.dp) // Отступы для осей
                    .pointerInput(hasIncomeData, hasExpenseData, chartStartDate, chartEndDate, minValue, maxValue, thresholdPx, animatedProgress) { // Перемещаем pointerInput сюда, ключи зависят от данных
                        Timber.d("Canvas pointerInput block entered.")
                        detectTapGestures { offset -> // offset теперь относительно Canvas
                            Timber.d("Tap detected in Canvas at $offset")
                            // Используем size.width и size.height из Canvas scope
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            // Проверка на выход за границы Canvas не нужна, т.к. жест на самом Canvas

                            val incomePoint = if (hasIncomeData) findNearestPoint(
                                points = incomeData,
                                startDate = chartStartDate,
                                endDate = chartEndDate,
                                minValue = minValue,
                                maxValue = maxValue,
                                tapPosition = offset, // Используем offset напрямую
                                chartWidth = canvasWidth.toFloat(), // Используем размер Canvas
                                chartHeight = canvasHeight.toFloat(), // Используем размер Canvas (конвертируем в Float)
                                threshold = thresholdPx,
                                animatedProgress = animatedProgress
                            ) else null

                            val expensePoint = if (hasExpenseData) findNearestPoint(
                                points = expenseData,
                                startDate = chartStartDate,
                                endDate = chartEndDate,
                                minValue = minValue,
                                maxValue = maxValue,
                                tapPosition = offset, // Используем offset напрямую
                                chartWidth = canvasWidth.toFloat(), // Используем размер Canvas
                                chartHeight = canvasHeight.toFloat(), // Используем размер Canvas (конвертируем в Float)
                                threshold = thresholdPx,
                                animatedProgress = animatedProgress
                            ) else null

                            Timber.d("findNearestPoint result - income: ${incomePoint?.value?.amount}, expense: ${expensePoint?.value?.amount}")

                            if (incomePoint != null && expensePoint != null) {
                                Timber.d("Tap Handler: Both points found.")
                                // Пересчитываем координаты для сравнения дистанций, используя размеры Canvas
                                val incomeX = (incomePoint.date.time - chartStartDate).toFloat() / (chartEndDate - chartStartDate).toFloat() * canvasWidth * animatedProgress
                                val incomeY = (1f - (incomePoint.value.amount.toFloat() - minValue) / (maxValue - minValue)) * canvasHeight.toFloat() // Конвертируем в Float

                                val expenseX = (expensePoint.date.time - chartStartDate).toFloat() / (chartEndDate - chartStartDate).toFloat() * canvasWidth * animatedProgress
                                val expenseY = (1f - (expensePoint.value.amount.toFloat() - minValue) / (maxValue - minValue)) * canvasHeight.toFloat() // Конвертируем в Float

                                val incomeDistance = kotlin.math.hypot(incomeX - offset.x, incomeY - offset.y)
                                val expenseDistance = kotlin.math.hypot(expenseX - offset.x, expenseY - offset.y)

                                Timber.d("Tap Handler: Income Dist=$incomeDistance, Expense Dist=$expenseDistance")

                                if (incomeDistance <= expenseDistance) { // Используем <= для предпочтения дохода при равных дистанциях
                                    selectedIncomePoint = incomePoint
                                    selectedExpensePoint = null
                                    onPointSelected(incomePoint)
                                } else {
                                    selectedIncomePoint = null
                                    selectedExpensePoint = expensePoint
                                    onPointSelected(expensePoint)
                                }
                            } else if (incomePoint != null) {
                                Timber.d("Tap Handler: Only income point found.")
                                selectedIncomePoint = incomePoint
                                selectedExpensePoint = null
                                onPointSelected(incomePoint)
                            } else if (expensePoint != null) {
                                Timber.d("Tap Handler: Only expense point found.")
                                selectedIncomePoint = null
                                selectedExpensePoint = expensePoint
                                onPointSelected(expensePoint)
                            } else {
                                Timber.d("Tap Handler: No point found nearby, clearing selection.")
                                if (selectedIncomePoint != null || selectedExpensePoint != null) {
                                    selectedIncomePoint = null
                                    selectedExpensePoint = null
                                    onPointSelected(null)
                                }
                            }
                        }
                        Timber.d("Canvas pointerInput block finished.")
                    }
            ) {
                // Логируем состояние ВНУТРИ Box ПЕРЕД Canvas
                Timber.d("In-Box Pre-Canvas: Current selectedIncomePoint=${selectedIncomePoint?.value?.amount}")
                
                // Отрисовка графика на холсте
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                         .drawBehind {
                             val width = size.width
                             val height = size.height

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
                                 val date = java.util.Date(chartStartDate + ((chartEndDate - chartStartDate) * ratio).toLong())
                                
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

                             // Отрисовка сетки
                             drawGridLines(width, height)
                         }
                ) {

                    // Отрисовка сетки (можно вернуть сюда, если drawBehind вызывает проблемы)
                    // drawGridLines(width, height)
                    
                    // Логируем ПЕРЕД отрисовкой линии доходов
                    Timber.d("Canvas Draw: Attempting Income Draw. hasIncomeData=$hasIncomeData")
                    Timber.d("Canvas Draw: Passed selectedIncomePoint=${selectedIncomePoint?.value?.amount}")

                    // Отрисовка линии доходов
                    if (hasIncomeData) {
                        Timber.d("Canvas Draw: Drawing Income Line...")
                        drawLineChart(
                            points = incomeData,
                            startDate = chartStartDate,
                            endDate = chartEndDate,
                            minValue = minValue,
                            maxValue = maxValue,
                            lineColor = currentIncomeColor,
                            fillColor = currentIncomeColor.copy(alpha = 0.2f),
                            animatedProgress = animatedProgress,
                            selectedPoint = selectedIncomePoint
                        )
                    }

                    // Логируем ПЕРЕД отрисовкой линии расходов
                    Timber.d("Canvas Draw: Attempting Expense Draw. hasExpenseData=$hasExpenseData")
                    Timber.d("Canvas Draw: Passed selectedExpensePoint=${selectedExpensePoint?.value?.amount}")
                    
                    // Отрисовка линии расходов
                    if (hasExpenseData) {
                        Timber.d("Canvas Draw: Drawing Expense Line...")
                        drawLineChart(
                            points = expenseData,
                            startDate = chartStartDate,
                            endDate = chartEndDate,
                            minValue = minValue,
                            maxValue = maxValue,
                            lineColor = currentExpenseColor,
                            fillColor = currentExpenseColor.copy(alpha = 0.2f),
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
                        color = currentIncomeColor,
                        text = stringResource(id = R.string.chart_title_income)
                    )

                    if (showExpense) {
                        Spacer(modifier = Modifier.width(spacingNormal))
                    }
                }

                if (showExpense) {
                    ChartLegendItem(
                        color = currentExpenseColor,
                        text = stringResource(id = R.string.chart_title_expense)
                    )
                }
            }
        }
    }

    // Отслеживаем изменения видимости и сбрасываем точки, которые не должны отображаться
    LaunchedEffect(showIncome, showExpense) {
        Timber.d("График: LaunchedEffect сработал. showIncome=$showIncome, showExpense=$showExpense")
        Timber.d("График: LaunchedEffect - ВХОДЯЩИЕ данные: income=${incomeData.size}, expense=${expenseData.size}")
        Timber.d("График: LaunchedEffect - ТЕКУЩЕЕ состояние: selectedIncome=${selectedIncomePoint?.value?.amount}, selectedExpense=${selectedExpensePoint?.value?.amount}")

        if (showIncome && !showExpense) {
            Timber.d("График: РЕЖИМ ДОХОДОВ активирован")
            
            if (incomeData.isEmpty()) {
                Timber.d("График: данные доходов пусты!")
            } else {
                Timber.d("График: данные доходов содержат ${incomeData.size} точек, первая: ${incomeData.first().date}, значение: ${incomeData.first().value.amount}")
            }
            
            if (selectedIncomePoint == null && incomeData.isNotEmpty()) {
                Timber.d("LaunchedEffect: Данные дохода есть, но точка не выбрана по умолчанию.") 
            } else if (selectedIncomePoint != null) {
                Timber.d("LaunchedEffect: Сохраняем выбранную точку дохода: ${selectedIncomePoint!!.date}, значение: ${selectedIncomePoint!!.value.amount}")
                onPointSelected(selectedIncomePoint!!)
            } else if (incomeData.isEmpty() && (selectedIncomePoint != null || selectedExpensePoint != null)) {
                 Timber.d("График: Данных дохода нет, сбрасываем любой активный выбор.")
                 selectedIncomePoint = null
                 selectedExpensePoint = null
                 onPointSelected(null)
            } else {
                Timber.d("График: НЕТ ТОЧКИ для выбора/сброса в режиме INCOME!")
            }
            
            if (selectedExpensePoint != null) {
                 Timber.d("LaunchedEffect: Сбрасываем точку расхода")
                 selectedExpensePoint = null
                 if (selectedIncomePoint == null) {
                    onPointSelected(null)
                 }
            }
        }
        
        if (showExpense && !showIncome) {
            Timber.d("График: РЕЖИМ РАСХОДОВ активирован")
             if (expenseData.isEmpty()) {
                Timber.d("График: данные расходов пусты!")
            } else {
                Timber.d("График: данные расходов содержат ${expenseData.size} точек")
            }

            if (selectedExpensePoint == null && expenseData.isNotEmpty()) {
                 Timber.d("LaunchedEffect: Данные расхода есть, но точка не выбрана по умолчанию.")
            } else if (selectedExpensePoint != null) {
                 Timber.d("LaunchedEffect: Сохраняем выбранную точку расхода: ${selectedExpensePoint!!.date}")
                 onPointSelected(selectedExpensePoint!!)
            } else if (expenseData.isEmpty() && (selectedIncomePoint != null || selectedExpensePoint != null)) {
                 Timber.d("График: Данных расхода нет, сбрасываем любой активный выбор.")
                 selectedIncomePoint = null
                 selectedExpensePoint = null
                 onPointSelected(null)
            } else {
                 Timber.d("График: НЕТ ТОЧКИ для выбора/сброса в режиме EXPENSE!")
            }

            if (selectedIncomePoint != null) {
                 Timber.d("LaunchedEffect: Сбрасываем точку дохода")
                 selectedIncomePoint = null
                 if (selectedExpensePoint == null) {
                     onPointSelected(null)
                 }
            }
        }
        
        if (showIncome && showExpense) {
             Timber.d("График: РЕЖИМ ОБА активирован")
            val pointToShow = when {
                selectedIncomePoint != null -> selectedIncomePoint
                selectedExpensePoint != null -> selectedExpensePoint
                else -> null
            }
            if (pointToShow != null) {
                 Timber.d("LaunchedEffect (Оба): Вызываем onPointSelected с ранее выбранной точкой: ${pointToShow.value.amount}")
                 onPointSelected(pointToShow)
            } else {
                 Timber.d("LaunchedEffect (Оба): Нет ранее выбранной точки для вызова onPointSelected")
            }
        }
        
        if (!showIncome && !showExpense) {
             Timber.d("График: РЕЖИМ НИЧЕГО не активирован, сброс точек")
             val wasSelected = selectedIncomePoint != null || selectedExpensePoint != null
             if (selectedIncomePoint != null) selectedIncomePoint = null
             if (selectedExpensePoint != null) selectedExpensePoint = null
             if (wasSelected) {
                 onPointSelected(null)
             }
             Timber.d("LaunchedEffect: точки сброшены, onPointSelected вызван с null = ${wasSelected}")
        }
        
        Timber.d("График: LaunchedEffect ЗАВЕРШЕН. selectedIncome=${selectedIncomePoint?.value?.amount}, selectedExpense=${selectedExpensePoint?.value?.amount}")
    }
} 