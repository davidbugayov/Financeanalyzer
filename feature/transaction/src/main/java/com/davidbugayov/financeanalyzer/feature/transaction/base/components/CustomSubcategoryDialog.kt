package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiSubcategory
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог добавления кастомной сабкатегории
 */
@Composable
fun CustomSubcategoryDialog(
    customSubcategory: String,
    onCustomSubcategoryChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    existingSubcategories: List<UiSubcategory> = emptyList(), // Новый параметр
) {
    var localSubcategory by remember { mutableStateOf(customSubcategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White, // Белый фон как во всех диалогах
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Новая подкатегория",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Показываем существующие подкатегории, если они есть
                if (existingSubcategories.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Существующие подкатегории:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(existingSubcategories) { subcategory ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = subcategory.color.copy(alpha = 0.1f),
                                        contentColor = subcategory.color,
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = subcategory.color,
                                        )
                                        Text(
                                            text = subcategory.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = subcategory.color,
                                        )
                                    }
                                }
                            }
                        }

                        // Разделитель
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        )
                    }
                }

                // Поле ввода новой подкатегории
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Название новой подкатегории:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    OutlinedTextField(
                        value = localSubcategory,
                        onValueChange = {
                            localSubcategory = it
                            onCustomSubcategoryChange(it)
                        },
                        label = { Text("Введите название") },
                        placeholder = { Text("Например: Кафе, Транспорт, Развлечения") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )

                    if (localSubcategory.isBlank()) {
                        Text(
                            text = "💡 Совет: Используйте короткие и понятные названия",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (localSubcategory.isNotBlank()) {
                        onConfirm()
                    }
                },
                enabled = localSubcategory.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(UiR.string.cancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}
