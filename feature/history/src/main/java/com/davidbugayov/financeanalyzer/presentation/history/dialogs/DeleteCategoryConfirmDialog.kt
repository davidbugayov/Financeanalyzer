package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.history.R

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
    val messageText = if (isDefaultCategory) {
        "Вы уверены, что хотите удалить стандартную категорию \"$category\"?\n\n" +
            "Это скроет её из списка, но она может быть восстановлена при переустановке приложения."
    } else {
        "Вы уверены, что хотите удалить категорию \"$category\"?\n\n" +
            "Она будет полностью удалена и не может быть восстановлена."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = { Text(text = messageText) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
