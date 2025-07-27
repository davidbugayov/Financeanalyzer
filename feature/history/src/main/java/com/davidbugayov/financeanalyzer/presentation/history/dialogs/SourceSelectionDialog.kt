package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.feature.history.R
import com.davidbugayov.financeanalyzer.utils.ColorUtils

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
    sources: List<Source>,
    selectedSources: List<String>,
    onSourcesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var localSelectedSources by remember { mutableStateOf(selectedSources) }
    val sourceNames = sources.map { it.name }

    AlertDialog(
        onDismissRequest = onDismiss,
                    title = { Text("Выбрать источники") },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Кнопка "Выбрать все"
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                localSelectedSources =
                                    if (localSelectedSources.size == sourceNames.size) {
                                        emptyList()
                                    } else {
                                        sourceNames
                                    }
                            }
                            .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            if (localSelectedSources.isEmpty()) {
                                "Все источники"
                            } else if (localSelectedSources.size == sourceNames.size) {
                                "Очистить выбор"
                            } else {
                                "Выбрать все"
                            },
                        color =
                            if (localSelectedSources.isEmpty() ||
                                localSelectedSources.size == sourceNames.size
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        modifier = Modifier.weight(1f),
                    )

                    Checkbox(
                        checked = localSelectedSources.size == sourceNames.size && sourceNames.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            localSelectedSources =
                                if (isChecked) {
                                    sourceNames
                                } else {
                                    emptyList()
                                }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Список источников
                val isDarkTheme = isSystemInDarkTheme()
                sources.forEach { source ->
                    val effectiveColor =
                        if (source.color != 0) { // Предполагаем, что 0 означает отсутствие цвета
                            Color(source.color)
                        } else {
                            ColorUtils.getEffectiveSourceColor(
                                sourceName = source.name,
                                sourceColorHex = null, // Так как source.color (Int) не был валидным HEX
                                isExpense = false, // Для диалога выбора источника не применимо
                                isDarkTheme = isDarkTheme,
                            )
                        }
                    SourceCheckboxItem(
                        sourceName = source.name,
                        color = effectiveColor, // Передаем уже готовый Color
                        isSelected = localSelectedSources.contains(source.name),
                        onToggle = { isChecked ->
                            localSelectedSources =
                                if (isChecked) {
                                    localSelectedSources + source.name
                                } else {
                                    localSelectedSources - source.name
                                }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSourcesSelected(localSelectedSources)
                onDismiss()
            }) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
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
    color: Color,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onToggle(!isSelected) })
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle,
            )

            Text(
                text = sourceName,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        color
                    },
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
            )
        }
    }
}
