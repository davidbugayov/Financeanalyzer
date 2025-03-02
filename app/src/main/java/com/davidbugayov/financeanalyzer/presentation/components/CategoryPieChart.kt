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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class CategoryData(
    val category: String,
    val amount: Float,
    val percentage: Float,
    val color: Color,
    val isExpense: Boolean
)

@Composable
fun CategoryPieChart(
    transactions: List<Transaction>,
    isExpenseView: Boolean = true,
    modifier: Modifier = Modifier
) {
    val categoryData = calculateCategoryData(transactions, isExpenseView)
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Заголовок
        Text(
            text = if (isExpenseView) "Расходы по категориям" else "Доходы по категориям",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (categoryData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isExpenseView) "Нет данных о расходах" else "Нет данных о доходах",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // Круговая диаграмма
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val radius = minOf(canvasWidth, canvasHeight) / 2.5f
                    val center = Offset(canvasWidth / 2, canvasHeight / 2)
                    
                    var startAngle = 0f
                    
                    categoryData.forEach { category ->
                        val sweepAngle = category.percentage * 360f
                        
                        // Рисуем сектор
                        drawArc(
                            color = category.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        
                        // Рисуем границу сектора
                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        startAngle += sweepAngle
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Легенда
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                categoryData.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(category.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.category,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format("%.2f ₽", category.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = String.format("%.1f%%", category.percentage * 100),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(60.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun calculateCategoryData(transactions: List<Transaction>, isExpenseView: Boolean): List<CategoryData> {
    // Фильтруем транзакции по типу (расходы или доходы)
    val filteredTransactions = transactions.filter { it.isExpense == isExpenseView }
    
    if (filteredTransactions.isEmpty()) return emptyList()
    
    // Группируем по категориям и суммируем
    val categoryAmounts = filteredTransactions
        .groupBy { it.category }
        .mapValues { (_, transactions) -> transactions.sumOf { it.amount }.toFloat() }
    
    val totalAmount = categoryAmounts.values.sum()
    
    // Создаем список цветов для категорий
    val colors = listOf(
        Color(0xFF4CAF50), // Зеленый
        Color(0xFF2196F3), // Синий
        Color(0xFFFFC107), // Желтый
        Color(0xFFE91E63), // Розовый
        Color(0xFF9C27B0), // Фиолетовый
        Color(0xFF00BCD4), // Голубой
        Color(0xFFFF5722), // Оранжевый
        Color(0xFF795548), // Коричневый
        Color(0xFF607D8B)  // Серо-синий
    )
    
    // Создаем данные для каждой категории
    return categoryAmounts.entries
        .sortedByDescending { it.value }
        .mapIndexed { index, (category, amount) ->
            CategoryData(
                category = category,
                amount = amount,
                percentage = amount / totalAmount,
                color = colors[index % colors.size],
                isExpense = isExpenseView
            )
        }
} 