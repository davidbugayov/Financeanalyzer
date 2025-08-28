package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Генератор унифицированных рекомендаций на основе финансовых метрик
 */
object RecommendationGenerator {
    /**
     * Генерация умных рекомендаций для детального экрана статистики
     */
    @Composable
    fun generateDetailedRecommendations(
        savingsRate: Float,
        monthsOfSavings: Float,
        topExpenseCategory: String,
        topCategoryPercentage: Float,
        expenseTransactionsCount: Int,
    ): List<UnifiedRecommendation> {
        val recommendations = mutableListOf<UnifiedRecommendation>()

        // Критические рекомендации
        if (savingsRate < 5f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.critical_low_savings_title),
                    description = stringResource(R.string.critical_low_savings_description),
                    icon = Icons.Filled.Warning,
                    priority = UnifiedRecommendationPriority.CRITICAL,
                    impact = stringResource(R.string.critical_low_savings_impact),
                    category = stringResource(R.string.recommendation_category_savings),
                ),
            )
        }

        // Высокий приоритет
        if (savingsRate in 5f..10f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.improve_savings_rate_title),
                    description = stringResource(R.string.improve_savings_rate_description),
                    icon = Icons.Filled.Savings,
                    priority = UnifiedRecommendationPriority.HIGH,
                    impact = stringResource(R.string.improve_savings_rate_impact),
                    category = stringResource(R.string.recommendation_category_savings),
                ),
            )
        }

        // Рекомендации по категориям расходов
        if (topExpenseCategory.isNotEmpty() && topCategoryPercentage > 35f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.optimize_category_title, topExpenseCategory),
                    description = stringResource(R.string.optimize_category_description, topCategoryPercentage.toInt()),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    impact = stringResource(R.string.optimize_category_impact),
                    category = stringResource(R.string.recommendation_category_expenses),
                ),
            )
        }

        // Рекомендации по финансовой подушке
        if (monthsOfSavings < 3) {
            val priority =
                if (monthsOfSavings <
                    1
                ) {
                    UnifiedRecommendationPriority.HIGH
                } else {
                    UnifiedRecommendationPriority.MEDIUM
                }
            recommendations.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.create_emergency_fund_title_ui),
                    description = stringResource(R.string.create_emergency_fund_description_ui),
                    icon = Icons.Filled.PriorityHigh,
                    priority = priority,
                    impact = stringResource(R.string.create_emergency_fund_impact),
                    category = stringResource(R.string.recommendation_category_emergency_fund),
                ),
            )
        }

        // Позитивные рекомендации для хороших показателей
        if (savingsRate > 20f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.consider_investments_title),
                    description = stringResource(R.string.consider_investments_description),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.NORMAL,
                    impact = stringResource(R.string.consider_investments_impact),
                    category = stringResource(R.string.recommendation_category_investments),
                ),
            )
        }

        // Рекомендации по количеству транзакций
        if (expenseTransactionsCount > 100) {
            recommendations.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.many_small_expenses_title),
                    description =
                        pluralStringResource(
                            R.plurals.many_small_expenses_description,
                            expenseTransactionsCount,
                            expenseTransactionsCount,
                        ),
                    icon = Icons.Filled.PriorityHigh,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    impact = stringResource(R.string.many_small_expenses_impact),
                    category = stringResource(R.string.recommendation_category_expenses),
                ),
            )
        }

        return recommendations.sortedBy { it.priority.order }
    }

    /**
     * Конвертация старых AdviceCard рекомендаций в унифицированный формат
     */
    @Composable
    fun convertAdviceToUnified(
        title: String,
        description: String,
        priority: AdvicePriority,
    ): UnifiedRecommendation {
        val unifiedPriority =
            when (priority) {
                AdvicePriority.HIGH -> UnifiedRecommendationPriority.HIGH
                AdvicePriority.MEDIUM -> UnifiedRecommendationPriority.MEDIUM
                AdvicePriority.NORMAL -> UnifiedRecommendationPriority.NORMAL
            }

        return UnifiedRecommendation(
            title = title,
            description = description,
            icon = Icons.Filled.Lightbulb,
            priority = unifiedPriority,
            category = stringResource(R.string.recommendation_category_general),
        )
    }
}
