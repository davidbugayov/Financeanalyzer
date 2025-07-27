package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.budget.R

/**
 * Диалог для выбора категорий (импорта или связывания)
 *
 * @param onDismiss Функция, вызываемая при закрытии диалога
 * @param onImport Функция, вызываемая при выборе категорий
 * @param availableCategories Список доступных категорий
 * @param title Заголовок диалога
 * @param subtitle Подзаголовок диалога
 * @param confirmButtonText Текст кнопки подтверждения
 * @param preselectedCategories Список предварительно выбранных категорий
 */
@Composable
fun ImportCategoriesDialog(
    onDismiss: () -> Unit,
    onImport: (List<String>) -> Unit,
    availableCategories: List<UiCategory>,
    title: String = stringResource(R.string.import_categories_title),
    subtitle: String = stringResource(R.string.import_categories_subtitle),
    confirmButtonText: String = stringResource(R.string.import_categories_confirm),
    preselectedCategories: List<String> = emptyList(),
) {
    // Список выбранных категорий
    val selectedCategories =
        remember {
            mutableStateListOf<String>().apply {
                addAll(preselectedCategories)
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(availableCategories) { category ->
                        CategoryCheckboxItem(
                            category = category.name,
                            isChecked = selectedCategories.contains(category.name),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    // Добавляем категорию в список выбранных
                                    selectedCategories.add(category.name)
                                } else {
                                    // Удаляем категорию из списка выбранных
                                    selectedCategories.remove(category.name)
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Вызываем функцию импорта с выбранными категориями
                    onImport(selectedCategories.toList())
                },
                enabled = selectedCategories.isNotEmpty(),
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * Элемент списка с чекбоксом для выбора категории
 */
@Composable
private fun CategoryCheckboxItem(
    category: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
