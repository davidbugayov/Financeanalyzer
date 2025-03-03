package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel.MonthlyTransactionData

@Composable
fun MonthlyBarChart(
    data: Map<String, MonthlyTransactionData>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOfOrNull { 
        maxOf(it.totalIncome, it.totalExpense)
    } ?: 0.0
    
    val categoryColors = mapOf(
        "Продукты" to Color(0xFF4CAF50),
        "Транспорт" to Color(0xFF2196F3),
        "Развлечения" to Color(0xFFFFC107),
        "Здоровье" to Color(0xFFE91E63),
        "Одежда" to Color(0xFF9C27B0),
        "Рестораны" to Color(0xFF00BCD4),
        "Коммунальные платежи" to Color(0xFFFF5722),
        "Другое" to Color(0xFF795548)
    )

    Column(modifier = modifier) {
        // График
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = (size.width - 32.dp.toPx()) / data.size / 2 // Ширина для доходов/расходов
            val spacing = 4.dp.toPx()

            data.entries.forEachIndexed { index, (_, monthData) ->
                val x = index * (barWidth * 2 + spacing)

                // Столбец доходов
                val incomeHeight = (monthData.totalIncome / maxValue * size.height).toFloat()
                drawRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(x, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight)
                )

                // Столбец расходов с разбивкой по категориям
                var currentHeight = size.height
                monthData.categoryBreakdown.forEach { (category, amount) ->
                    val categoryHeight = (amount / maxValue * size.height).toFloat()
                    drawRect(
                        color = categoryColors[category] ?: Color.Gray,
                        topLeft = Offset(x + barWidth + spacing, currentHeight - categoryHeight),
                        size = Size(barWidth, categoryHeight)
                    )
                    currentHeight -= categoryHeight
                }
            }
        }

        // Легенда
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.keys.forEach { month ->
                Text(
                    text = month,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp)
                )
            }
        }

        // Легенда категорий
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Легенда:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(categoryColors.toList()) { (category, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = category, fontSize = 12.sp)
                }
            }
        }
    }
} 