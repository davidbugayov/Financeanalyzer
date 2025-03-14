package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

/**
 * Displays a summary of financial information including income, expenses, and balance
 * for a specified period.
 *
 * @param income The total income amount
 * @param expense The total expense amount
 * @param period The time period description (e.g., "01.01.2023 - 31.01.2023")
 * @param onPeriodClick Callback when the period text is clicked
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun SummarySection(
    income: Money,
    expense: Money,
    period: String,
    onPeriodClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val balance = income - expense
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val balanceColor = if (balance.isNegative()) expenseColor else incomeColor

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { onPeriodClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = stringResource(R.string.select_period),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = stringResource(R.string.currency_format, balance.format(false)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.income_currency_format, income.format(false)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = incomeColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.expense_currency_format, expense.format(false)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = expenseColor
                    )
                }
            }

            // Visual representation of income vs expense ratio
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            val totalAmount = income.amount.toDouble() + expense.amount.toDouble()
            val incomeRatio = if (totalAmount > 0) income.amount.toDouble() / totalAmount else 0.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
            ) {
                Box(
                    modifier = Modifier
                        .weight(incomeRatio.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(incomeColor)
                )
                Box(
                    modifier = Modifier
                        .weight((1 - incomeRatio).toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(expenseColor)
                )
            }
        }
    }
} 