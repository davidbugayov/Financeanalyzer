package com.davidbugayov.financeanalyzer.presentation.chart.statistics.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.viewmodel.FinancialMetrics
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor
import java.util.Locale

@Composable
fun TransactionsStatisticsCard(metrics: FinancialMetrics, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            dimensionResource(R.dimen.financial_statistics_card_corner_radius)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.financial_statistics_card_elevation)
        ),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.financial_statistics_card_padding))
        ) {
            Text(
                text = stringResource(R.string.transaction_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_large)
                )
            )

            // Количество транзакций
            MetricRow(
                title = stringResource(R.string.total_transactions),
                value = "${metrics.totalTransactions}"
            )

            MetricRow(
                title = stringResource(R.string.income_transactions),
                value = "${metrics.incomeTransactionsCount}"
            )

            MetricRow(
                title = stringResource(R.string.expense_transactions),
                value = "${metrics.expenseTransactionsCount}"
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_medium)
                )
            )

            // Средние значения
            MetricRow(
                title = stringResource(R.string.avg_income_per_transaction),
                value = metrics.averageIncomePerTransaction.format(true)
            )

            MetricRow(
                title = stringResource(R.string.avg_expense_per_transaction),
                value = metrics.averageExpensePerTransaction.format(true)
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_medium)
                )
            )

            // Максимальные значения
            MetricRow(
                title = stringResource(R.string.max_income),
                value = metrics.maxIncome.format(true)
            )

            MetricRow(
                title = stringResource(R.string.max_expense),
                value = metrics.maxExpense.format(true)
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_medium)
                )
            )

            // Дополнительные метрики
            MetricRow(
                title = stringResource(R.string.savings_rate_label),
                value = "${metrics.savingsRate.toBigDecimal().setScale(
                    0,
                    java.math.RoundingMode.FLOOR
                ).toPlainString()}%"
            )
            MetricRow(
                title = stringResource(R.string.months_of_savings_label),
                value = String.format(Locale.US, "%.1f", metrics.monthsOfSavings)
            )
        }
    }
} 
