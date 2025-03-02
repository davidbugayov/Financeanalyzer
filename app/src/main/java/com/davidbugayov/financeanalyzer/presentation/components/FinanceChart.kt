package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.FinanceData
import kotlin.math.max

@Composable
fun FinanceBarChart(
    financeData: FinanceData,
    modifier: Modifier = Modifier
) {
    val incomeColor = Color(0xFF4CAF50) // Зеленый
    val expenseColor = Color(0xFFF44336) // Красный
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Легенда
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = incomeColor, text = "Доходы")
            LegendItem(color = expenseColor, text = "Расходы")
        }
        
        // График
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {
            // Находим максимальное значение для масштабирования
            val maxValue = max(
                financeData.incomes.maxOrNull() ?: 0f,
                financeData.expenses.maxOrNull() ?: 0f
            ) * 1.2f // Добавляем 20% сверху для лучшего отображения
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (financeData.labels.size * 3) // 3 части: доход, расход, промежуток
                val chartHeight = size.height
                
                // Рисуем горизонтальные линии сетки
                drawGridLines(chartHeight, maxValue)
                
                // Рисуем бары
                financeData.labels.forEachIndexed { index, _ ->
                    val income = financeData.incomes.getOrNull(index) ?: 0f
                    val expense = financeData.expenses.getOrNull(index) ?: 0f
                    
                    val incomeHeight = (income / maxValue) * chartHeight
                    val expenseHeight = (expense / maxValue) * chartHeight
                    
                    val x = index * (barWidth * 3) + barWidth / 2
                    
                    // Доход (зеленый)
                    drawRect(
                        color = incomeColor,
                        topLeft = Offset(x, chartHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight)
                    )
                    
                    // Расход (красный)
                    drawRect(
                        color = expenseColor,
                        topLeft = Offset(x + barWidth, chartHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight)
                    )
                }
            }
            
            // Подписи по оси X (даты)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                financeData.labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }
        
        // Статистика
        FinanceStatistics(financeData = financeData)
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun DrawScope.drawGridLines(height: Float, maxValue: Float) {
    val gridLineCount = 5
    val gridLineSpacing = height / gridLineCount
    
    repeat(gridLineCount + 1) { i ->
        val y = i * gridLineSpacing
        
        // Рисуем горизонтальную линию
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
        
        // Подпись значения
        val value = maxValue * (gridLineCount - i) / gridLineCount
        // Подписи значений можно добавить отдельно через Text
    }
}

@Composable
fun FinanceStatistics(financeData: FinanceData) {
    val totalIncome = financeData.incomes.sum()
    val totalExpense = financeData.expenses.sum()
    val balance = totalIncome - totalExpense
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Общая статистика",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    title = "Доходы",
                    value = String.format("%.2f ₽", totalIncome),
                    color = Color(0xFF4CAF50)
                )
                
                StatItem(
                    title = "Расходы",
                    value = String.format("%.2f ₽", totalExpense),
                    color = Color(0xFFF44336)
                )
                
                StatItem(
                    title = "Баланс",
                    value = String.format("%.2f ₽", balance),
                    color = if (balance >= 0) Color(0xFF2196F3) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
} 