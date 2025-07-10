package com.davidbugayov.financeanalyzer.presentation.chart.detail.components

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
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.statistics.R
import com.davidbugayov.financeanalyzer.presentation.chart.detail.model.FinancialMetrics
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

/**
 * Улучшенная карточка рекомендаций с детальными советами и приоритизацией
 */
@Composable
fun RecommendationsCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier,
) {
    val recommendations = generateSmartRecommendations(metrics)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(R.dimen.financial_statistics_card_corner_radius)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.financial_statistics_card_elevation),
        ),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.financial_statistics_card_padding)),
        ) {
            // Заголовок с иконкой
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.smart_recommendations_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))

            Text(
                text = stringResource(R.string.expense_transactions_summary, metrics.expenseTransactionsCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))

            if (recommendations.isEmpty()) {
                Text(
                    text = stringResource(R.string.all_good_no_recommendations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recommendations.forEach { recommendation ->
                    EnhancedRecommendationItem(recommendation)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Общий призыв к действию
            if (recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.recommendations_cta),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Улучшенный элемент рекомендации
 */
@Composable
private fun EnhancedRecommendationItem(recommendation: SmartRecommendation) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = recommendation.priority.color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { /* Можно добавить навигацию */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка приоритета
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(recommendation.priority.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = recommendation.icon,
                        contentDescription = null,
                        tint = recommendation.priority.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recommendation.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        PriorityBadge(recommendation.priority)
                    }

                    Text(
                        text = recommendation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }

            if (recommendation.impact.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF38A169),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recommendation.impact,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38A169)
                    )
                }
            }
        }
    }
}

/**
 * Значок приоритета
 */
@Composable
private fun PriorityBadge(priority: RecommendationPriority) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(priority.color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = priority.color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Генерация умных рекомендаций на основе метрик
 */
@Composable
private fun generateSmartRecommendations(metrics: FinancialMetrics): List<SmartRecommendation> {
    val recommendations = mutableListOf<SmartRecommendation>()

    // Критические рекомендации
    if (metrics.savingsRate < 5f) {
        recommendations.add(
            SmartRecommendation(
                title = stringResource(R.string.critical_savings_title),
                description = stringResource(R.string.critical_savings_description),
                icon = Icons.Filled.Warning,
                priority = RecommendationPriority.CRITICAL,
                impact = stringResource(R.string.critical_savings_impact)
            )
        )
    }

    // Высокий приоритет
    if (metrics.savingsRate in 5f..10f) {
        recommendations.add(
            SmartRecommendation(
                title = stringResource(R.string.improve_savings_title),
                description = stringResource(R.string.improve_savings_description),
                icon = Icons.Filled.Savings,
                priority = RecommendationPriority.HIGH,
                impact = stringResource(R.string.improve_savings_impact)
            )
        )
    }

    // Рекомендации по категориям расходов
    if (metrics.topExpenseCategory.isNotEmpty()) {
        val topCategoryPercentage = metrics.expenseCategories
            .maxByOrNull { it.amount.amount }?.percentage?.toFloat() ?: 0f

        if (topCategoryPercentage > 35f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.optimize_category_title, metrics.topExpenseCategory),
                    description = stringResource(R.string.optimize_category_description, topCategoryPercentage.toInt()),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = RecommendationPriority.MEDIUM,
                    impact = stringResource(R.string.optimize_category_impact),
                ),
            )
        }
    }

    // Рекомендации по финансовой подушке
    if (metrics.monthsOfSavings < 3) {
        val priority = if (metrics.monthsOfSavings < 1) RecommendationPriority.HIGH else RecommendationPriority.MEDIUM
        recommendations.add(
            SmartRecommendation(
                title = stringResource(R.string.emergency_fund_recommendation_title),
                description = stringResource(R.string.emergency_fund_recommendation_description),
                icon = Icons.Filled.PriorityHigh,
                priority = priority,
                impact = stringResource(R.string.emergency_fund_recommendation_impact)
            )
        )
    }

    // Позитивные рекомендации для хороших показателей
    if (metrics.savingsRate > 20f) {
        recommendations.add(
            SmartRecommendation(
                title = stringResource(R.string.investment_suggestion_title),
                description = stringResource(R.string.investment_suggestion_description),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                priority = RecommendationPriority.LOW,
                impact = stringResource(R.string.investment_suggestion_impact),
            ),
        )
    }

    if (metrics.expenseTransactionsCount > 100) {
        recommendations.add(
            SmartRecommendation(
                title = stringResource(R.string.recommendation_reduce_small_expenses_title),
                description = stringResource(R.string.recommendation_reduce_small_expenses_description, metrics.expenseTransactionsCount),
                icon = Icons.Filled.PriorityHigh,
                priority = RecommendationPriority.MEDIUM,
                impact = stringResource(R.string.recommendation_reduce_small_expenses_impact)
            )
        )
    }

    return recommendations.sortedBy { it.priority.order }
}

/**
 * Данные для умной рекомендации
 */
data class SmartRecommendation(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val priority: RecommendationPriority,
    val impact: String
)

/**
 * Приоритет рекомендации
 */
enum class RecommendationPriority(
    val label: String,
    val color: Color,
    val order: Int
) {
    CRITICAL("Критично", Color(0xFFE53E3E), 0),
    HIGH("Высокий", Color(0xFFED8936), 1),
    MEDIUM("Средний", Color(0xFF4299E1), 2),
    LOW("Низкий", Color(0xFF38A169), 3)
}
