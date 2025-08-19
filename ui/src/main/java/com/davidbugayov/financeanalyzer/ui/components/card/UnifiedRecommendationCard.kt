package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Унифицированная карточка рекомендаций для всех экранов приложения
 */
@Composable
fun UnifiedRecommendationCard(
    modifier: Modifier = Modifier,
    recommendations: List<UnifiedRecommendation>,
    title: String = stringResource(R.string.recommendations_title),
    titleIcon: ImageVector = Icons.Filled.Lightbulb,
    emptyStateText: String = stringResource(R.string.empty_state_text),
    cardStyle: RecommendationCardStyle = RecommendationCardStyle.DETAILED,
    onRecommendationClick: ((UnifiedRecommendation) -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Заголовок карточки
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = titleIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Контент карточки
            if (recommendations.isEmpty()) {
                Text(
                    text = emptyStateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                when (cardStyle) {
                    RecommendationCardStyle.SIMPLE -> {
                        SimpleRecommendationsList(
                            recommendations = recommendations,
                            onRecommendationClick = onRecommendationClick,
                        )
                    }
                    RecommendationCardStyle.DETAILED -> {
                        DetailedRecommendationsList(
                            recommendations = recommendations,
                            onRecommendationClick = onRecommendationClick,
                        )
                    }
                    RecommendationCardStyle.COMPACT -> {
                        CompactRecommendationsList(
                            recommendations = recommendations,
                            onRecommendationClick = onRecommendationClick,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Простой список рекомендаций (как в BudgetTip)
 */
@Composable
private fun SimpleRecommendationsList(
    recommendations: List<UnifiedRecommendation>,
    onRecommendationClick: ((UnifiedRecommendation) -> Unit)?,
) {
    recommendations.forEach { recommendation ->
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onRecommendationClick != null) {
                        onRecommendationClick?.invoke(recommendation)
                    }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = recommendation.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (recommendation.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Детальный список рекомендаций (как в RecommendationsCard)
 */
@Composable
private fun DetailedRecommendationsList(
    recommendations: List<UnifiedRecommendation>,
    onRecommendationClick: ((UnifiedRecommendation) -> Unit)?,
) {
    recommendations.forEachIndexed { index, recommendation ->
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = recommendation.priority.color.copy(alpha = 0.08f),
                ),
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onRecommendationClick != null) {
                        onRecommendationClick?.invoke(recommendation)
                    },
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Иконка с приоритетом
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(recommendation.priority.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = recommendation.icon,
                            contentDescription = null,
                            tint = recommendation.priority.color,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = recommendation.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PriorityBadge(recommendation.priority)
                        }

                        if (recommendation.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = recommendation.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (onRecommendationClick != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // Показать impact если есть
                if (recommendation.impact.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = recommendation.impact,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        if (index < recommendations.lastIndex) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Компактный список рекомендаций (как в ActionableTipsCard)
 */
@Composable
private fun CompactRecommendationsList(
    recommendations: List<UnifiedRecommendation>,
    onRecommendationClick: ((UnifiedRecommendation) -> Unit)?,
) {
    recommendations.forEachIndexed { index, recommendation ->
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onRecommendationClick != null) {
                        onRecommendationClick?.invoke(recommendation)
                    }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = stringResource(R.string.bullet_point),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text =
                    if (recommendation.description.isNotEmpty()) {
                        "${recommendation.title}: ${recommendation.description}"
                    } else {
                        recommendation.title
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (index < recommendations.lastIndex) {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Значок приоритета
 */
@Composable
private fun PriorityBadge(priority: UnifiedRecommendationPriority) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(priority.color.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = priority.color,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Унифицированная модель рекомендации
 */
data class UnifiedRecommendation(
    val title: String,
    val description: String = "",
    val icon: ImageVector,
    val priority: UnifiedRecommendationPriority = UnifiedRecommendationPriority.NORMAL,
    val impact: String = "",
    val category: String = "",
    val actionData: Any? = null,
)

/**
 * Унифицированные приоритеты рекомендаций
 */
enum class UnifiedRecommendationPriority(
    val label: String,
    val color: Color,
    val order: Int,
) {
    CRITICAL("Критично", Color(0xFFE53E3E), 0),
    HIGH("Высокий", Color(0xFFED8936), 1),
    MEDIUM("Средний", Color(0xFF4299E1), 2),
    NORMAL("Обычный", Color(0xFF38A169), 3),
    LOW("Низкий", Color(0xFF718096), 4),
}

/**
 * Стили отображения карточек рекомендаций
 */
enum class RecommendationCardStyle {
    SIMPLE, // Простые строки с иконками (как BudgetTip)
    DETAILED, // Детальные карточки с приоритетами (как RecommendationsCard)
    COMPACT, // Компактный список с буллетами (как ActionableTipsCard)
}
