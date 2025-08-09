package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiSubcategory
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог выбора сабкатегории
 */
@Composable
fun SubcategoryPickerDialog(
    subcategories: List<UiSubcategory>,
    onSubcategorySelected: (String) -> Unit,
    onCustomSubcategoryClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(UiR.string.select_subcategory),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface, // Поддержка светлой и темной темы
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (subcategories.isEmpty()) {
                    // Если нет подкатегорий, показываем дружелюбное сообщение
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        )
                        Text(
                            text = "Пока нет подкатегорий",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Создайте первую подкатегорию для этой категории",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    // Заголовок для существующих подкатегорий
                    Text(
                        text = "Выберите подкатегорию:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 200.dp),
                    ) {
                        items(subcategories) { subcategory ->
                            SubcategoryItemButton(
                                subcategory = subcategory,
                                onClick = {
                                    onSubcategorySelected(subcategory.name)
                                    onDismiss()
                                },
                            )
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

                // Кнопка создания новой подкатегории (всегда видна)
                Surface(
                    onClick = {
                        onCustomSubcategoryClick()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border =
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Создать новую подкатегорию",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Добавьте свою подкатегорию",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(UiR.string.cancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

/**
 * Кнопка сабкатегории в диалоге выбора
 */
@Composable
fun SubcategoryItemButton(
    subcategory: UiSubcategory,
    onClick: () -> Unit,
) {
    val backgroundColor = subcategory.color.copy(alpha = 0.1f)
    val contentColor = subcategory.color

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // Более округлые углы
        border =
            BorderStroke(
                1.5.dp, // Немного толще граница
                subcategory.color.copy(alpha = 0.3f),
            ),
        color = backgroundColor,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp), // Больше отступы
        ) {
            // Иконка в круглом фоне
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = subcategory.name,
                    tint = contentColor,
                    modifier =
                        Modifier
                            .size(20.dp)
                            .padding(10.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subcategory.name,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    ),
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(32.dp),
            )
        }
    }
}
