package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Source

/**
 * Диалог выбора источников для фильтрации транзакций.
 *
 * @param selectedSources Список выбранных источников
 * @param sources Список всех доступных источников
 * @param onSourcesSelected Callback, вызываемый при выборе источников
 * @param onDismiss Callback, вызываемый при закрытии диалога
 */
@Composable
fun SourceSelectionDialog(
    selectedSources: List<String>,
    sources: List<Source>,
    onSourcesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Локальное состояние для хранения выбранных источников
    var localSelectedSources by remember(selectedSources) {
        mutableStateOf(selectedSources)
    }

    // Получаем список названий источников
    val sourceNames = remember(sources) {
        sources.map { it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_sources)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Опция "Все источники"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Если не все источники выбраны - выбираем все,
                            // иначе - очищаем выбор
                            localSelectedSources =
                                if (localSelectedSources.size != sourceNames.size) {
                                    sourceNames
                                } else {
                                    emptyList()
                                }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (localSelectedSources.isEmpty())
                            stringResource(R.string.all_sources)
                        else if (localSelectedSources.size == sourceNames.size)
                            stringResource(R.string.clear_selection)
                        else
                            stringResource(R.string.select_all_sources),
                        color = if (localSelectedSources.isEmpty() ||
                            localSelectedSources.size == sourceNames.size
                        )
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Чекбокс "Выбрать все"
                    Checkbox(
                        checked = localSelectedSources.size == sourceNames.size && sourceNames.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            localSelectedSources = if (isChecked) {
                                sourceNames
                            } else {
                                emptyList()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Список источников
                sources.forEach { source ->
                    SourceCheckboxItem(
                        sourceName = source.name,
                        color = source.color,
                        isSelected = localSelectedSources.contains(source.name),
                        onToggle = { isChecked ->
                            localSelectedSources = if (isChecked) {
                                localSelectedSources + source.name
                            } else {
                                localSelectedSources - source.name
                            }
                        }
                    )
                }

                // Добавляем "Наличные" если его нет в списке
                if (!sourceNames.contains("Наличные")) {
                    SourceCheckboxItem(
                        sourceName = "Наличные",
                        color = 0xFF9E9E9E.toInt(), // Серый цвет
                        isSelected = localSelectedSources.contains("Наличные"),
                        onToggle = { isChecked ->
                            localSelectedSources = if (isChecked) {
                                localSelectedSources + "Наличные"
                            } else {
                                localSelectedSources - "Наличные"
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSourcesSelected(localSelectedSources)
                onDismiss()
            }) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

/**
 * Элемент списка источников с чекбоксом.
 *
 * @param sourceName Название источника
 * @param color Цвет источника
 * @param isSelected Выбран ли источник
 * @param onToggle Callback, вызываемый при выборе/отмене выбора источника
 */
@Composable
private fun SourceCheckboxItem(
    sourceName: String,
    color: Int,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val sourceColor = remember(color) {
        android.graphics.Color.valueOf(color)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isSelected) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle
            )

            Text(
                text = sourceName,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }
    }
} 