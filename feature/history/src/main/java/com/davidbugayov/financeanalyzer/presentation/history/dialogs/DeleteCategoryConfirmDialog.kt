package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.history.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог подтверждения удаления категории.
 *
 * @param category Название категории, которую нужно удалить
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 * @param isDefaultCategory Является ли категория стандартной
 */
@Composable
fun DeleteCategoryConfirmDialog(
    category: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDefaultCategory: Boolean = false,
) {
    val messageText =
        if (isDefaultCategory) {
            stringResource(UiR.string.dialog_delete_category_message_irreversible, category)
        } else {
            stringResource(UiR.string.dialog_delete_category_message_default, category)
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(UiR.string.dialog_delete_title)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = { Text(text = messageText) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(UiR.string.dialog_delete_title))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(UiR.string.dialog_cancel))
            }
        },
    )
}
