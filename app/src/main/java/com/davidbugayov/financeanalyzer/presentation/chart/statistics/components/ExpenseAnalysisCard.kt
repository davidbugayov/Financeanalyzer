package com.davidbugayov.financeanalyzer.presentation.chart.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.viewmodel.FinancialMetrics
import com.davidbugayov.financeanalyzer.R

@Composable
fun ExpenseAnalysisCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(R.dimen.financial_statistics_card_corner_radius)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.financial_statistics_card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.financial_statistics_card_padding))
        ) {
            Text(
                text = stringResource(R.string.expense_analysis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))

            // Ежедневные расходы
            MetricRow(
                title = stringResource(R.string.avg_daily_expense),
                value = metrics.averageDailyExpense.format(true)
            )

            MetricRow(
                title = stringResource(R.string.avg_monthly_expense),
                value = metrics.averageMonthlyExpense.format(true)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))

            // Основная категория доходов
            if (metrics.topIncomeCategory.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.main_income_category, metrics.topIncomeCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
            }
            // Основная категория расходов
            if (metrics.topExpenseCategory.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.main_expense_category, metrics.topExpenseCategory),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
            }
            // Перечисление топ-3 категорий расходов
            metrics.topExpenseCategories.forEachIndexed { index, (category, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.financial_statistics_metric_row_vertical)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}. $category",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = amount.format(true),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Самый частый день расходов
            if (metrics.mostFrequentExpenseDay.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
                Text(
                    text = stringResource(R.string.most_frequent_expense_day, metrics.mostFrequentExpenseDay),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 