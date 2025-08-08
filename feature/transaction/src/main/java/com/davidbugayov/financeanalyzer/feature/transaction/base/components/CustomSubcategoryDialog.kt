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

import androidx.compose.ui.res.dimensionResource
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
        containerColor = MaterialTheme.colorScheme.surface, // Поддержка светлой и темной темы
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.spacing_small)),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.dialog_new_subcategory),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.spacing_large)),
            ) {
                // Показываем существующие подкатегории, если они есть
                if (existingSubcategories.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.spacing_small)),
                    ) {
                        Text(
                            text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.dialog_existing_subcategories),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.spacing_small)),
                        ) {
                            items(existingSubcategories) { subcategory ->
                                Card(
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = subcategory.color.copy(alpha = 0.1f),
                                            contentColor = subcategory.color,
                                        ),
                                    shape = RoundedCornerShape(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.radius_card)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.card_horizontal_padding),
                                            vertical = dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.card_vertical_padding)
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.spacing_tiny)),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            modifier = Modifier.size(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.icon_size_small)),
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
                            modifier =
                                Modifier
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
                        text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.dialog_new_subcategory_name_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    OutlinedTextField(
                        value = localSubcategory,
                        onValueChange = {
                            localSubcategory = it
                            onCustomSubcategoryChange(it)
                        },
                        label = { Text(stringResource(com.davidbugayov.financeanalyzer.ui.R.string.input_hint_enter_name)) },
                        placeholder = { Text(stringResource(com.davidbugayov.financeanalyzer.ui.R.string.input_placeholder_examples)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.radius_12dp)),
                    )

                    if (localSubcategory.isBlank()) {
                        Text(
                            text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.tip_use_short_names),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.spacing_tiny)),
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
                shape = RoundedCornerShape(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.radius_small)),
            ) {
                Text(stringResource(com.davidbugayov.financeanalyzer.ui.R.string.create))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(dimensionResource(com.davidbugayov.financeanalyzer.ui.R.dimen.radius_small)),
            ) {
                Text(
                    text = stringResource(UiR.string.cancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}
