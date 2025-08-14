package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

/**
 * 🧠 Умный генератор персональных финансовых рекомендаций
 * Создан профессиональным маркетологом с учетом психологии потребителей
 */
object SmartRecommendationGenerator {
    /**
     * 🎯 Генерация критически важных рекомендаций для финансового здоровья
     */
    @Composable
    fun generateCriticalFinancialRecommendations(
        savingsRate: Float,
        monthsOfEmergencyFund: Float,
        debtToIncomeRatio: Float = 0f,
        topExpenseCategory: String = "",
        topCategoryPercentage: Float = 0f,
        totalTransactions: Int = 0,
        unusualSpendingDetected: Boolean = false,
    ): List<SmartRecommendation> {
        val recommendations = mutableListOf<SmartRecommendation>()

        // 🚨 КРИТИЧЕСКИЕ рекомендации (требуют немедленного внимания)

        // Отсутствие финансовой подушки
        if (monthsOfEmergencyFund < 1f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_critical_emergency_title),
                    description = stringResource(R.string.rec_critical_emergency_desc),
                    icon = Icons.Default.Warning,
                    priority = SmartRecommendationPriority.CRITICAL,
                    impact = stringResource(R.string.rec_critical_emergency_impact),
                    category = RecommendationCategory.EMERGENCY_FUND,
                ),
            )
        }

        // Критически низкие сбережения
        if (savingsRate < 5f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_critical_savings_title),
                    description = stringResource(R.string.rec_critical_savings_desc),
                    icon = Icons.Default.PriorityHigh,
                    priority = SmartRecommendationPriority.CRITICAL,
                    impact = stringResource(R.string.rec_critical_savings_impact),
                    category = RecommendationCategory.SAVINGS,
                ),
            )
        }

        // Высокая долговая нагрузка
        if (debtToIncomeRatio > 0.4f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_critical_debt_title),
                    description = stringResource(R.string.rec_critical_debt_desc),
                    icon = Icons.Default.Error,
                    priority = SmartRecommendationPriority.CRITICAL,
                    impact = stringResource(R.string.rec_critical_debt_impact, (debtToIncomeRatio * 100).toInt()),
                    category = RecommendationCategory.EXPENSES,
                ),
            )
        }

        // ⚠️ ВАЖНЫЕ рекомендации (требуют внимания в ближайшее время)

        // Малая финансовая подушка
        if (monthsOfEmergencyFund in 1f..3f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_high_emergency_title),
                    description = stringResource(R.string.rec_high_emergency_desc, monthsOfEmergencyFund.toInt()),
                    icon = Icons.Default.Savings,
                    priority = SmartRecommendationPriority.HIGH,
                    impact = stringResource(R.string.rec_high_emergency_impact),
                    category = RecommendationCategory.EMERGENCY_FUND,
                ),
            )
        }

        // Низкие сбережения
        if (savingsRate in 5f..15f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_high_savings_title),
                    description = stringResource(R.string.rec_high_savings_desc, savingsRate.toInt()),
                    icon = Icons.Default.AccountBalance,
                    priority = SmartRecommendationPriority.HIGH,
                    impact = stringResource(R.string.rec_high_savings_impact),
                    category = RecommendationCategory.SAVINGS,
                ),
            )
        }

        // Концентрация расходов в одной категории
        if (topExpenseCategory.isNotEmpty() && topCategoryPercentage > 40f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_high_expense_title, topExpenseCategory),
                    description = stringResource(R.string.rec_high_expense_desc, topCategoryPercentage.toInt()),
                    icon = Icons.Default.PieChart,
                    priority = SmartRecommendationPriority.HIGH,
                    impact = stringResource(R.string.rec_high_expense_impact),
                    category = RecommendationCategory.EXPENSES,
                ),
            )
        }

        // 💡 СРЕДНИЕ рекомендации (стоит рассмотреть)

        // Улучшение привычек трат
        if (totalTransactions > 150) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_medium_habits_title),
                    description = stringResource(R.string.rec_medium_habits_desc, totalTransactions),
                    icon = Icons.Default.ShoppingCart,
                    priority = SmartRecommendationPriority.MEDIUM,
                    impact = stringResource(R.string.rec_medium_habits_impact),
                    category = RecommendationCategory.HABITS,
                ),
            )
        }

        // Необычные траты
        if (unusualSpendingDetected) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_medium_unusual_title),
                    description = stringResource(R.string.rec_medium_unusual_desc),
                    icon = Icons.Default.Analytics,
                    priority = SmartRecommendationPriority.MEDIUM,
                    impact = stringResource(R.string.rec_medium_unusual_impact),
                    category = RecommendationCategory.BUDGETING,
                ),
            )
        }

        // ✅ НОРМАЛЬНЫЕ рекомендации (общие советы)

        // Хорошие показатели - инвестиции
        if (savingsRate > 20f && monthsOfEmergencyFund > 3f) {
            recommendations.add(
                SmartRecommendation(
                    title = stringResource(R.string.rec_normal_invest_title),
                    description = stringResource(R.string.rec_normal_invest_desc),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = SmartRecommendationPriority.NORMAL,
                    impact = stringResource(R.string.rec_normal_invest_impact),
                    category = RecommendationCategory.INVESTMENTS,
                ),
            )
        }

        // Регулярный анализ бюджета
        recommendations.add(
            SmartRecommendation(
                title = stringResource(R.string.rec_normal_weekly_title),
                description = stringResource(R.string.rec_normal_weekly_desc),
                icon = Icons.Default.Schedule,
                priority = SmartRecommendationPriority.NORMAL,
                impact = stringResource(R.string.rec_normal_weekly_impact),
                category = RecommendationCategory.HABITS,
            ),
        )

        return recommendations.sortedBy { it.priority.order }
    }

    /**
     * 🏠 Рекомендации для главного экрана (онбординг)
     */
    @Composable
    fun generateOnboardingRecommendations(): List<SmartRecommendation> =
        listOf(
            SmartRecommendation(
                title = stringResource(R.string.rec_onboarding_achievements_title),
                description = stringResource(R.string.rec_onboarding_achievements_desc),
                icon = Icons.Default.EmojiEvents,
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL,
            ),
            SmartRecommendation(
                title = stringResource(R.string.rec_onboarding_import_title),
                description = stringResource(R.string.rec_onboarding_import_desc),
                icon = Icons.Default.Upload,
                priority = SmartRecommendationPriority.HIGH,
                category = RecommendationCategory.GENERAL,
            ),
            SmartRecommendation(
                title = stringResource(R.string.rec_onboarding_stats_title),
                description = stringResource(R.string.rec_onboarding_stats_desc),
                icon = Icons.Default.Analytics,
                priority = SmartRecommendationPriority.MEDIUM,
                category = RecommendationCategory.GENERAL,
            ),
            SmartRecommendation(
                title = stringResource(R.string.rec_onboarding_tips_title),
                description = stringResource(R.string.rec_onboarding_tips_desc),
                icon = Icons.Default.Lightbulb,
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL,
            ),
        )

    /**
     * 📊 Динамические советы для экрана статистики
     */
    @Composable
    fun generateStatisticsTips(): List<SmartRecommendation> {
        val tips =
            listOf(
                stringResource(R.string.rec_stats_tip1),
                stringResource(R.string.rec_stats_tip2),
                stringResource(R.string.rec_stats_tip3),
                stringResource(R.string.rec_stats_tip4),
                stringResource(R.string.rec_stats_tip5),
            )

        return tips.mapIndexed { index, tip ->
            SmartRecommendation(
                title = tip,
                description = stringResource(R.string.rec_stats_tip_desc, index + 1),
                icon =
                    when (index % 5) {
                        0 -> Icons.Default.Compare
                        1 -> Icons.Default.PieChart
                        2 -> Icons.Default.AccountBalanceWallet
                        3 -> Icons.Default.Savings
                        else -> Icons.Default.Psychology
                    },
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL,
            )
        }
    }

    /**
     * 🎯 Самые важные советы для бюджетирования
     */
    @Composable
    fun generateTopBudgetingTips(): List<SmartRecommendation> =
        listOf(
            SmartRecommendation(
                title = stringResource(R.string.rec_budget_rule_title),
                description = stringResource(R.string.rec_budget_rule_desc),
                icon = Icons.Default.Percent,
                priority = SmartRecommendationPriority.HIGH,
                impact = stringResource(R.string.rec_budget_rule_impact),
                category = RecommendationCategory.BUDGETING,
            ),
            SmartRecommendation(
                title = stringResource(R.string.rec_budget_auto_title),
                description = stringResource(R.string.rec_budget_auto_desc),
                icon = Icons.Default.AutoMode,
                priority = SmartRecommendationPriority.HIGH,
                impact = stringResource(R.string.rec_budget_auto_impact),
                category = RecommendationCategory.SAVINGS,
            ),
            SmartRecommendation(
                title = stringResource(R.string.rec_budget_track_title),
                description = stringResource(R.string.rec_budget_track_desc),
                icon = Icons.Default.Visibility,
                priority = SmartRecommendationPriority.MEDIUM,
                impact = stringResource(R.string.rec_budget_track_impact),
                category = RecommendationCategory.HABITS,
            ),
            SmartRecommendation(
                title = stringResource(R.string.rec_budget_category_title),
                description = stringResource(R.string.rec_budget_category_desc),
                icon = Icons.Default.Category,
                priority = SmartRecommendationPriority.MEDIUM,
                impact = stringResource(R.string.rec_budget_category_impact),
                category = RecommendationCategory.BUDGETING,
            ),
        )

    /**
     * 🔄 Конвертация старых рекомендаций в новый формат
     */
    fun convertLegacyRecommendations(oldRecommendations: List<Any>): List<SmartRecommendation> {
        // Здесь можно добавить логику конвертации старых форматов
        return emptyList()
    }
}
