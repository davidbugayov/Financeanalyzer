package com.davidbugayov.financeanalyzer.presentation.chart.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartMonthlyData

/**
 * Компонент для отображения сравнения по месяцам в стиле CoinKeeper
 */
@Composable
fun MonthlyComparisonChart(
    data: Map<String, ChartMonthlyData>,
    modifier: Modifier = Modifier,
    onMonthSelected: (String) -> Unit = {}
) {
    if (data.isEmpty()) return

    val density = LocalDensity.current

    // Находим максимальное значение для масштабирования
    val maxAmount = data.values.maxOf {
        maxOf(
            it.totalIncome.amount.toDouble(),
            it.totalExpense.amount.toDouble()
        )
    } * 1.2 // Добавляем 20% для отступа сверху

    // Анимация для появления
    val animatedProgress = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "MonthlyComparisonAnimation"
    )

    // Состояние для выбранного месяца
    var selectedMonth by remember { mutableStateOf<String?>(null) }

    // Сортируем данные по месяцам
    val sortedData = data.entries.sortedBy { it.key }

    // Цвета для графика
    val incomeColor = Color(0xFF66BB6A) // Зеленый
    val expenseColor = Color(0xFFEF5350) // Красный
    val balanceColor = MaterialTheme.colorScheme.onSurfaceVariant // Цвет текста на фоне
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Заголовок с информацией о выбранном месяце
            val currentSelectedMonth = selectedMonth
            if (currentSelectedMonth != null && data.containsKey(currentSelectedMonth)) {
                val selectedData = data[currentSelectedMonth]!!
                val balance = selectedData.totalIncome - selectedData.totalExpense

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentSelectedMonth,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Доходы
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = selectedData.totalIncome.format(false),
                                    color = incomeColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Доходы",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }

                            // Расходы
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = selectedData.totalExpense.format(false),
                                    color = expenseColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Расходы",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }

                            // Баланс
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = balance.format(false),
                                    color = balanceColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Баланс",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // График
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                        .clickable { /* Обработка клика по графику */ }
                ) {
                    // Сначала рисуем фон
                    drawRect(
                        color = backgroundColor,
                        size = size
                    )

                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = canvasWidth / (sortedData.size * 3) // 3 = 2 столбца + отступ
                    val progress = animatedProgress.value

                    // Рисуем горизонтальные линии сетки
                    val gridLines = 5
                    for (i in 0..gridLines) {
                        val y = canvasHeight * (1 - i.toFloat() / gridLines)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Значения на оси Y
                        val amount = (maxAmount * i / gridLines)
                        val amountText = Money(amount).format(false)

                        drawContext.canvas.nativeCanvas.drawText(
                            amountText,
                            8.dp.toPx(),
                            y - 4.dp.toPx(),
                            Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 10.sp.toPx()
                                textAlign = Paint.Align.LEFT
                                alpha = 150
                            }
                        )
                    }

                    // Подготовка данных для линии баланса
                    val balancePoints = mutableListOf<Offset>()

                    // Рисуем столбцы и собираем точки для линии баланса
                    sortedData.forEachIndexed { index, (month, monthData) ->
                        val x = index * (barWidth * 3)

                        // Расходы (красный столбец)
                        val expenseHeight = (monthData.totalExpense.amount.toDouble() / maxAmount * canvasHeight * progress).toFloat()
                        val expenseRect = Offset(x, canvasHeight - expenseHeight)

                        drawRect(
                            color = expenseColor.copy(alpha = if (month == selectedMonth) 1f else 0.7f),
                            topLeft = expenseRect,
                            size = Size(barWidth, expenseHeight)
                        )

                        // Доходы (зеленый столбец)
                        val incomeHeight = (monthData.totalIncome.amount.toDouble() / maxAmount * canvasHeight * progress).toFloat()
                        val incomeRect = Offset(x + barWidth + 4.dp.toPx(), canvasHeight - incomeHeight)

                        drawRect(
                            color = incomeColor.copy(alpha = if (month == selectedMonth) 1f else 0.7f),
                            topLeft = incomeRect,
                            size = Size(barWidth, incomeHeight)
                        )

                        // Добавляем точку для линии баланса
                        val balance = monthData.totalIncome - monthData.totalExpense
                        val balanceHeight = (balance.amount.toDouble() / maxAmount * canvasHeight * progress).toFloat()
                        val balancePoint = Offset(
                            x + barWidth + barWidth / 2,
                            canvasHeight - balanceHeight.coerceAtLeast(0f)
                        )
                        balancePoints.add(balancePoint)

                        // Отображаем метку месяца
                        drawContext.canvas.nativeCanvas.drawText(
                            month,
                            x + barWidth,
                            canvasHeight + 14.dp.toPx(),
                            Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 10.sp.toPx()
                                textAlign = Paint.Align.CENTER
                                alpha = 200
                            }
                        )
                    }

                    // Рисуем линию баланса
                    if (balancePoints.size > 1) {
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

            // Легенда
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Доходы
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(incomeColor)
                    )
                    Text(
                        text = "Доходы",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Расходы
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(expenseColor)
                    )
                    Text(
                        text = "Расходы",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Баланс
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(balanceColor)
                    )
                    Text(
                        text = "Баланс",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Подсказка
            Text(
                text = "Нажмите на столбец для подробностей",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
} 