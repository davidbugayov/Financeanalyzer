package com.davidbugayov.financeanalyzer.presentation.chart.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

@Composable
fun KeyMetricsCard(
    income: Money,
    expense: Money,
    savingsRate: Double,
    modifier: Modifier = Modifier
) {
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
                text = stringResource(R.string.key_metrics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_large)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.income),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = income.format(true),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorResource(R.color.financial_statistics_progress_good),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.expense),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = expense.format(true),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_large)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.savings_norm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (savingsRate >= 20) {
                        stringResource(R.string.savings_status_done)
                    } else {
                        stringResource(
                            R.string.savings_status_attention
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (savingsRate >= 20) {
                        colorResource(R.color.savings_rate_good)
                    } else {
                        colorResource(
                            R.color.savings_rate_needs_attention
                        )
                    }
                )
            }
            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_medium)
                )
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(0.15f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                            .background(
                                colorResource(R.color.savings_rate_needs_attention).copy(
                                    alpha = 0.2f
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.1f)
                            .height(8.dp)
                            .background(
                                colorResource(R.color.savings_rate_satisfactory).copy(alpha = 0.2f)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.15f)
                            .height(8.dp)
                            .background(colorResource(R.color.savings_rate_good).copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                            .background(
                                colorResource(R.color.savings_rate_excellent).copy(alpha = 0.2f)
                            )
                    )
                }
                val normalizedRate = (savingsRate / 100.0).coerceIn(0.0, 1.0)
                val progressColor = when {
                    savingsRate >= 30 -> colorResource(R.color.savings_rate_excellent)
                    savingsRate >= 15 -> colorResource(R.color.savings_rate_good)
                    savingsRate >= 5 -> colorResource(R.color.savings_rate_satisfactory)
                    else -> colorResource(R.color.savings_rate_needs_attention)
                }
                LinearProgressIndicator(
                    progress = { normalizedRate.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = Color.Transparent
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.savings_rate_percent_0),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.savings_rate_percent_15),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.savings_rate_percent_30),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.savings_rate_percent_50),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.financial_statistics_spacer_small)
                )
            )
        }
    }
} 
