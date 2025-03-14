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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.model.ChartMonthlyData

/**
 * Компонент для отображения расходов и доходов по дням в стиле CoinKeeper
 */
@Composable
fun DailyExpensesChart(
    data: Map<String, ChartMonthlyData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

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
        label = "DailyExpensesAnimation"
    )

    // Сортируем данные по дням
    val sortedData = data.entries.sortedBy { it.key }

    // Цвета для графика
    val incomeColor = Color(0xFF66BB6A) // Зеленый
    val expenseColor = Color(0xFFEF5350) // Красный
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
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
                    val barWidth = canvasWidth / (sortedData.size * 2) // 2 = столбец + отступ
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

                    // Рисуем столбцы
                    sortedData.forEachIndexed { index, (day, dayData) ->
                        val x = index * (barWidth * 2)

                        // Определяем цвет столбца в зависимости от типа (доход/расход)
                        val isIncome = dayData.totalIncome > dayData.totalExpense
                        val barColor = if (isIncome) incomeColor else expenseColor
                        val barAmount = if (isIncome) dayData.totalIncome else dayData.totalExpense

                        // Рисуем столбец
                        val barHeight = (barAmount.amount.toDouble() / maxAmount * canvasHeight * progress).toFloat()
                        val barRect = Offset(x + barWidth / 2, canvasHeight - barHeight)

                        drawRect(
                            color = barColor,
                            topLeft = barRect,
                            size = Size(barWidth, barHeight)
                        )

                        // Отображаем метку дня
                        drawContext.canvas.nativeCanvas.drawText(
                            day,
                            x + barWidth,
                            canvasHeight + 14.dp.toPx(),
                            Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 10.sp.toPx()
                                textAlign = Paint.Align.CENTER
                                alpha = 200
                            }
                        )

                        // Отображаем сумму над столбцом
                        if (barHeight > 30.dp.toPx()) {
                            drawContext.canvas.nativeCanvas.drawText(
                                barAmount.format(false),
                                x + barWidth,
                                canvasHeight - barHeight - 8.dp.toPx(),
                                Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 10.sp.toPx()
                                    textAlign = Paint.Align.CENTER
                                    alpha = 200
                                }
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
                            .size(8.dp)
                            .background(incomeColor)
                    )
                    Text(
                        text = stringResource(R.string.income),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Расходы
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(expenseColor)
                    )
                    Text(
                        text = stringResource(R.string.expense),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Подсказка
            Text(
                text = stringResource(R.string.tap_for_details),
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