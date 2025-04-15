package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Диалог создания пользовательской категории
 */
@Composable
fun CustomCategoryDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCustomCategoryConfirm: (String) -> Unit
) {
    if (!isVisible) return
    
    var categoryName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая категория") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { 
                        categoryName = it
                        error = validateCategoryName(it)
                    },
                    label = { Text("Название категории") },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = androidx.compose.ui.graphics.Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validation = validateCategoryName(categoryName)
                    if (validation == null) {
                        onCustomCategoryConfirm(categoryName.trim())
                        categoryName = ""
                    } else {
                        error = validation
                    }
                },
                enabled = error == null && categoryName.trim().isNotEmpty()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun validateCategoryName(name: String): String? {
    return when {
        name.trim().isEmpty() -> "Название категории не может быть пустым"
        name.trim().length < 2 -> "Название категории должно содержать минимум 2 символа"
        name.trim().length > 30 -> "Название категории не должно превышать 30 символов"
        else -> null
    }
} 