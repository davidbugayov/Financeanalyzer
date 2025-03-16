package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction

/**
 * Диалог подтверждения удаления транзакции.
 *
 * @param transaction Транзакция, которую нужно удалить
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 */
@Composable
fun DeleteTransactionDialog(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_transaction)) },
        text = {
            Text(
                stringResource(
                    R.string.delete_transaction_confirmation,
                    transaction.category,
                    transaction.amount.formatted(false)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 