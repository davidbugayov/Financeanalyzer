package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Карточка для отображения совета или предупреждения.
 *
 * @param title Заголовок совета (строка или stringResource)
 * @param description Описание совета (строка или stringResource)
 * @param priority Приоритет (цветовая индикация)
 * @param modifier Модификатор
 */
@Composable
fun AdviceCard(
    title: String,
    description: String,
    priority: AdvicePriority = AdvicePriority.NORMAL,
    modifier: Modifier = Modifier,
) {
    val color =
        when (priority) {
            AdvicePriority.HIGH -> MaterialTheme.colorScheme.errorContainer
            AdvicePriority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
            AdvicePriority.NORMAL -> MaterialTheme.colorScheme.primaryContainer
        }
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .background(color)
                    .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

enum class AdvicePriority {
    HIGH,
    MEDIUM,
    NORMAL,
} 
