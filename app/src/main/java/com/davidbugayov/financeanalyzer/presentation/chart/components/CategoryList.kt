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
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
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

    // Generate colors for each category
    val colors = generateCategoryColors(data.size, isIncome)

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
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Category name
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Amount
                Text(
                    text = amount.format(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isIncome) LocalIncomeColor.current else LocalExpenseColor.current
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < sortedData.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Generates a list of colors for the category list.
 *
 * @param count Number of colors needed
 * @param isIncome Whether the data represents income (true) or expenses (false)
 * @return List of colors
 */
private fun generateCategoryColors(count: Int, isIncome: Boolean): List<Color> {
    val baseColor = if (isIncome) {
        Color(0xFF66BB6A) // Green for income
    } else {
        Color(0xFFEF5350) // Red for expenses
    }

    return List(count) { index ->
        val hue = (baseColor.hue + index * 30f) % 360f
        Color.hsv(
            hue = hue,
            saturation = 0.7f,
            value = 0.9f
        )
    }
}

/**
 * Extension function to get the hue component of a Color.
 */
private val Color.hue: Float
    get() {
        val min = minOf(red, green, blue)
        val max = maxOf(red, green, blue)

        if (min == max) return 0f

        val hue = when (max) {
            red -> (green - blue) / (max - min) * 60f
            green -> (blue - red) / (max - min) * 60f + 120f
            blue -> (red - green) / (max - min) * 60f + 240f
            else -> 0f
        }

        return (hue + 360f) % 360f
    }

/**
 * Creates a color from HSV values.
 */
private fun Color.Companion.hsv(hue: Float, saturation: Float, value: Float): Color {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
    val m = value - c

    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = r + m,
        green = g + m,
        blue = b + m,
        alpha = 1f
    )
} 