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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.davidbugayov.financeanalyzer.R
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
        title = { Text(text = stringResource(R.string.profile_theme_select)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                ThemeOption(
                    text = stringResource(R.string.profile_theme_light),
                    selected = selectedTheme == ThemeMode.LIGHT,
                    onClick = { 
                        onThemeSelected(ThemeMode.LIGHT)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                
                ThemeOption(
                    text = stringResource(R.string.profile_theme_dark),
                    selected = selectedTheme == ThemeMode.DARK,
                    onClick = { 
                        onThemeSelected(ThemeMode.DARK)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                
                ThemeOption(
                    text = stringResource(R.string.profile_theme_system),
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
                Text(stringResource(R.string.done))
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
            .padding(dimensionResource(R.dimen.spacing_medium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null, так как обработка происходит внутри Row
        )

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
} 