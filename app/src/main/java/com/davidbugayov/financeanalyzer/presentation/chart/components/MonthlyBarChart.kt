package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartMonthlyData
import com.davidbugayov.financeanalyzer.util.formatNumber
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Столбчатая диаграмма для отображения доходов и расходов по месяцам.
 * Отображает два столбца для каждого месяца: доходы и расходы.
 * Включает легенду и подписи осей.
 *
 * @param data Карта с данными по месяцам, где ключ - название месяца, значение - данные о доходах и расходах
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun MonthlyBarChart(
    data: Map<String, ChartMonthlyData>,
    modifier: Modifier = Modifier
) {
    val maxAmount = data.values.maxOf { maxOf(it.totalIncome, it.totalExpense) }
    val barWidth = 24.dp
    val chartHeight = 200.dp
    val spaceBetweenBars = 24.dp
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
                val date = java.time.LocalDate.parse("$dateStr.$currentYear", fullDateFormatter)
                YearMonth.of(date.year, date.month)
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = barWidth / 2)
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
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            formatNumber(amount.toDouble()),
                            8.dp.toPx(),
                            y - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 10.sp.toPx()
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }
                }

                // Отрисовка столбцов
                data.values.toList().reversed().forEachIndexed { index, monthData ->
                    val x = index * barSpace + barWidthPx
                    
                    // Расходы (красный столбец)
                    val expenseHeight = (monthData.totalExpense / maxAmount * availableHeight).toFloat()
                    drawRect(
                        color = Color(0xFFF44336),
                        topLeft = Offset(x, availableHeight - expenseHeight),
                        size = Size(barWidthPx, expenseHeight)
                    )
                    
                    // Доходы (зеленый столбец)
                    val incomeHeight = (monthData.totalIncome / maxAmount * availableHeight).toFloat()
                    drawRect(
                        color = Color(0xFF4CAF50),
                        topLeft = Offset(x + barWidthPx + 2.dp.toPx(), availableHeight - incomeHeight),
                        size = Size(barWidthPx, incomeHeight)
                    )
                }
            }
        }
        
        // Легенда с датами
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.entries.toList().reversed().forEach { (monthStr, _) ->
                val yearMonth = parseDate(monthStr)
                val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
                Text(
                    text = "$monthName ${yearMonth.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Легенда типов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF4CAF50))
            )
            Text(
                text = stringResource(R.string.chart_income),
                modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFFF44336))
            )
            Text(
                text = stringResource(R.string.chart_expenses),
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 