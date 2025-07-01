package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory

/**
 * Диалог выбора категории
 */
@Composable
fun CategoryPickerDialog(
    categories: List<UiCategory>,
    onCategorySelected: (String) -> Unit,
    onCustomCategoryClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_category)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.category_dialog_item_spacing),
                ),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.category_dialog_item_spacing),
                ),
            ) {
                items(categories) { category ->
                    CategoryItemButton(
                        category = category,
                        onClick = {
                            if (category.name == "Другое") {
                                onCustomCategoryClick()
                            } else {
                                onCategorySelected(category.name)
                                onDismiss()
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * Кнопка категории в диалоге выбора
 */
@Composable
fun CategoryItemButton(category: UiCategory, onClick: () -> Unit) {
    val backgroundColor = category.color
    val contentColor = contentColorFor(backgroundColor = backgroundColor)

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            dimensionResource(R.dimen.category_dialog_item_border),
            backgroundColor.copy(alpha = 0.7f),
        ),
        color = backgroundColor,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(dimensionResource(R.dimen.category_dialog_item_padding)),
        ) {
            Icon(
                imageVector = category.icon ?: Icons.Default.Category,
                contentDescription = category.name,
                tint = contentColor,
            )
            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.category_dialog_item_spacing_vertical),
                ),
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
            )
        }
    }
}

/**
 * Диалог добавления пользовательской категории
 */
@Composable
fun CustomCategoryDialog(
    categoryText: String,
    onCategoryTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    availableIcons: List<ImageVector> = emptyList(),
    selectedIcon: ImageVector? = null,
    onIconSelected: (ImageVector) -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = categoryText,
                    onValueChange = onCategoryTextChange,
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                if (availableIcons.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.select_icon),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimensionResource(R.dimen.spacing_tiny),
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            dimensionResource(R.dimen.spacing_tiny),
                        ),
                        contentPadding = PaddingValues(
                            top = dimensionResource(R.dimen.spacing_tiny),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = dimensionResource(
                                    R.dimen.category_dialog_icon_grid_max_height,
                                ) / 2,
                                max = dimensionResource(
                                    R.dimen.category_dialog_icon_grid_max_height,
                                ),
                            ),
                    ) {
                        items(availableIcons) { icon ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = if (icon == selectedIcon) {
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.2f,
                                    )
                                } else {
                                    Color.Transparent
                                },
                                border = if (icon == selectedIcon) {
                                    BorderStroke(
                                        dimensionResource(R.dimen.category_dialog_icon_border),
                                        MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    null
                                },
                                modifier = Modifier
                                    .size(dimensionResource(R.dimen.category_dialog_icon_size))
                                    .clickable { onIconSelected(icon) },
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        dimensionResource(R.dimen.padding_small),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.spacing_small),
                    Alignment.End,
                ),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(
                    onClick = onConfirm,
                    enabled = categoryText.isNotBlank() && selectedIcon != null,
                ) {
                    Text(stringResource(R.string.add_button))
                }
            }
        },
    )
}
