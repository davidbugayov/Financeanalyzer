package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.ui.theme.LocalErrorStateBackgroundColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalErrorStateContentColor

/**
 * Секция выбора категории
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySection(
    categories: List<UiCategory>,
    selectedCategory: String,
    onCategorySelected: (UiCategory) -> Unit,
    onAddCategoryClick: () -> Unit,
    onCategoryLongClick: (UiCategory) -> Unit = {},
    isError: Boolean = false,
) {
    val maxRows = 2
    val columns = 4
    val maxVisibleCategories = maxRows * columns - 1 // 7 категорий + 1 кнопка
    val showExpand = categories.size > maxVisibleCategories
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val visibleCategories = if (expanded || !showExpand) {
        categories
    } else {
        categories.take(
            maxVisibleCategories,
        )
    }

    val errorBackgroundColor = LocalErrorStateBackgroundColor.current
    val errorContentColor = LocalErrorStateContentColor.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.category_section_padding_horizontal),
                vertical = dimensionResource(R.dimen.category_section_padding_vertical),
            ),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.category_section_spacing),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.category_header_spacing),
                ),
            ) {
                Text(
                    text = stringResource(R.string.category) + " *",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    ),
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.category_header_padding_horizontal),
                        vertical = dimensionResource(R.dimen.category_header_padding_vertical),
                    ),
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
            if (showExpand) {
                Spacer(
                    modifier = Modifier.width(
                        dimensionResource(R.dimen.category_expand_spacer_width),
                    ),
                )
                if (!expanded) {
                    Text(
                        text = stringResource(R.string.show_more_categories),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { setExpanded(true) }
                            .padding(
                                vertical = dimensionResource(
                                    R.dimen.category_expand_text_padding_vertical,
                                ),
                            ),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.hide_categories),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { setExpanded(false) }
                            .padding(
                                vertical = dimensionResource(
                                    R.dimen.category_expand_text_padding_vertical,
                                ),
                            ),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expanded || !showExpand) {
                        Modifier.heightIn(
                            max = dimensionResource(R.dimen.category_max_height),
                        )
                    } else {
                        Modifier.height(dimensionResource(R.dimen.category_collapsed_height))
                    },
                ),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.category_grid_spacing),
            ),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.category_grid_spacing),
            ),
            userScrollEnabled = expanded || !showExpand,
        ) {
            items(visibleCategories) { category ->
                contentColorFor(backgroundColor = category.color)
                val selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.category_item_width))
                        .combinedClickable(
                            onClick = { onCategorySelected(category) },
                            onLongClick = { onCategoryLongClick(category) },
                        )
                        .padding(
                            vertical = dimensionResource(R.dimen.category_item_vertical_padding),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.category_item_circle_size))
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError && selectedCategory.isBlank() -> errorBackgroundColor
                                    category.name == selectedCategory -> MaterialTheme.colorScheme.primaryContainer
                                    else -> category.color
                                },
                            )
                            .border(
                                width = when {
                                    category.name == selectedCategory -> 2.dp
                                    isError && selectedCategory.isBlank() -> 2.dp
                                    else -> 1.dp
                                },
                                color = when {
                                    category.name == selectedCategory -> MaterialTheme.colorScheme.primary
                                    isError && selectedCategory.isBlank() -> MaterialTheme.colorScheme.error
                                    else -> category.color.copy(alpha = 0.5f)
                                },
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = category.icon ?: Icons.Default.Category,
                            contentDescription = category.name,
                            tint = when {
                                isError && selectedCategory.isBlank() -> errorContentColor
                                category.name == selectedCategory -> selectedContentColor
                                else -> Color.White
                            },
                            modifier = Modifier.size(
                                dimensionResource(R.dimen.category_item_icon_size),
                            ),
                        )
                    }
                    Spacer(
                        modifier = Modifier.height(
                            dimensionResource(R.dimen.category_item_spacer_height),
                        ),
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        color = when {
                            isError && selectedCategory.isBlank() -> errorContentColor
                            category.name == selectedCategory -> selectedContentColor
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
            item {
                AddCategoryItem(
                    onClick = onAddCategoryClick,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.category_item_width))
                        .padding(
                            vertical = dimensionResource(R.dimen.category_item_vertical_padding),
                        ),
                )
            }
        }
    }
}

/**
 * Элемент добавления новой категории
 */
@Composable
fun AddCategoryItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(dimensionResource(R.dimen.category_item_width))
            .clickable(onClick = onClick)
            .padding(vertical = dimensionResource(R.dimen.category_item_vertical_padding)),
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.category_item_circle_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = dimensionResource(R.dimen.border_width_small),
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_custom_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(R.dimen.category_item_icon_size)),
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.category_item_spacer_height)))

        Text(
            text = stringResource(R.string.add_custom_category),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
