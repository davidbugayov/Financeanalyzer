package com.davidbugayov.financeanalyzer.feature.transaction.base.components
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.R as UiR
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
    // Новые параметры для кнопки подкатегории
    onSubcategoryButtonClick: (() -> Unit)? = null,
    selectedSubcategory: String = "",
    hasAvailableSubcategories: Boolean = false,
) {
    val maxRows = 3
    val columns = 4
    val maxVisibleCategories = maxRows * columns - 1 // 11 категорий + 1 кнопка
    val showExpand = categories.size > maxVisibleCategories
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val visibleCategories =
        if (expanded || !showExpand) {
            categories
        } else {
            categories.take(
                maxVisibleCategories,
            )
        }

    val errorBackgroundColor = LocalErrorStateBackgroundColor.current
    val errorContentColor = LocalErrorStateContentColor.current

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    horizontal = dimensionResource(UiR.dimen.category_section_padding_horizontal),
                    vertical = 0.dp,
                ),
        verticalArrangement =
            Arrangement.spacedBy(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.wrapContentHeight(),
            ) {
                Text(
                    text = stringResource(UiR.string.category) + " *",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        ),
                    modifier = Modifier.padding(end = 8.dp),
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )

                // Кнопка выбора подкатегории (показывается только если выбрана категория)
                if (selectedCategory.isNotBlank() && onSubcategoryButtonClick != null) {
                    Column {
                        Box(
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .clickable { onSubcategoryButtonClick() }
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        CircleShape,
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text =
                                        if (selectedSubcategory.isNotBlank()) {
                                            selectedSubcategory
                                        } else {
                                             stringResource(UiR.string.subcategory)
                                        },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                )
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        // Индикатор наличия подкатегорий
                        Text(
                            text = when {
                                selectedSubcategory.isNotBlank() -> stringResource(UiR.string.subcategory_selected)
                                hasAvailableSubcategories -> stringResource(UiR.string.has_subcategories)
                                else -> stringResource(UiR.string.no_subcategories)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 8.dp, top = 1.dp),
                        )
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (expanded || !showExpand) {
                            Modifier.heightIn(
                                max = dimensionResource(UiR.dimen.category_max_height),
                            )
                        } else {
                            Modifier.height(dimensionResource(UiR.dimen.category_collapsed_height))
                        },
                    ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    dimensionResource(UiR.dimen.category_grid_spacing),
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    dimensionResource(UiR.dimen.category_grid_spacing),
                ),
            userScrollEnabled = expanded || !showExpand,
        ) {
            items(visibleCategories) { category ->
                contentColorFor(backgroundColor = category.color)
                val selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .width(dimensionResource(UiR.dimen.category_item_width))
                            .combinedClickable(
                                onClick = { onCategorySelected(category) },
                                onLongClick = { onCategoryLongClick(category) },
                            )
                            .padding(
                                vertical = dimensionResource(UiR.dimen.category_item_vertical_padding),
                            ),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(dimensionResource(UiR.dimen.category_item_circle_size))
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError && selectedCategory.isBlank() -> errorBackgroundColor
                                        category.name == selectedCategory -> MaterialTheme.colorScheme.primaryContainer
                                        else -> category.color
                                    },
                                )
                                .border(
                                    width =
                                        when {
                                            category.name == selectedCategory -> 2.dp
                                            isError && selectedCategory.isBlank() -> 2.dp
                                            else -> 1.dp
                                        },
                                    color =
                                        when {
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
                            tint =
                                when {
                                    isError && selectedCategory.isBlank() -> errorContentColor
                                    category.name == selectedCategory -> selectedContentColor
                                    else -> Color.White
                                },
                            modifier =
                                Modifier.size(
                                     dimensionResource(UiR.dimen.category_item_icon_size),
                                ),
                        )
                    }
                    Spacer(
                        modifier =
                            Modifier.height(
                                dimensionResource(UiR.dimen.category_item_spacer_height),
                            ),
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        color =
                            when {
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
                    modifier =
                        Modifier
                            .width(dimensionResource(UiR.dimen.category_item_width))
                            .padding(
                                 vertical = dimensionResource(UiR.dimen.category_item_vertical_padding),
                            ),
                )
            }
        }
        // Expand/Collapse button под гридом
        if (showExpand) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextButton(onClick = { setExpanded(!expanded) }) {
                    Text(
                        text = stringResource(if (expanded) R.string.hide_categories else R.string.show_more_categories),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/**
 * Элемент добавления новой категории
 */
@Composable
fun AddCategoryItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .width(dimensionResource(UiR.dimen.category_item_width))
                .clickable(onClick = onClick)
                .padding(vertical = dimensionResource(UiR.dimen.category_item_vertical_padding)),
    ) {
        Box(
            modifier =
                Modifier
                    .size(dimensionResource(UiR.dimen.category_item_circle_size))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = dimensionResource(UiR.dimen.border_width_small),
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_custom_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(UiR.dimen.category_item_icon_size)),
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.category_item_spacer_height)))

        Text(
            text = stringResource(R.string.add_custom_category),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
