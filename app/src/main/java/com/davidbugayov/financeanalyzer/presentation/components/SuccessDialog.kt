package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit,
    onAddAnother: () -> Unit,
    isEditMode: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.success)) },
        text = { 
            Text(
                text = stringResource(
                    if (isEditMode) R.string.transaction_updated_successfully 
                    else R.string.transaction_added_successfully
                ),
                modifier = Modifier.clickable { onDismiss() }
            ) 
        },
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