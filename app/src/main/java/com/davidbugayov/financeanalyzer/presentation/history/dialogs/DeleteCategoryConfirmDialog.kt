package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R

/**
 * Диалог подтверждения удаления категории.
 *
 * @param category Название категории для удаления
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 * @param isDefaultCategory Является ли категория стандартной (нельзя удалить)
 */
@Composable
fun DeleteCategoryConfirmDialog(
    category: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDefaultCategory: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_category)) },
        text = {
            if (isDefaultCategory) {
                Text(stringResource(R.string.cannot_delete_default_category, category))
            } else {
                Text(stringResource(R.string.delete_category_confirmation, category))
            }
        },
        confirmButton = {
            if (!isDefaultCategory) {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.delete))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 