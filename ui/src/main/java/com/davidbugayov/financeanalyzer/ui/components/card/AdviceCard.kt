package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    val unifiedRecommendation =
        RecommendationGenerator.convertAdviceToUnified(
            title = title,
            description = description,
            priority = priority,
        )

    UnifiedRecommendationCard(
        recommendations = listOf(unifiedRecommendation),
        title = "",
        cardStyle = RecommendationCardStyle.COMPACT,
        modifier = modifier,
    )
}

enum class AdvicePriority {
    HIGH,
    MEDIUM,
    NORMAL,
}
