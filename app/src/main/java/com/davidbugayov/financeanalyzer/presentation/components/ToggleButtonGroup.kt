package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Группа кнопок переключения для выбора опций.
 *
 * @param options Список строк с названиями опций
 * @param selectedOptionIndex Индекс выбранной опции
 * @param onOptionSelected Callback при выборе опции
 * @param modifier Modifier для настройки внешнего вида
 */
@Composable
fun ToggleButtonGroup(
    options: List<String>,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedOptionIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onOptionSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 