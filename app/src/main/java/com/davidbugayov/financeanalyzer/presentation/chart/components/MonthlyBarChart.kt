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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.model.ChartMonthlyData
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
private fun ChartLegend(
    monthStr: String,
    monthYearFormatter: DateTimeFormatter,
    fullDateFormatter: DateTimeFormatter
) {
    fun parseDate(dateStr: String): YearMonth {
        return try {
            YearMonth.parse(dateStr, monthYearFormatter)
        } catch (e: Exception) {
            try {
                val date = java.time.LocalDate.parse(dateStr, fullDateFormatter)
                YearMonth.of(date.year, date.month)
            } catch (e: Exception) {
                val currentYear = YearMonth.now().year
                val date = java.time.LocalDate.parse("$dateStr.$currentYear", fullDateFormatter)
                YearMonth.of(date.year, date.month)
            }
        }
    }

    val yearMonth = parseDate(monthStr)
    val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
    Text(
        text = "$monthName ${yearMonth.year}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
fun MonthlyBarChart(
    data: Map<String, ChartMonthlyData>,
    modifier: Modifier = Modifier
) {
    val maxAmount = data.values.maxOf { maxOf(it.totalIncome.amount.toDouble(), it.totalExpense.amount.toDouble()) }
    val barWidth = dimensionResource(R.dimen.chart_bar_width)
    val spaceBetweenBars = dimensionResource(R.dimen.space_between_bars)
    val monthYearFormatter = DateTimeFormatter.ofPattern("MM.yyyy")
    val fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val density = LocalDensity.current
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary

    // Получаем значения размеров из ресурсов
    val spacingNormal = dimensionResource(R.dimen.spacing_normal)
    val chartHeight = dimensionResource(R.dimen.chart_height)
    val strokeThin = dimensionResource(R.dimen.stroke_thin)
    val spacingLarge = dimensionResource(R.dimen.spacing_large)
    val spacingSmall = dimensionResource(R.dimen.spacing_small)
    val spacingMedium = dimensionResource(R.dimen.spacing_medium)
    val spacingXXSmall = dimensionResource(R.dimen.spacing_xxsmall)
    val legendIconSize = dimensionResource(R.dimen.legend_icon_size)
    val chartAxisTextSize = dimensionResource(R.dimen.chart_axis_text_size)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacingNormal)
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
                val barWidthPx = with(density) { barWidth.toPx() }
                val barSpace = with(density) { spaceBetweenBars.toPx() }
                
                // Горизонтальные линии сетки
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = availableHeight * (1 - i.toFloat() / gridLines)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(availableWidth, y),
                        strokeWidth = with(density) { strokeThin.toPx() }
                    )
                    
                    // Значения на оси Y
                    val amount = (maxAmount * i / gridLines)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            Money(amount).format(false),
                            with(density) { spacingLarge.toPx() },
                            y - with(density) { spacingSmall.toPx() },
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = with(density) { chartAxisTextSize.toPx() }
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }
                }

                // Отрисовка столбцов
                data.values.toList().reversed().forEachIndexed { index, monthData ->
                    val x = index * barSpace + barWidthPx
                    
                    // Расходы (красный столбец)
                    val expenseHeight = (monthData.totalExpense.amount.toDouble() / maxAmount * availableHeight).toFloat()
                    drawRect(
                        color = errorColor,
                        topLeft = Offset(x, availableHeight - expenseHeight),
                        size = Size(barWidthPx, expenseHeight)
                    )
                    
                    // Доходы (зеленый столбец)
                    val incomeHeight = (monthData.totalIncome.amount.toDouble() / maxAmount * availableHeight).toFloat()
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(x + barWidthPx + with(density) { spacingXXSmall.toPx() }, availableHeight - incomeHeight),
                        size = Size(barWidthPx, incomeHeight)
                    )
                }
            }
        }
        
        // Легенда с датами
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacingLarge),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.entries.toList().reversed().forEach { (monthStr, _) ->
                ChartLegend(monthStr, monthYearFormatter, fullDateFormatter)
            }
        }
        
        // Легенда типов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacingSmall),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(legendIconSize)
                    .background(primaryColor)
            )
            Text(
                text = stringResource(R.string.chart_income),
                modifier = Modifier.padding(
                    start = spacingSmall,
                    end = spacingMedium
                ),
                style = MaterialTheme.typography.bodySmall
            )
            
            Box(
                modifier = Modifier
                    .size(legendIconSize)
                    .background(errorColor)
            )
            Text(
                text = stringResource(R.string.chart_expenses),
                modifier = Modifier.padding(start = spacingSmall),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 