package com.davidbugayov.financeanalyzer.ui.components.tips

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Категории финансовых советов
 */
enum class TipCategory(
    val displayName: String,
    val icon: ImageVector,
    val priority: Int,
) {
    CRITICAL("Критично", Icons.AutoMirrored.Filled.TrendingDown, 1),
    SAVINGS("Накопления", Icons.Default.Savings, 2),
    ANALYTICS("Анализ", Icons.Default.Analytics, 3),
    OPTIMIZATION("Оптимизация", Icons.AutoMirrored.Filled.TrendingUp, 4),
    PLANNING("Планирование", Icons.Default.PieChart, 5),
}

/**
 * Финансовый совет с категоризацией и приоритизацией
 */
data class FinancialTip(
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    val descriptionArgs: List<Any> = emptyList(),
    val category: TipCategory,
    val priority: Int = category.priority,
    @StringRes val actionResId: Int? = null,
    val isPersonalized: Boolean = false,
)

/**
 * Менеджер финансовых советов
 */
object FinancialTipsManager {
    /**
     * Получает персонализированные советы на основе финансовых данных
     *
     * @param savingsRate Коэффициент сбережений (0-1)
     * @param monthsOfSavings Месяцы накоплений
     * @param hasRegularIncome Есть ли регулярный доход
     * @param expenseGrowth Рост расходов (отрицательный = снижение)
     * @return Список приоритизированных советов
     */
    fun getPersonalizedTips(
        savingsRate: Double = 0.0,
        monthsOfSavings: Double = 0.0,
        hasRegularIncome: Boolean = true,
        expenseGrowth: Double = 0.0,
    ): List<FinancialTip> {
        val tips = mutableListOf<FinancialTip>()

        // Критичные советы
        if (monthsOfSavings < 3.0) {
            tips.add(
                FinancialTip(
                    titleResId = R.string.tip_create_emergency_fund_title,
                    descriptionResId = R.string.tip_create_emergency_fund_desc,
                    descriptionArgs = listOf(monthsOfSavings.toInt()),
                    category = TipCategory.CRITICAL,
                    priority = 1,
                    actionResId = R.string.action_start_saving,
                    isPersonalized = true,
                ),
            )
        }

        if (savingsRate < 0.1) {
            tips.add(
                FinancialTip(
                    titleResId = R.string.tip_increase_savings_title,
                    descriptionResId = R.string.tip_increase_savings_desc,
                    descriptionArgs = listOf((savingsRate * 100).toInt()),
                    category = TipCategory.CRITICAL,
                    priority = 2,
                    actionResId = R.string.action_review_budget,
                    isPersonalized = true,
                ),
            )
        }

        // Советы по накоплениям
        if (monthsOfSavings >= 3.0 && monthsOfSavings < 6.0) {
            tips.add(
                FinancialTip(
                    titleResId = R.string.tip_strengthen_safety_net_title,
                    descriptionResId = R.string.tip_strengthen_safety_net_desc,
                    category = TipCategory.SAVINGS,
                    actionResId = R.string.action_continue_saving,
                ),
            )
        }

        // Советы по анализу
        tips.add(
            FinancialTip(
                titleResId = R.string.tip_track_trends_title,
                descriptionResId = R.string.tip_track_trends_desc,
                category = TipCategory.ANALYTICS,
                actionResId = R.string.action_study_statistics,
            ),
        )

        // Советы по оптимизации расходов
        if (expenseGrowth > 0.15) {
            tips.add(
                FinancialTip(
                    titleResId = R.string.tip_control_expense_growth_title,
                    descriptionResId = R.string.tip_control_expense_growth_desc,
                    descriptionArgs = listOf((expenseGrowth * 100).toInt()),
                    category = TipCategory.OPTIMIZATION,
                    priority = 1,
                    actionResId = R.string.action_analyze_spending,
                    isPersonalized = true,
                ),
            )
        }

        // Советы по экономии
        tips.add(
            FinancialTip(
                titleResId = R.string.tip_easy_savings_title,
                descriptionResId = R.string.tip_easy_savings_desc,
                category = TipCategory.SAVINGS,
                actionResId = R.string.action_view_categories,
            ),
        )

        // Советы по увеличению дохода/инвестиций
        tips.add(
            FinancialTip(
                titleResId = R.string.tip_increase_income_title,
                descriptionResId = R.string.tip_increase_income_desc,
                category = TipCategory.PLANNING,
                actionResId = R.string.action_create_plan,
            ),
        )

        // Базовые советы по планированию
        tips.add(
            FinancialTip(
                titleResId = R.string.tip_set_goals_title,
                descriptionResId = R.string.tip_set_goals_desc,
                category = TipCategory.PLANNING,
                actionResId = R.string.action_create_plan,
            ),
        )

        tips.add(
            FinancialTip(
                titleResId = R.string.tip_automate_savings_title,
                descriptionResId = R.string.tip_automate_savings_desc,
                category = TipCategory.SAVINGS,
            ),
        )

        // Возвращаем отсортированные по приоритету советы (максимум 4-5)
        return tips.sortedBy { it.priority }.take(5)
    }

    /**
     * Получает общие советы для начинающих
     */
    fun getGeneralTips(): List<FinancialTip> =
        listOf(
            FinancialTip(
                titleResId = R.string.tip_basic_tracking_title,
                descriptionResId = R.string.tip_basic_tracking_desc,
                category = TipCategory.ANALYTICS,
                actionResId = R.string.action_add_transaction,
            ),
            FinancialTip(
                titleResId = R.string.tip_rule_50_30_20_title,
                descriptionResId = R.string.tip_rule_50_30_20_desc,
                category = TipCategory.PLANNING,
            ),
            FinancialTip(
                titleResId = R.string.tip_create_emergency_fund_title,
                descriptionResId = R.string.tip_create_emergency_fund_desc,
                descriptionArgs = listOf(3),
                category = TipCategory.SAVINGS,
                actionResId = R.string.action_start_saving,
            ),
        )
}
