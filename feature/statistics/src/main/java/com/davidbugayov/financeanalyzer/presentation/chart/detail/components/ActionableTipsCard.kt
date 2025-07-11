package com.davidbugayov.financeanalyzer.presentation.chart.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.statistics.R
import com.davidbugayov.financeanalyzer.presentation.chart.detail.model.FinancialMetrics
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

@Composable
fun ActionableTipsCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier,
) {
    val tips = generateSimpleTips(metrics)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.actionable_tips_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))

            if (tips.isEmpty()) {
                Text(
                    text = stringResource(R.string.all_good_no_recommendations),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                tips.forEach { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

private fun generateSimpleTips(metrics: FinancialMetrics): List<String> {
    val tips = mutableListOf<String>()

    if (metrics.savingsRate < 10f) {
        tips.add("Увеличьте норму сбережений до 10% и выше")
    }

    if (metrics.topExpenseCategory.isNotEmpty()) {
        tips.add("Сократите расходы в категории \"${metrics.topExpenseCategory}\"")
    }

    if (metrics.monthsOfSavings < 3) {
        tips.add("Создайте финансовую подушку на 3-6 месяцев расходов")
    }

    if (metrics.mostFrequentExpenseDay.isNotEmpty()) {
        tips.add("Планируйте покупки заранее — особенно по ${metrics.mostFrequentExpenseDay.lowercase()}")
    }

    return tips
}
