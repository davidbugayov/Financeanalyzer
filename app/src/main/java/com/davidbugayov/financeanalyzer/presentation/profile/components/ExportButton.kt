package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase.ExportAction

/**
 * Кнопка для экспорта данных в CSV с функцией выбора действия.
 *
 * @param onClick Обработчик нажатия кнопки, принимает выбранное действие
 * @param isExporting Флаг, указывающий на процесс экспорта
 * @param showFilePath Путь к экспортированному файлу для отображения
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun ExportButton(
    onClick: (ExportAction) -> Unit,
    isExporting: Boolean,
    showFilePath: String? = null,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }
    val exportedPath = remember { mutableStateOf(showFilePath) }
    
    Button(
        onClick = { showDialog.value = true },
        modifier = modifier,
        enabled = !isExporting
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = dimensionResource(R.dimen.spacing_medium))
                    .size(dimensionResource(R.dimen.icon_size_small)),
                strokeWidth = dimensionResource(R.dimen.stroke_medium)
            )
        } else {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_medium))
            )
        }
        Text(
            text = stringResource(R.string.export_to_csv),
            style = MaterialTheme.typography.labelMedium
        )
    }
    
    // Диалог выбора действия
    if (showDialog.value) {
        ActionChoiceDialog(
            onDismiss = { showDialog.value = false },
            onActionSelected = { action ->
                onClick(action)
                showDialog.value = false
            },
            filePath = exportedPath.value
        )
    }
}

/**
 * Диалог выбора действия с экспортированным файлом
 */
@Composable
private fun ActionChoiceDialog(
    onDismiss: () -> Unit,
    onActionSelected: (ExportAction) -> Unit,
    filePath: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_choose_action)) },
        text = {
            if (filePath != null) {
                Text(stringResource(R.string.exported_file_path, filePath))
            } else {
                Text(stringResource(R.string.export_description))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onActionSelected(ExportAction.SHARE) }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_small))
                )
                Text(stringResource(R.string.export_share))
            }
        },
        dismissButton = {
            // Комбинируем две кнопки вместе
            androidx.compose.foundation.layout.Row {
                TextButton(
                    onClick = { onActionSelected(ExportAction.OPEN) }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_small))
                    )
                    Text(stringResource(R.string.export_open))
                }
                
                TextButton(
                    onClick = { onActionSelected(ExportAction.SAVE_ONLY) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_small))
                    )
                    Text(stringResource(R.string.export_only_save))
                }
            }
        }
    )
}

/**
 * Описание функциональности экспорта данных.
 *
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun ExportDescription(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.export_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
} 