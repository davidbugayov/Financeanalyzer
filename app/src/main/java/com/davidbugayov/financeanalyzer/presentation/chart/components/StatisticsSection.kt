package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.util.Locale

/**
 * Displays statistics about expenses and savings rate.
 *
 * @param avgDailyExpense Average daily expense
 * @param avgMonthlyExpense Average monthly expense
 * @param avgYearlyExpense Average yearly expense
 * @param savingsRate Savings rate percentage
 * @param onSavingsRateInfoClick Callback for when the savings rate info icon is clicked
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun StatisticsSection(
    avgDailyExpense: Double,
    avgMonthlyExpense: Double,
    avgYearlyExpense: Double,
    savingsRate: Double,
    onSavingsRateInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_large)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = stringResource(R.string.average_values),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = stringResource(R.string.analyze_expenses_for_budget),
                fontSize = dimensionResource(id = R.dimen.text_size_large).value.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Daily and monthly averages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.average_daily),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Money(avgDailyExpense).format(false),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalExpenseColor.current
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.average_monthly),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Money(avgMonthlyExpense).format(false),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalExpenseColor.current
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Yearly average and savings rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.average_yearly),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Money(avgYearlyExpense).format(false),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LocalExpenseColor.current
                    )
                }

                // Savings rate
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.savings_rate),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", savingsRate),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LocalIncomeColor.current
                        )
                        IconButton(
                            onClick = onSavingsRateInfoClick,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = stringResource(R.string.savings_rate),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                            )
                        }
                    }
                }
            }

            // Visual representation of savings rate
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            Text(
                text = stringResource(R.string.savings_percentage),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
            ) {
                Box(
                    modifier = Modifier
                        .weight((savingsRate / 100).coerceAtLeast(0.01).toFloat())
                        .fillMaxHeight()
                        .background(LocalIncomeColor.current)
                )
                Box(
                    modifier = Modifier
                        .weight((1 - (savingsRate / 100)).coerceAtLeast(0.01).toFloat())
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // Interpretation hints
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        savingsRate < 10 -> stringResource(R.string.low)
                        savingsRate < 20 -> stringResource(R.string.medium)
                        else -> stringResource(R.string.good)
                    },
                    fontSize = 12.sp,
                    color = when {
                        savingsRate < 10 -> MaterialTheme.colorScheme.error
                        savingsRate < 20 -> MaterialTheme.colorScheme.tertiary
                        else -> LocalIncomeColor.current
                    }
                )
                Text(
                    text = stringResource(R.string.recommended_savings_rate),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
} 