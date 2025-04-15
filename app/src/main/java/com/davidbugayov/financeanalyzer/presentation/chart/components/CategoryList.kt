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
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_1
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_2
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_3
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_4
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_5
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_6
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_7
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_8
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_green_9
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_1
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_2
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_3
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_4
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_5
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_6
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_7
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_8
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_chart_red_9

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
            md_theme_chart_green_1, // Зеленый
            md_theme_chart_green_2,
            md_theme_chart_green_3,
            md_theme_chart_green_4,
            md_theme_chart_green_5,
            md_theme_chart_green_6,
            md_theme_chart_green_7,
            md_theme_chart_green_8,
            md_theme_chart_green_9
        )
    } else {
        listOf(
            md_theme_chart_red_1, // Красный
            md_theme_chart_red_2,
            md_theme_chart_red_3,
            md_theme_chart_red_4,
            md_theme_chart_red_5,
            md_theme_chart_red_6,
            md_theme_chart_red_7,
            md_theme_chart_red_8,
            md_theme_chart_red_9
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