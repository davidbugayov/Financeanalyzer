package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

/**
 * 🔄 Маппер для преобразования финансовых данных в премиум карточки
 * Создает красивые и информативные представления данных
 */
object FinancialDataMapper {
    /**
     * 📊 Создание статистики транзакций
     */
    @Composable
    fun createTransactionStatistics(
        totalTransactions: Int,
        incomeTransactionsCount: Int,
        expenseTransactionsCount: Int,
        averageIncomePerTransaction: String,
        averageExpensePerTransaction: String,
        maxIncome: String,
        maxExpense: String,
        savingsRate: Float,
        monthsOfSavings: Float,
    ): List<StatisticItem> {
        return listOf(
            StatisticItem(
                label = stringResource(id = R.string.stat_total_transactions),
                value = totalTransactions.toString(),
                description = stringResource(id = R.string.stat_total_transactions_desc),
                icon = Icons.Default.Receipt,
                type = StatisticType.NEUTRAL,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_income_transactions),
                value = incomeTransactionsCount.toString(),
                description = stringResource(
                    id = R.string.stat_income_transactions_desc,
                    (incomeTransactionsCount.toFloat() / totalTransactions * 100).toInt(),
                ),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                type = StatisticType.POSITIVE,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_expense_transactions),
                value = expenseTransactionsCount.toString(),
                description = stringResource(
                    id = R.string.stat_expense_transactions_desc,
                    (expenseTransactionsCount.toFloat() / totalTransactions * 100).toInt(),
                ),
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                type = StatisticType.NEGATIVE,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_avg_income),
                value = averageIncomePerTransaction,
                description = stringResource(id = R.string.stat_avg_income_desc),
                icon = Icons.Default.AttachMoney,
                type = StatisticType.POSITIVE,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_avg_expense),
                value = averageExpensePerTransaction,
                description = stringResource(id = R.string.stat_avg_expense_desc),
                icon = Icons.Default.Money,
                type = StatisticType.NEGATIVE,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_max_income),
                value = maxIncome,
                description = stringResource(id = R.string.stat_max_income_desc),
                icon = Icons.Default.Star,
                type = StatisticType.POSITIVE,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_max_expense),
                value = maxExpense,
                description = stringResource(id = R.string.stat_max_expense_desc),
                icon = Icons.Default.Warning,
                type =
                    if (maxExpense.replace(Regex("[^\\d.]"), "").toFloatOrNull() ?: 0f > 50000f) {
                        StatisticType.WARNING
                    } else {
                        StatisticType.NEGATIVE
                    },
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_savings_rate),
                value = "${savingsRate.toInt()}%",
                description = stringResource(id = R.string.stat_savings_rate_desc),
                icon = Icons.Default.Savings,
                type =
                    when {
                        savingsRate >= 20f -> StatisticType.POSITIVE
                        savingsRate >= 10f -> StatisticType.WARNING
                        else -> StatisticType.NEGATIVE
                    },
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_financial_cushion),
                value = "${monthsOfSavings.toInt()} ${stringResource(id = R.string.months_short)}",
                description = stringResource(id = R.string.stat_financial_cushion_desc),
                icon = Icons.Default.Shield,
                type =
                    when {
                        monthsOfSavings >= 6f -> StatisticType.POSITIVE
                        monthsOfSavings >= 3f -> StatisticType.WARNING
                        else -> StatisticType.NEGATIVE
                    },
            ),
        )
    }

    /**
     * 📈 Создание анализа расходов
     */
    @Composable
    fun createExpenseAnalysis(
        averageDailyExpense: String,
        averageMonthlyExpense: String,
        topIncomeCategory: String,
        topExpenseCategory: String,
        topExpenseCategories: List<Pair<String, String>>,
        mostFrequentExpenseDay: String,
    ): List<StatisticItem> {
        val statistics = mutableListOf<StatisticItem>()

        statistics.add(
            StatisticItem(
                label = stringResource(id = R.string.stat_daily_expense),
                value = averageDailyExpense,
                description = stringResource(id = R.string.stat_daily_expense_desc),
                icon = Icons.Default.CalendarToday,
                type = StatisticType.NEUTRAL,
            ),
        )

        statistics.add(
            StatisticItem(
                label = stringResource(id = R.string.stat_monthly_expense),
                value = averageMonthlyExpense,
                description = stringResource(id = R.string.stat_monthly_expense_desc),
                icon = Icons.Default.DateRange,
                type = StatisticType.NEUTRAL,
            ),
        )

        if (topIncomeCategory.isNotEmpty()) {
            statistics.add(
                StatisticItem(
                    label = stringResource(id = R.string.stat_main_income_category),
                    value = topIncomeCategory,
                    description = stringResource(id = R.string.stat_main_income_category_desc),
                    icon = Icons.Default.AccountBalance,
                    type = StatisticType.POSITIVE,
                ),
            )
        }

        if (topExpenseCategory.isNotEmpty()) {
            statistics.add(
                StatisticItem(
                    label = stringResource(id = R.string.stat_main_expense_category),
                    value = topExpenseCategory,
                    description = stringResource(id = R.string.stat_main_expense_category_desc),
                    icon = Icons.Default.PieChart,
                    type = StatisticType.WARNING,
                ),
            )
        }

        if (mostFrequentExpenseDay.isNotEmpty()) {
            statistics.add(
                StatisticItem(
                    label = stringResource(id = R.string.stat_most_frequent_expense_day),
                    value = mostFrequentExpenseDay,
                    description = stringResource(id = R.string.stat_most_frequent_expense_day_desc),
                    icon = Icons.Default.Schedule,
                    type = StatisticType.NEUTRAL,
                ),
            )
        }

        return statistics
    }

    /**
     * 🔍 Создание инсайтов расходов
     */
    @Composable
    fun createExpenseInsights(
        topExpenseCategories: List<Pair<String, String>>,
        savingsRate: Float,
        monthsOfSavings: Float,
        totalTransactions: Int,
        mostFrequentExpenseDay: String,
    ): List<InsightItem> {
        val insights = mutableListOf<InsightItem>()

        // Анализ категорий расходов
        if (topExpenseCategories.isNotEmpty()) {
            val topCategory = topExpenseCategories.first()
            val totalExpense =
                topExpenseCategories.sumOf {
                    it.second.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
                }
            val percentage =
                if (totalExpense > 0) {
                    (((topCategory.second.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0) / totalExpense) * 100).toInt()
                } else {
                    0
                }

            insights.add(
                InsightItem(
                    title = stringResource(id = R.string.insight_expense_concentration),
                    description = stringResource(id = R.string.insight_expense_concentration_desc),
                    metric = stringResource(id = R.string.insight_expense_concentration_metric),
                    icon = Icons.Default.PieChart,
                    importance =
                        when {
                            percentage > 50 -> InsightImportance.HIGH
                            percentage > 30 -> InsightImportance.MEDIUM
                            else -> InsightImportance.LOW
                        },
                ),
            )
        }

        // Анализ нормы сбережений
        insights.add(
            InsightItem(
                title = stringResource(id = R.string.insight_financial_health),
                description =
                    when {
                        savingsRate >= 20f -> stringResource(id = R.string.insight_financial_health_excellent)
                        savingsRate >= 15f -> stringResource(id = R.string.insight_financial_health_good)
                        savingsRate >= 10f -> stringResource(id = R.string.insight_financial_health_ok)
                        savingsRate >= 5f -> stringResource(id = R.string.insight_financial_health_low)
                        else -> stringResource(id = R.string.insight_financial_health_critical)
                    },
                metric = stringResource(id = R.string.insight_financial_health_metric, savingsRate.toInt()),
                icon =
                    when {
                        savingsRate >= 15f -> Icons.AutoMirrored.Filled.TrendingUp
                        savingsRate >= 10f -> Icons.Default.Balance
                        else -> Icons.Default.Warning
                    },
                importance =
                    when {
                        savingsRate < 5f -> InsightImportance.HIGH
                        savingsRate < 15f -> InsightImportance.MEDIUM
                        else -> InsightImportance.LOW
                    },
            ),
        )

        // Анализ финансовой подушки
        insights.add(
            InsightItem(
                title = stringResource(id = R.string.insight_financial_protection),
                description =
                    when {
                        monthsOfSavings >= 6f -> stringResource(id = R.string.insight_financial_protection_excellent)
                        monthsOfSavings >= 3f -> stringResource(id = R.string.insight_financial_protection_good)
                        monthsOfSavings >= 1f -> stringResource(id = R.string.insight_financial_protection_minimal)
                        else -> stringResource(id = R.string.insight_financial_protection_none)
                    },
                metric = stringResource(id = R.string.insight_financial_protection_metric, monthsOfSavings.toInt()),
                icon =
                    when {
                        monthsOfSavings >= 3f -> Icons.Default.Shield
                        monthsOfSavings >= 1f -> Icons.Default.Security
                        else -> Icons.Default.Warning
                    },
                importance =
                    when {
                        monthsOfSavings < 1f -> InsightImportance.HIGH
                        monthsOfSavings < 3f -> InsightImportance.MEDIUM
                        else -> InsightImportance.LOW
                    },
            ),
        )

        // Анализ количества транзакций
        if (totalTransactions > 100) {
            insights.add(
                InsightItem(
                    title = stringResource(id = R.string.insight_expense_frequency),
                    description = stringResource(id = R.string.insight_expense_frequency_desc, totalTransactions),
                    metric = stringResource(id = R.string.insight_expense_frequency_metric, totalTransactions),
                    icon = Icons.Default.ShoppingCart,
                    importance = if (totalTransactions > 200) InsightImportance.MEDIUM else InsightImportance.LOW,
                ),
            )
        }

        // Анализ паттернов трат
        if (mostFrequentExpenseDay.isNotEmpty()) {
            insights.add(
                InsightItem(
                    title = stringResource(id = R.string.insight_expense_pattern),
                    description = stringResource(id = R.string.insight_expense_pattern_desc, mostFrequentExpenseDay),
                    metric = stringResource(id = R.string.insight_expense_pattern_metric, mostFrequentExpenseDay),
                    icon = Icons.Default.Timeline,
                    importance = InsightImportance.LOW,
                ),
            )
        }

        return insights
    }

    /**
     * 📋 Создание инсайтов паттернов трат
     */
    @Composable
    fun createSpendingPatternInsights(
        mostFrequentExpenseDay: String,
        expenseTransactionsCount: Int,
        averageExpensePerTransaction: Float,
    ): List<InsightItem> {
        val insights = mutableListOf<InsightItem>()

        if (mostFrequentExpenseDay.isNotEmpty()) {
            insights.add(
                InsightItem(
                    title = stringResource(id = R.string.insight_active_weekday),
                    description = stringResource(id = R.string.insight_active_weekday_desc, mostFrequentExpenseDay),
                    metric = stringResource(id = R.string.insight_active_weekday_metric, mostFrequentExpenseDay),
                    icon = Icons.Default.CalendarToday,
                    importance = InsightImportance.LOW,
                ),
            )
        }

        // Анализ размера трат
        when {
            averageExpensePerTransaction < 500f -> {
                insights.add(
                    InsightItem(
                        title = stringResource(id = R.string.insight_small_expenses),
                        description = stringResource(id = R.string.insight_small_expenses_desc),
                        metric = stringResource(id = R.string.insight_small_expenses_metric, averageExpensePerTransaction.toInt()),
                        icon = Icons.Default.LocalGroceryStore,
                        importance = InsightImportance.LOW,
                    ),
                )
            }
            averageExpensePerTransaction > 2000f -> {
                insights.add(
                    InsightItem(
                        title = stringResource(id = R.string.insight_large_expenses),
                        description = stringResource(id = R.string.insight_large_expenses_desc),
                        metric = stringResource(id = R.string.insight_large_expenses_metric, averageExpensePerTransaction.toInt()),
                        icon = Icons.Default.ShoppingBag,
                        importance = InsightImportance.MEDIUM,
                    ),
                )
            }
        }

        // Анализ частоты трат
        if (expenseTransactionsCount > 150) {
            insights.add(
                InsightItem(
                    title = stringResource(id = R.string.insight_high_activity),
                    description = stringResource(id = R.string.insight_high_activity_desc, expenseTransactionsCount),
                    metric = stringResource(id = R.string.insight_high_activity_metric, expenseTransactionsCount),
                    icon = Icons.Default.Speed,
                    importance = InsightImportance.MEDIUM,
                ),
            )
        }

        return insights
    }
}
