package com.davidbugayov.financeanalyzer.presentation.debt

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог частичного погашения долга.
 */
@Composable
fun repayDebtDialog(
    debtId: String,
    onDismiss: () -> Unit,
    onConfirm: (Money) -> Unit,
) {
    val amountStr = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = UiR.string.debt_repay)) },
        text = {
            OutlinedTextField(
                value = amountStr.value,
                onValueChange = { amountStr.value = it },
                label = { Text(text = stringResource(id = UiR.string.debt_amount)) },
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountStr.value.replace(',', '.').toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    onConfirm(Money(amount, Currency.RUB))
                }
            }) { Text(text = stringResource(id = UiR.string.confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(id = UiR.string.cancel)) } },
    )
}
