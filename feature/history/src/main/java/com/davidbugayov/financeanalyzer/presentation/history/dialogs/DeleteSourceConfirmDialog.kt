package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.davidbugayov.financeanalyzer.feature.history.util.StringProvider

/**
 * Диалог подтверждения удаления источника.
 *
 * @param source Название источника, который нужно удалить
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 */
@Composable
fun DeleteSourceConfirmDialog(
    source: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = StringProvider.delete) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Text(
                text = StringProvider.deleteSourceConfirmationMessage(source),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = StringProvider.delete)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = StringProvider.cancel)
            }
        },
    )
}
