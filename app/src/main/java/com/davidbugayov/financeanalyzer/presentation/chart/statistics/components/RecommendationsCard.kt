package com.davidbugayov.financeanalyzer.presentation.chart.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.viewmodel.FinancialMetrics
import com.davidbugayov.financeanalyzer.R

@Composable
fun RecommendationsCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier
) {
    val effectiveRecommendations = buildList {
        if (metrics.savingsRate < 10) {
            add(stringResource(R.string.recommendation_save_10_15))
        } else if (metrics.savingsRate < 20) {
            add(stringResource(R.string.recommendation_increase_savings))
        } else {
            add(stringResource(R.string.recommendation_savings_excellent))
        }
        if (metrics.topExpenseCategory.isNotEmpty()) {
            add(stringResource(R.string.recommendation_top_expense_category, metrics.topExpenseCategory))
        }
        if (metrics.monthsOfSavings < 3) {
            add(stringResource(R.string.recommendation_months_of_savings_low))
        } else if (metrics.monthsOfSavings in 3.0..6.0) {
            add(stringResource(R.string.recommendation_months_of_savings_medium))
        } else if (metrics.monthsOfSavings > 6) {
            add(stringResource(R.string.recommendation_months_of_savings_high))
        }
        add(stringResource(R.string.recommendation_increase_income))
        add(stringResource(R.string.recommendation_track_expenses))
        add(stringResource(R.string.recommendation_auto_savings))
        add(stringResource(R.string.recommendation_compare_prices))
        add(stringResource(R.string.recommendation_avoid_impulse))
    }
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
                text = stringResource(R.string.recommendations),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))

            if (effectiveRecommendations.isNotEmpty()) {
                effectiveRecommendations.forEach { recommendation ->
                    Text(
                        text = "• $recommendation",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.financial_statistics_metric_row_vertical))
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.no_recommendations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}