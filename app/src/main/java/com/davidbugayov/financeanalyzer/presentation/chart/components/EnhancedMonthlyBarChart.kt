package com.davidbugayov.financeanalyzer.presentation.chart.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartMonthlyData
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Улучшенная столбчатая диаграмма с интерактивными элементами и анимацией.
 * Отображает доходы, расходы и баланс по месяцам.
 * Поддерживает выбор столбца для отображения детальной информации.
 *
 * @param data Карта с данными по месяцам
 * @param modifier Модификатор для настройки внешнего вида
 * @param showBalance Флаг для отображения линии баланса
 */
@Composable
fun EnhancedMonthlyBarChart(
    data: Map<String, ChartMonthlyData>,
    modifier: Modifier = Modifier,
    showBalance: Boolean = true
) {
    if (data.isEmpty()) return

    val maxAmount = data.values.maxOf {
        maxOf(
            it.totalIncome.amount.toDouble(),
            it.totalExpense.amount.toDouble()
        )
    } * 1.2 // Добавляем 20% для отступа сверху

    val barWidth = 28.dp
    val chartHeight = 250.dp
    val spaceBetweenBars = 40.dp

    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val balanceColor = MaterialTheme.colorScheme.tertiary

    // Анимация для появления столбцов
    val animatedProgress = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "BarChartAnimation"
    )

    // Состояние для выбранного месяца
    var selectedMonth by remember { mutableStateOf<String?>(null) }

    // Форматирование дат
    val monthYearFormatter = DateTimeFormatter.ofPattern("MM.yyyy")
    val fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun parseDate(dateStr: String): YearMonth {
        return try {
            // Пробуем формат MM.yyyy
            YearMonth.parse(dateStr, monthYearFormatter)
        } catch (e: Exception) {
            try {
                // Пробуем формат dd.MM.yyyy
                val date = java.time.LocalDate.parse(dateStr, fullDateFormatter)
                YearMonth.of(date.year, date.month)
            } catch (e: Exception) {
                // Пробуем формат dd.MM и добавляем текущий год
                val currentYear = YearMonth.now().year
                val monthDay = dateStr.split(".")
                if (monthDay.size >= 2) {
                    YearMonth.of(currentYear, monthDay[1].toInt())
                } else {
                    YearMonth.now()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Отображение детальной информации о выбранном месяце
        selectedMonth?.let { month ->
            data[month]?.let { monthData ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val yearMonth = parseDate(month)
                        val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("ru"))

                        Text(
                            text = "$monthName ${yearMonth.year}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.income),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = monthData.totalIncome.format(false),
                                    color = incomeColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.expense),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = monthData.totalExpense.format(false),
                                    color = expenseColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val balance = monthData.totalIncome - monthData.totalExpense
                        Text(
                            text = stringResource(R.string.balance),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Text(
                            text = balance.format(false),
                            color = if (balance.isNegative()) expenseColor else incomeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        // Топ категорий расходов
                        if (monthData.categoryBreakdown.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.top_expense_categories),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            monthData.categoryBreakdown.entries
                                .sortedByDescending { it.value.amount }
                                .take(3)
                                .forEach { (category, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = category,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = amount.format(false),
                                            color = expenseColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = barWidth / 2, vertical = 16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val availableWidth = size.width
                            val barSpace = spaceBetweenBars.toPx()
                            val barWidthPx = barWidth.toPx()

                            // Определяем, на какой столбец нажали
                            val sortedData = data.entries.sortedBy { it.key }
                            sortedData.forEachIndexed { index, (month, _) ->
                                val x = index * barSpace
                                val barArea = x + barWidthPx * 2 + 4.dp.toPx()

                                if (offset.x in x..barArea) {
                                    selectedMonth = if (selectedMonth == month) null else month
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val availableWidth = size.width
                val availableHeight = size.height
                val barSpace = spaceBetweenBars.toPx()
                val barWidthPx = barWidth.toPx()

                // Горизонтальные линии сетки
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = availableHeight * (1 - i.toFloat() / gridLines)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(availableWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Значения на оси Y
                    val amount = (maxAmount * i / gridLines)
                    val amountText = Money(amount).format(false)

                    drawContext.canvas.nativeCanvas.drawText(
                        amountText,
                        8.dp.toPx(),
                        y - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }

                // Подготовка данных для линии баланса
                val balancePoints = mutableListOf<Offset>()

                // Отрисовка столбцов
                val sortedData = data.entries.sortedBy { it.key }
                sortedData.forEachIndexed { index, (month, monthData) ->
                    val x = index * barSpace
                    val progress = animatedProgress.value

                    // Расходы (красный столбец)
                    val expenseHeight = (monthData.totalExpense.amount.toDouble() / maxAmount * availableHeight * progress).toFloat()
                    val expenseRect = Offset(x, availableHeight - expenseHeight)

                    drawRect(
                        color = if (month == selectedMonth) expenseColor else expenseColor.copy(alpha = 0.7f),
                        topLeft = expenseRect,
                        size = Size(barWidthPx, expenseHeight)
                    )

                    // Доходы (зеленый столбец)
                    val incomeHeight = (monthData.totalIncome.amount.toDouble() / maxAmount * availableHeight * progress).toFloat()
                    val incomeRect = Offset(x + barWidthPx + 4.dp.toPx(), availableHeight - incomeHeight)

                    drawRect(
                        color = if (month == selectedMonth) incomeColor else incomeColor.copy(alpha = 0.7f),
                        topLeft = incomeRect,
                        size = Size(barWidthPx, incomeHeight)
                    )

                    // Добавляем точку для линии баланса
                    val balance = monthData.totalIncome - monthData.totalExpense
                    val balanceHeight = (balance.amount.toDouble() / maxAmount * availableHeight * progress).toFloat()
                    val balancePoint = Offset(
                        x + barWidthPx + 2.dp.toPx(),
                        availableHeight - balanceHeight.coerceAtLeast(0f)
                    )
                    balancePoints.add(balancePoint)

                    // Отображаем метку месяца
                    val yearMonth = parseDate(month)
                    val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale("ru"))

                    drawContext.canvas.nativeCanvas.drawText(
                        monthName,
                        x + barWidthPx,
                        availableHeight + 14.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Рисуем линию баланса
                if (showBalance && balancePoints.size > 1) {
                    val path = Path()
                    path.moveTo(balancePoints.first().x, balancePoints.first().y)

                    for (i in 1 until balancePoints.size) {
                        path.lineTo(balancePoints[i].x, balancePoints[i].y)
                    }

                    drawPath(
                        path = path,
                        color = balanceColor,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Рисуем точки на линии баланса
                    balancePoints.forEach { point ->
                        drawCircle(
                            color = balanceColor,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }

        // Легенда типов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(incomeColor)
            )
            Text(
                text = stringResource(R.string.chart_income),
                modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall
            )

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(expenseColor)
            )
            Text(
                text = stringResource(R.string.chart_expenses),
                modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall
            )

            if (showBalance) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(balanceColor)
                )
                Text(
                    text = stringResource(R.string.balance),
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Подсказка
        Text(
            text = stringResource(R.string.tap_bar_for_details),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 