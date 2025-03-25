package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
 * Dialog that displays information about savings rate.
 *
 * @param totalIncome Total income
 * @param totalExpense Total expense
 * @param savingsRate Savings rate
 * @param onDismiss Callback for when the dialog is dismissed
 */
@Composable
fun SavingsRateDialog(
    totalIncome: Double,
    totalExpense: Double,
    savingsRate: Double,
    onDismiss: () -> Unit
) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.savings_rate_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.spacing_medium))
            ) {
                Text(
                    text = stringResource(R.string.income_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                )

                Text(
                    text = stringResource(R.string.total_income),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                )

                Text(
                    text = Money(totalIncome).format(),
                    style = MaterialTheme.typography.titleMedium,
                    color = incomeColor,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )

                Text(
                    text = stringResource(R.string.expenses_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                )

                Text(
                    text = Money(totalExpense).format(),
                    style = MaterialTheme.typography.titleMedium,
                    color = expenseColor,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )

                Text(
                    text = stringResource(R.string.savings_rate),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                )

                Text(
                    text = String.format("%.1f%%", savingsRate),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )

                Text(
                    text = stringResource(R.string.savings_amount),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                )

                Text(
                    text = Money(totalIncome - totalExpense).format(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (totalIncome > totalExpense) incomeColor else expenseColor,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}