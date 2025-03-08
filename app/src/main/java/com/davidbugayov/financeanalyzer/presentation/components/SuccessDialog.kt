package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit,
    onAddAnother: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.success)) },
        text = { Text(text = stringResource(R.string.transaction_added_successfully)) },
        confirmButton = {
            TextButton(onClick = onAddAnother) {
                Text(text = stringResource(R.string.add_another))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.done))
            }
        }
    )
} 