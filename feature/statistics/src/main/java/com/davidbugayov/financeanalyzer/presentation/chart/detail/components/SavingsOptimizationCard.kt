package com.davidbugayov.financeanalyzer.presentation.chart.detail.components

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
import com.davidbugayov.financeanalyzer.feature.statistics.R
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.presentation.chart.detail.model.FinancialMetrics
import com.davidbugayov.financeanalyzer.presentation.chart.detail.model.setScale
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor
import timber.log.Timber

@Composable
fun SavingsOptimizationCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier,
) {
    // Временный лог для диагностики
    Timber.d("SavingsOptimizationCard: savingsRate=${metrics.savingsRate}, monthsOfSavings=${metrics.monthsOfSavings}")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(R.dimen.financial_statistics_card_corner_radius)),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = dimensionResource(R.dimen.financial_statistics_card_elevation),
            ),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.financial_statistics_card_padding)),
        ) {
            Text(
                text = stringResource(R.string.savings_optimization),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier =
                    Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_large),
                    ),
            )

            MetricRow(
                title = stringResource(UiR.string.savings_rate_title),
                value = stringResource(R.string.savings_rate_percent, metrics.savingsRate.setScale(1).toString()),
            )

            MetricRow(
                title = stringResource(R.string.months_of_savings),
                value = stringResource(R.string.months_format, metrics.monthsOfSavings.toInt()),
            )
        }
    }
}
