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
 * DEPRECATED: Используйте UnifiedRecommendationCard для новых реализаций
 *
 * @param title Заголовок совета (строка или stringResource)
 * @param description Описание совета (строка или stringResource)
 * @param priority Приоритет (цветовая индикация)
 * @param modifier Модификатор
 */
@Deprecated("Используйте UnifiedRecommendationCard для новых реализаций")
@Composable
fun AdviceCard(
    title: String,
    description: String,
    priority: AdvicePriority = AdvicePriority.NORMAL,
    modifier: Modifier = Modifier,
) {
    // Используем новый унифицированный компонент
    val unifiedRecommendation = RecommendationGenerator.convertAdviceToUnified(
        title = title,
        description = description,
        priority = priority
    )
    
    UnifiedRecommendationCard(
        recommendations = listOf(unifiedRecommendation),
        title = "",
        cardStyle = RecommendationCardStyle.COMPACT,
        modifier = modifier
    )
}

enum class AdvicePriority {
    HIGH,
    MEDIUM,
    NORMAL,
} 
