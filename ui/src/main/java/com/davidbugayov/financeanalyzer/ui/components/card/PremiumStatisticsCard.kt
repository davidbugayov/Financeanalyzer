package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.ui.R

/**
 * 📊 Премиум карточка статистики с профессиональным дизайном
 * Создана для отображения финансовой статистики в современном стиле
 */
@Composable
fun PremiumStatisticsCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    statistics: List<StatisticItem>,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter =
            slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(500, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(500)),
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        ),
                                ),
                        ).padding(20.dp),
            ) {
                // Заголовок с иконкой и градиентом
                PremiumStatisticsHeader(
                    title = title,
                    icon = icon,
                    accentColor = accentColor,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Статистические элементы
                StatisticsGrid(
                    statistics = statistics,
                    accentColor = accentColor,
                )
            }
        }
    }
}

/**
 * 🎨 Заголовок карточки с премиум дизайном
 */
@Composable
private fun PremiumStatisticsHeader(
    title: String,
    icon: ImageVector,
    accentColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Иконка с градиентным фоном
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        accentColor.copy(alpha = 0.2f),
                                        accentColor.copy(alpha = 0.05f),
                                    ),
                            ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Заголовок с улучшенной типографикой
        Column {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.detailed_analytics),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.8f),
            )
        }
    }
}

/**
 * 📈 Сетка статистических элементов
 */
@Composable
private fun StatisticsGrid(
    statistics: List<StatisticItem>,
    accentColor: Color,
) {
    // Группируем статистику в пары для красивого отображения
    val groupedStats = statistics.chunked(2)

    groupedStats.forEachIndexed { groupIndex, group ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            group.forEachIndexed { itemIndex, stat ->
                AnimatedStatisticItem(
                    statistic = stat,
                    accentColor = accentColor,
                    animationDelay = (groupIndex * 2 + itemIndex) * 100L,
                    modifier = Modifier.weight(1f),
                )
            }

            // Если в группе только один элемент, добавляем Spacer
            if (group.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (groupIndex < groupedStats.lastIndex) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ✨ Анимированный элемент статистики
 */
@Composable
private fun AnimatedStatisticItem(
    statistic: StatisticItem,
    accentColor: Color,
    animationDelay: Long,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
    )

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
            ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.alpha(alpha),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Иконка статистики (если есть)
            statistic.icon?.let { icon ->
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Значение
            Text(
                text = statistic.value,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color =
                    when (statistic.type) {
                        StatisticType.POSITIVE -> MaterialTheme.colorScheme.primary
                        StatisticType.NEGATIVE -> MaterialTheme.colorScheme.error
                        StatisticType.WARNING -> MaterialTheme.colorScheme.tertiary
                        StatisticType.NEUTRAL -> MaterialTheme.colorScheme.onSurface
                    },
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Название
            Text(
                text = statistic.label,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )

            // Дополнительная информация
            if (statistic.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = statistic.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 🔍 Премиум карточка инсайтов
 */
@Composable
fun PremiumInsightsCard(
    title: String,
    insights: List<InsightItem>,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter =
            slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(500, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(500)),
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        ),
                                ),
                        ).padding(20.dp),
            ) {
                // Заголовок инсайтов
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    brush =
                                        Brush.linearGradient(
                                            colors =
                                                listOf(
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                                                ),
                                        ),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp,
                                ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.smart_observations),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.8f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Список инсайтов
                insights.forEachIndexed { index, insight ->
                    AnimatedInsightItem(
                        insight = insight,
                        animationDelay = index * 150L,
                    )

                    if (index < insights.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * 💡 Анимированный элемент инсайта
 */
@Composable
private fun AnimatedInsightItem(
    insight: InsightItem,
    animationDelay: Long,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(alpha),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Индикатор важности
            Box(
                modifier =
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when (insight.importance) {
                                InsightImportance.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                InsightImportance.MEDIUM -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                InsightImportance.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = null,
                    tint =
                        when (insight.importance) {
                            InsightImportance.HIGH -> MaterialTheme.colorScheme.error
                            InsightImportance.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            InsightImportance.LOW -> MaterialTheme.colorScheme.primary
                        },
                    modifier = Modifier.size(14.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (insight.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insight.description,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Метрика инсайта
                if (insight.metric.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = insight.metric,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📊 Модель элемента статистики
 */
data class StatisticItem(
    val label: String,
    val value: String,
    val description: String = "",
    val icon: ImageVector? = null,
    val type: StatisticType = StatisticType.NEUTRAL,
)

/**
 * 🎯 Тип статистики для цветовой индикации
 */
enum class StatisticType {
    POSITIVE, // Зеленый - хорошие показатели
    NEGATIVE, // Красный - проблемные показатели
    WARNING, // Оранжевый - предупреждения
    NEUTRAL, // Обычный цвет
}

/**
 * 💡 Модель элемента инсайта
 */
data class InsightItem(
    val title: String,
    val description: String = "",
    val metric: String = "",
    val icon: ImageVector,
    val importance: InsightImportance = InsightImportance.MEDIUM,
)

/**
 * ⚡ Важность инсайта
 */
enum class InsightImportance {
    HIGH, // Высокая важность - красный
    MEDIUM, // Средняя важность - оранжевый
    LOW, // Низкая важность - синий
}
