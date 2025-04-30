package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.LineChartDisplayMode

/**
 * Селектор типа данных для линейного графика (доходы/расходы/оба)
 *
 * @param selectedMode Выбранный режим отображения
 * @param onModeSelected Колбэк для обработки выбора режима
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun LineChartTypeSelector(
    selectedMode: LineChartDisplayMode,
    onModeSelected: (LineChartDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка для отображения расходов
        SelectorButton(
            text = "Расходы",
            isSelected = selectedMode == LineChartDisplayMode.EXPENSE,
            color = MaterialTheme.colorScheme.error,
            onClick = { onModeSelected(LineChartDisplayMode.EXPENSE) }
        )

        SelectorButton(
            text = "Доходы",
            isSelected = selectedMode == LineChartDisplayMode.INCOME,
            color = MaterialTheme.colorScheme.primary,
            onClick = { onModeSelected(LineChartDisplayMode.INCOME) }
        )

        SelectorButton(
            text = "Оба",
            isSelected = selectedMode == LineChartDisplayMode.BOTH,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = { onModeSelected(LineChartDisplayMode.BOTH) }
        )
    }
}

/**
 * Кнопка для селектора типа графика
 */
@Composable
private fun SelectorButton(
    text: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 