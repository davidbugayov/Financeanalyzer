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
import com.davidbugayov.financeanalyzer.presentation.chart.statistics.model.FinancialMetrics
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

@Composable
fun RecommendationsCard(metrics: FinancialMetrics, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            dimensionResource(R.dimen.financial_statistics_card_corner_radius),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.financial_statistics_card_elevation),
        ),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.financial_statistics_card_padding)),
        ) {
            Text(
                text = stringResource(R.string.recommendations),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_large),
                ),
            )

            // Рекомендация по норме сбережений
            if (metrics.savingsRate < 20) {
                Text(
                    text = stringResource(R.string.recommendation_savings_rate),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_medium),
                    ),
                )
            }

            // Рекомендация по основной категории расходов
            if (metrics.topExpenseCategory.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.recommendation_top_expense,
                        metrics.topExpenseCategory,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_medium),
                    ),
                )
            }

            // Рекомендация по финансовой подушке безопасности
            if (metrics.monthsOfSavings < 6) {
                Text(
                    text = if (metrics.monthsOfSavings <= 0) {
                        stringResource(R.string.recommendation_no_savings)
                    } else if (metrics.monthsOfSavings < 3) {
                        stringResource(R.string.recommendation_low_savings)
                    } else {
                        stringResource(R.string.recommendation_medium_savings)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
