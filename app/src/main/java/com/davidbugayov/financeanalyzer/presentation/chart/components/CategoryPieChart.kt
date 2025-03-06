package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R

@Composable
fun CategoryPieChart(
    data: Map<String, Double>,
    isIncome: Boolean = false,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    val colors = if (isIncome) {
        listOf(
            Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
            Color(0xFF009688), Color(0xFF00BCD4), Color(0xFF03A9F4),
            Color(0xFF2196F3), Color(0xFF3F51B5), Color(0xFF673AB7)
        )
    } else {
        listOf(
            Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
            Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3),
            Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF795548)
        )
    }
    
    val colorMap = data.keys.mapIndexed { index, category ->
        category to colors[index % colors.size]
    }.toMap()
    
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .padding(16.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = minOf(canvasWidth, canvasHeight) / 2
                val center = Offset(canvasWidth / 2, canvasHeight / 2)
                
                var startAngle = 0f
                
                data.forEach { (category, value) ->
                    val sweepAngle = (value / total * 360).toFloat()
                    
                    // Рисуем сектор
                    drawArc(
                        color = colorMap[category] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    // Рисуем обводку
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        style = Stroke(width = 2f),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    startAngle += sweepAngle
                }
            }
        }
        
        // Легенда
        LazyColumn(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(data.entries.toList()) { (category, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colorMap[category] ?: Color.Gray)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = category,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", value)),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = String.format("(%.1f%%)", value / total * 100),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
} 