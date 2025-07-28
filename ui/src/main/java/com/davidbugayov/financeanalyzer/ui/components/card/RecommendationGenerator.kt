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
import com.davidbugayov.financeanalyzer.ui.util.StringProvider

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
                    title = StringProvider.criticalLowSavingsTitle,
                    description = StringProvider.criticalLowSavingsDescription,
                    icon = Icons.Filled.Warning,
                    priority = UnifiedRecommendationPriority.CRITICAL,
                    impact = StringProvider.criticalLowSavingsImpact,
                    category = "savings"
                )
            )
        }

        // Высокий приоритет
        if (savingsRate in 5f..10f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = StringProvider.improveSavingsRateTitle,
                    description = StringProvider.improveSavingsRateDescription,
                    icon = Icons.Filled.Savings,
                    priority = UnifiedRecommendationPriority.HIGH,
                    impact = StringProvider.improveSavingsRateImpact,
                    category = "savings"
                )
            )
        }

        // Рекомендации по категориям расходов
        if (topExpenseCategory.isNotEmpty() && topCategoryPercentage > 35f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = StringProvider.optimizeCategoryTitle(topExpenseCategory),
                    description = StringProvider.optimizeCategoryDescription(topCategoryPercentage.toInt()),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    impact = StringProvider.optimizeCategoryImpact,
                    category = "expenses"
                )
            )
        }

        // Рекомендации по финансовой подушке
        if (monthsOfSavings < 3) {
            val priority = if (monthsOfSavings < 1) UnifiedRecommendationPriority.HIGH else UnifiedRecommendationPriority.MEDIUM
            recommendations.add(
                UnifiedRecommendation(
                    title = StringProvider.createEmergencyFundTitleUi,
                    description = StringProvider.createEmergencyFundDescriptionUi,
                    icon = Icons.Filled.PriorityHigh,
                    priority = priority,
                    impact = StringProvider.createEmergencyFundImpact,
                    category = "emergency_fund"
                )
            )
        }

        // Позитивные рекомендации для хороших показателей
        if (savingsRate > 20f) {
            recommendations.add(
                UnifiedRecommendation(
                    title = StringProvider.considerInvestmentsTitle,
                    description = StringProvider.considerInvestmentsDescription,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.NORMAL,
                    impact = StringProvider.considerInvestmentsImpact,
                    category = "investments"
                )
            )
        }

        // Рекомендации по количеству транзакций
        if (expenseTransactionsCount > 100) {
            recommendations.add(
                UnifiedRecommendation(
                    title = StringProvider.manySmallExpensesTitle,
                    description = StringProvider.manySmallExpensesDescription(expenseTransactionsCount),
                    icon = Icons.Filled.PriorityHigh,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    impact = StringProvider.manySmallExpensesImpact,
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
                    title = "Увеличьте сбережения",
                    icon = Icons.Filled.Savings,
                    priority = UnifiedRecommendationPriority.HIGH,
                    category = "savings"
                )
            )
        }

        if (topExpenseCategory.isNotEmpty()) {
            tips.add(
                UnifiedRecommendation(
                    title = "Сократите расходы по категории $topExpenseCategory",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = UnifiedRecommendationPriority.MEDIUM,
                    category = "expenses"
                )
            )
        }

        if (monthsOfSavings < 3) {
            tips.add(
                UnifiedRecommendation(
                    title = "Создайте финансовую подушку",
                    icon = Icons.Filled.PriorityHigh,
                    priority = UnifiedRecommendationPriority.HIGH,
                    category = "emergency_fund"
                )
            )
        }

        if (mostFrequentExpenseDay.isNotEmpty()) {
            tips.add(
                UnifiedRecommendation(
                    title = "Планируйте покупки на ${mostFrequentExpenseDay.lowercase()}",
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
