package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
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
                    title = "Критически низкий уровень сбережений",
                    description = "Ваш уровень сбережений менее 5%. Это может привести к финансовым проблемам",
                    icon = Icons.Filled.Warning,
                    priority = UnifiedRecommendationPriority.CRITICAL,
                    impact = "Увеличение до 10% добавит финансовой стабильности",
                    category = "savings"
                )
            )
        }

        // Высокий приоритет
        if (savingsRate in 5f..10f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = "Улучшите норму сбережений",
                    description = "Стремитесь к сбережениям 15-20% от дохода для финансовой стабильности",
                    icon = Icons.Filled.Savings,
                    priority = UnifiedRecommendationPriority.HIGH,
                    impact = "Увеличение на 5% улучшит финансовое здоровье",
                    category = "savings"
                )
            )
        }

        // Рекомендации по категориям расходов
        if (topExpenseCategory.isNotEmpty() && topCategoryPercentage > 35f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = "Оптимизируйте категорию \"$topExpenseCategory\"",
                    description = "Эта категория составляет ${topCategoryPercentage.toInt()}% ваших расходов",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    impact = "Сокращение на 10% даст значительную экономию",
                    category = "expenses"
                )
            )
        }

        // Рекомендации по финансовой подушке
        if (monthsOfSavings < 3) {
            val priority = if (monthsOfSavings < 1) UnifiedRecommendationPriority.HIGH else UnifiedRecommendationPriority.MEDIUM
            recommendations.add(
                UnifiedRecommendation(
                    title = "Создайте финансовую подушку",
                    description = "Рекомендуется иметь 3-6 месяцев расходов в резерве",
                    icon = Icons.Filled.PriorityHigh,
                    priority = priority,
                    impact = "Защита от непредвиденных расходов",
                    category = "emergency_fund"
                )
            )
        }

        // Позитивные рекомендации для хороших показателей
        if (savingsRate > 20f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = "Рассмотрите инвестиции",
                    description = "У вас отличная норма сбережений! Время подумать об инвестициях",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.NORMAL,
                    impact = "Потенциальный рост капитала",
                    category = "investments"
                )
            )
        }

        // Рекомендации по количеству транзакций
        if (expenseTransactionsCount > 100) {
            recommendations.add(
                UnifiedRecommendation(
                    title = "Много мелких трат",
                    description = "У вас $expenseTransactionsCount расходных операций. Попробуйте их объединить",
                    icon = Icons.Filled.PriorityHigh,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    impact = "Упрощение контроля расходов",
                    category = "expenses"
                )
            )
        }

        return recommendations.sortedBy { it.priority.order }
    }

    /**
     * Генерация простых советов для основного экрана статистики
     */
    @Composable
    fun generateSimpleTips(
        savingsRate: Float,
        topExpenseCategory: String,
        monthsOfSavings: Float,
        mostFrequentExpenseDay: String,
    ): List<UnifiedRecommendation> {
        val tips = mutableListOf<UnifiedRecommendation>()

        if (savingsRate < 10f) {
            tips.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.increase_savings),
                    icon = Icons.Filled.Savings,
                    priority = UnifiedRecommendationPriority.HIGH,
                    category = "savings"
                )
            )
        }

        if (topExpenseCategory.isNotEmpty()) {
            tips.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.reduce_expenses_category, topExpenseCategory),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    category = "expenses"
                )
            )
        }

        if (monthsOfSavings < 3) {
            tips.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.create_emergency_fund),
                    icon = Icons.Filled.PriorityHigh,
                    priority = UnifiedRecommendationPriority.HIGH,
                    category = "emergency_fund"
                )
            )
        }

        if (mostFrequentExpenseDay.isNotEmpty()) {
            tips.add(
                UnifiedRecommendation(
                    title = stringResource(R.string.plan_purchases, mostFrequentExpenseDay.lowercase()),
                    icon = Icons.Filled.Lightbulb,
                    priority = UnifiedRecommendationPriority.NORMAL,
                    category = "planning"
                )
            )
        }

        return tips
    }
    
    /**
     * Конвертация старых AdviceCard рекомендаций в унифицированный формат
     */
    fun convertAdviceToUnified(
        title: String,
        description: String,
        priority: AdvicePriority,
    ): UnifiedRecommendation {
        val unifiedPriority = when (priority) {
            AdvicePriority.HIGH -> UnifiedRecommendationPriority.HIGH
            AdvicePriority.MEDIUM -> UnifiedRecommendationPriority.MEDIUM
            AdvicePriority.NORMAL -> UnifiedRecommendationPriority.NORMAL
        }

        return UnifiedRecommendation(
            title = title,
            description = description,
            icon = Icons.Filled.Lightbulb,
            priority = unifiedPriority,
            category = "general"
        )
    }
}
