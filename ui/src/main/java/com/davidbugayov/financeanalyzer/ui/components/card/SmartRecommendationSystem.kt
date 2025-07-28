package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.ui.util.StringProvider

/**
 * 🎯 Профессиональная система умных рекомендаций
 * Создана с учетом UX/UI принципов и поддержкой темной/светлой темы
 */
@Composable
fun SmartRecommendationCard(
    recommendations: List<SmartRecommendation>,
    title: String = StringProvider.smartCardDefaultTitle,
    subtitle: String = StringProvider.smartCardDefaultSubtitle,
    style: SmartCardStyle = SmartCardStyle.ENHANCED,
    showPriorityIndicator: Boolean = true,
    onRecommendationClick: ((SmartRecommendation) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val sortedRecommendations = recommendations.sortedBy { it.priority.order }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Заголовок с улучшенной типографикой
            SmartCardHeader(
                title = title,
                subtitle = subtitle,
                onDismiss = onDismiss
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Контент рекомендаций
            when (style) {
                SmartCardStyle.MINIMAL -> {
                    MinimalRecommendationsList(
                        recommendations = sortedRecommendations,
                        onRecommendationClick = onRecommendationClick
                    )
                }
                SmartCardStyle.ENHANCED -> {
                    EnhancedRecommendationsList(
                        recommendations = sortedRecommendations,
                        showPriorityIndicator = showPriorityIndicator,
                        onRecommendationClick = onRecommendationClick
                    )
                }
                SmartCardStyle.COMPACT -> {
                    CompactRecommendationsList(
                        recommendations = sortedRecommendations,
                        onRecommendationClick = onRecommendationClick
                    )
                }
            }

            // Показываем статистику если есть рекомендации
            if (recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                RecommendationStats(recommendations = recommendations)
            }
        }
    }
}

/**
 * 🎨 Заголовок карточки с улучшенным дизайном
 */
@Composable
private fun SmartCardHeader(
    title: String,
    subtitle: String,
    onDismiss: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.8f)
            )
        }

        if (onDismiss != null) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = StringProvider.closeButton,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 🎯 Минимальный стиль (как HomeTipsCard)
 */
@Composable
private fun MinimalRecommendationsList(
    recommendations: List<SmartRecommendation>,
    onRecommendationClick: ((SmartRecommendation) -> Unit)?
) {
    recommendations.forEach { recommendation ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onRecommendationClick != null) {
                    onRecommendationClick?.invoke(recommendation)
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка с анимацией
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = tween(150)
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                recommendation.priority.color.copy(alpha = 0.2f),
                                recommendation.priority.color.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = recommendation.icon,
                    contentDescription = null,
                    tint = recommendation.priority.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recommendation.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }
        }
    }
}

/**
 * ✨ Улучшенный стиль с приоритетами
 */
@Composable
private fun EnhancedRecommendationsList(
    recommendations: List<SmartRecommendation>,
    showPriorityIndicator: Boolean,
    onRecommendationClick: ((SmartRecommendation) -> Unit)?
) {
    recommendations.forEachIndexed { index, recommendation ->
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(index * 100L) // Плавное появление
            isVisible = true
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onRecommendationClick != null) {
                        onRecommendationClick?.invoke(recommendation)
                    }
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Верхняя часть - компактная компоновка
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Компактная иконка
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            recommendation.priority.color.copy(alpha = 0.2f),
                                            recommendation.priority.color.copy(alpha = 0.05f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = recommendation.icon,
                                contentDescription = null,
                                tint = recommendation.priority.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Контент с ограниченной шириной
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Заголовок с приоритетом в одной строке
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = recommendation.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 2
                                )

                                if (showPriorityIndicator) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ModernPriorityBadge(recommendation.priority)
                                }
                            }

                            // Описание с ограничением строк
                            if (recommendation.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = recommendation.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        lineHeight = 18.sp,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )
                            }
                        }

                        // Стрелка справа (если кликабельно)
                        if (onRecommendationClick != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Карточка воздействия (компактная)
                    if (recommendation.impact.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = recommendation.impact,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 📝 Компактный стиль (как ActionableTipsCard)
 */
@Composable
private fun CompactRecommendationsList(
    recommendations: List<SmartRecommendation>,
    onRecommendationClick: ((SmartRecommendation) -> Unit)?
) {
    recommendations.forEachIndexed { index, recommendation ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onRecommendationClick != null) {
                    onRecommendationClick?.invoke(recommendation)
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Современная bullet точка
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(recommendation.priority.color)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (recommendation.description.isNotEmpty()) {
                    "${recommendation.title}: ${recommendation.description}"
                } else {
                    recommendation.title
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 🏷️ Компактный современный значок приоритета
 */
@Composable
private fun ModernPriorityBadge(priority: SmartRecommendationPriority) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = priority.color.copy(alpha = 0.15f),
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = priority.color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Text(
            text = priority.emoji,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = priority.color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

/**
 * 📊 Статистика рекомендаций
 */
@Composable
private fun RecommendationStats(recommendations: List<SmartRecommendation>) {
    val criticalCount = recommendations.count { it.priority == SmartRecommendationPriority.CRITICAL }
    val highCount = recommendations.count { it.priority == SmartRecommendationPriority.HIGH }

    if (criticalCount > 0 || highCount > 0) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildString {
                        if (criticalCount > 0) append(StringProvider.criticalCountRecommendations(criticalCount))
                        if (criticalCount > 0 && highCount > 0) append(", ")
                        if (highCount > 0) append(StringProvider.importantCountRecommendations(highCount))
                        append(StringProvider.recommendationsRequireAttention)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 🎨 Стили карточек
 */
enum class SmartCardStyle {
    MINIMAL,    // Минимальный стиль как HomeTipsCard
    ENHANCED,   // Улучшенный с анимациями и приоритетами
    COMPACT     // Компактный список
}

/**
 * 📋 Модель умной рекомендации
 */
data class SmartRecommendation(
    val title: String,
    val description: String = "",
    val icon: ImageVector,
    val priority: SmartRecommendationPriority = SmartRecommendationPriority.NORMAL,
    val impact: String = "",
    val category: RecommendationCategory = RecommendationCategory.GENERAL,
    val actionData: Any? = null,
)

/**
 * ⭐ Приоритеты с эмодзи для лучшего UX
 */
enum class SmartRecommendationPriority(
    val label: String,
    val emoji: String,
    val color: Color,
    val order: Int,
) {
    CRITICAL("Критично", "🚨", Color(0xFFE53E3E), 0),
    HIGH("Важно", "⚠️", Color(0xFFED8936), 1),
    MEDIUM("Стоит рассмотреть", "💡", Color(0xFF3182CE), 2),
    NORMAL("Рекомендуем", "✅", Color(0xFF38A169), 3),
    LOW("На заметку", "💭", Color(0xFF718096), 4),
}

/**
 * 🏷️ Категории рекомендаций
 */
enum class RecommendationCategory(val label: String) {
    SAVINGS("Сбережения"),
    EXPENSES("Расходы"),
    BUDGETING("Бюджетирование"),
    INVESTMENTS("Инвестиции"),
    EMERGENCY_FUND("Финансовая подушка"),
    HABITS("Привычки"),
    GENERAL("Общие")
}
