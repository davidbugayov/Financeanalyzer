package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode

/**
 * Диалог выбора темы приложения.
 * @param selectedTheme Текущая выбранная тема.
 * @param onThemeSelected Обработчик выбора темы.
 * @param onDismiss Обработчик закрытия диалога.
 */
@Composable
fun ThemeSelectionDialog(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Выберите тему") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                ThemeOption(
                    text = "Светлая",
                    selected = selectedTheme == ThemeMode.LIGHT,
                    onClick = { 
                        onThemeSelected(ThemeMode.LIGHT)
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeOption(
                    text = "Темная",
                    selected = selectedTheme == ThemeMode.DARK,
                    onClick = { 
                        onThemeSelected(ThemeMode.DARK)
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeOption(
                    text = "Системная",
                    selected = selectedTheme == ThemeMode.SYSTEM,
                    onClick = { 
                        onThemeSelected(ThemeMode.SYSTEM)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}

/**
 * Опция выбора темы.
 * @param text Название темы.
 * @param selected Выбрана ли тема.
 * @param onClick Обработчик выбора темы.
 */
@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because we're handling the click on the row
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
} 