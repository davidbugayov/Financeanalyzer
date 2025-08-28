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
    ): List<StatisticItem> =
        listOf(
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
                description =
                    stringResource(
                        id = R.string.stat_income_transactions_desc,
                        (incomeTransactionsCount.toFloat() / totalTransactions * 100).toInt(),
                    ),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                type = StatisticType.POSITIVE,
            ),
            StatisticItem(
                label = stringResource(id = R.string.stat_expense_transactions),
                value = expenseTransactionsCount.toString(),
                description =
                    stringResource(
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

    /**
     * 📈 Создание анализа расходов
     */
    @Composable
    fun createExpenseAnalysis(
        averageDailyExpense: String,
        averageMonthlyExpense: String,
        topIncomeCategory: String,
        topExpenseCategory: String,
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
}
