package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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

@Composable
fun MonthlyBarChart(
    data: Map<String, Double>,
    isExpense: Boolean = false,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOfOrNull { kotlin.math.abs(it) } ?: 0.0
    val barColor = if (isExpense) Color(0xFFF44336) else Color(0xFF2196F3)
    
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    bottom = 32.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / (data.size * 2)
            val maxBarHeight = canvasHeight * 0.8f
            
            // Рисуем горизонтальные линии сетки
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = canvasHeight - (canvasHeight * i / gridLines)
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
            }
            
            // Рисуем столбцы
            data.entries.forEachIndexed { index, (_, value) ->
                val normalizedValue = (kotlin.math.abs(value) / maxValue).toFloat()
                val barHeight = normalizedValue * maxBarHeight
                val x = index * (barWidth * 2) + barWidth / 2
                val y = canvasHeight - barHeight
                
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        
        // Подписи по оси X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.keys.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
} 