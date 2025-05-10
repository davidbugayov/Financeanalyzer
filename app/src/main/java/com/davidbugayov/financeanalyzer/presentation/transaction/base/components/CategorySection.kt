package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
    isError: Boolean = false
) {
    val maxRows = 2
    val columns = 4
    val maxVisibleCategories = maxRows * columns - 1 // 7 категорий + 1 кнопка
    val showExpand = categories.size > maxVisibleCategories
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val visibleCategories = if (expanded || !showExpand) categories else categories.take(maxVisibleCategories)

    val errorBackgroundColor = LocalErrorStateBackgroundColor.current
    val errorContentColor = LocalErrorStateContentColor.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.category) + " *",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expanded || !showExpand) Modifier.heightIn(max = 400.dp)
                    else Modifier.height(120.dp)
                ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = expanded || !showExpand
        ) {
            items(visibleCategories) { category ->
                val contentColor = contentColorFor(backgroundColor = category.color)
                val selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(56.dp)
                        .combinedClickable(
                            onClick = { onCategorySelected(category) },
                            onLongClick = { onCategoryLongClick(category) }
                        )
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError && selectedCategory.isBlank() -> errorBackgroundColor
                                    category.name == selectedCategory -> MaterialTheme.colorScheme.primaryContainer
                                    else -> category.color
                                }
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
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon ?: Icons.Default.Category,
                            contentDescription = category.name,
                            tint = when {
                                isError && selectedCategory.isBlank() -> errorContentColor
                                category.name == selectedCategory -> selectedContentColor
                                else -> contentColor
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        color = when {
                            isError && selectedCategory.isBlank() -> errorContentColor
                            category.name == selectedCategory -> selectedContentColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            item {
                AddCategoryItem(
                    onClick = onAddCategoryClick,
                    modifier = Modifier
                        .width(64.dp)
                        .padding(vertical = 2.dp)
                )
            }
        }
        if (showExpand) {
            Spacer(modifier = Modifier.height(4.dp))
            if (!expanded) {
                Text(
                    text = "Показать ещё",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { setExpanded(true) }
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Скрыть",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { setExpanded(false) }
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
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
            .width(56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_custom_category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(R.string.add_custom_category),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
} 