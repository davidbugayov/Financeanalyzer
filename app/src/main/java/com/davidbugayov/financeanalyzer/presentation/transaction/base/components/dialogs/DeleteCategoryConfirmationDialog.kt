package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Диалог подтверждения удаления категории
 */
@Composable
fun DeleteCategoryConfirmationDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удаление категории") },
        text = { 
            Text("Вы уверены, что хотите удалить категорию '$categoryName'? " +
                "Эта операция не может быть отменена.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
} 