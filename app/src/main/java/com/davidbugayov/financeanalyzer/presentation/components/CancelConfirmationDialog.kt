package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R

@Composable
fun CancelConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.cancel_transaction)) },
        text = { Text(text = stringResource(R.string.cancel_transaction_confirmation)) },
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.no))
            }
        },
    )
}
