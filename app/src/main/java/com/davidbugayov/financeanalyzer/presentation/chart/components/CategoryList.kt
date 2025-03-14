package com.davidbugayov.financeanalyzer.presentation.chart.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import java.util.Locale

/**
 * Displays a list of categories with their amounts and percentages.
 *
 * @param data Map of category names to money amounts
 * @param modifier Optional modifier for styling
 * @param isIncome Whether the data represents income (true) or expenses (false)
 */
@Composable
fun CategoryList(
    data: Map<String, Money>,
    modifier: Modifier = Modifier,
    isIncome: Boolean = false
) {
    if (data.isEmpty()) return

    // Calculate total amount
    val total = data.values.fold(0.0) { acc, money -> acc + money.amount.toDouble() }

    // Используем фиксированные цвета, как в CategoryPieChart
    val colors = if (isIncome) {
        listOf(
            Color(0xFF66BB6A), // Зеленый
            Color(0xFF81C784),
            Color(0xFF4CAF50),
            Color(0xFF2E7D32),
            Color(0xFF43A047),
            Color(0xFF388E3C),
            Color(0xFF1B5E20),
            Color(0xFF00C853),
            Color(0xFF00E676)
        )
    } else {
        listOf(
            Color(0xFFEF5350), // Красный
            Color(0xFFE57373),
            Color(0xFFEF9A9A),
            Color(0xFFD32F2F),
            Color(0xFFC62828),
            Color(0xFFB71C1C),
            Color(0xFFFF8A80),
            Color(0xFFFF5252),
            Color(0xFFFF1744)
        )
    }

    // Sort categories by amount (descending)
    val sortedData = data.entries.sortedByDescending { it.value.amount.toDouble() }

    Column(modifier = modifier) {
        sortedData.forEachIndexed { index, (category, amount) ->
            val percentage = amount.amount.toDouble() / total * 100

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Получаем цвет для текущей категории
                val categoryColor = colors[index % colors.size]
                
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Category name - используем цвет категории
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = categoryColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Amount - используем цвет категории
                Text(
                    text = amount.format(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = categoryColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage - используем цвет категории
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryColor
                )
            }

            if (index < sortedData.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
} 