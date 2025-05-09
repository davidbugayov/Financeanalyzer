package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

/**
 * Диалог выбора категорий для фильтрации транзакций.
 *
 * @param selectedCategories Список выбранных категорий
 * @param expenseCategories Список категорий расходов
 * @param incomeCategories Список категорий доходов
 * @param onCategoriesSelected Callback, вызываемый при выборе категорий
 * @param onDismiss Callback, вызываемый при закрытии диалога
 */
@Composable
fun CategorySelectionDialog(
    selectedCategories: List<String>,
    expenseCategories: List<String>,
    incomeCategories: List<String>,
    onCategoriesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Состояние для раскрытия/скрытия групп категорий
    var isExpensesExpanded by remember { mutableStateOf(true) }
    var isIncomeExpanded by remember { mutableStateOf(true) }

    // Локальное состояние для хранения выбранных категорий
    var localSelectedCategories by remember(selectedCategories) {
        mutableStateOf(selectedCategories)
    }

    // Вычисляем список всех категорий
    val allCategories = remember(expenseCategories, incomeCategories) {
        expenseCategories + incomeCategories
    }

    // Цвета для категорий
    val expenseColor = LocalExpenseColor.current
    val incomeColor = LocalIncomeColor.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_category)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Опция "Все категории"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Если не все категории выбраны - выбираем все,
                            // иначе - очищаем выбор
                            localSelectedCategories =
                                if (localSelectedCategories.size != allCategories.size) {
                                    allCategories
                                } else {
                                    emptyList()
                                }
                        }
                        .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (localSelectedCategories.isEmpty())
                            stringResource(R.string.all_categories)
                        else if (localSelectedCategories.size == allCategories.size)
                            stringResource(R.string.clear_selection)
                        else
                            stringResource(R.string.select_all_categories),
                        color = if (localSelectedCategories.isEmpty() ||
                            localSelectedCategories.size == allCategories.size
                        )
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Чекбокс "Выбрать все"
                    Checkbox(
                        checked = localSelectedCategories.size == allCategories.size && allCategories.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            localSelectedCategories = if (isChecked) {
                                allCategories
                            } else {
                                emptyList()
                            }
                        }
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.divider_height)),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                // Группа "Расходы" с выпадающим списком
                CategoryGroupHeader(
                    title = stringResource(R.string.expenses),
                    isExpanded = isExpensesExpanded,
                    color = expenseColor,
                    onToggle = { isExpensesExpanded = !isExpensesExpanded },
                    onSelectAll = { isChecked ->
                        localSelectedCategories = if (isChecked) {
                            allCategories
                        } else {
                            emptyList()
                        }
                    },
                    isAllSelected = localSelectedCategories.size == allCategories.size && allCategories.isNotEmpty()
                )

                AnimatedVisibility(visible = isExpensesExpanded) {
                    Column {
                        expenseCategories.forEach { category ->
                            CategoryCheckboxItem(
                                category = category,
                                isSelected = localSelectedCategories.contains(category),
                                color = expenseColor,
                                onToggle = { isChecked ->
                                    localSelectedCategories = if (isChecked) {
                                        localSelectedCategories + category
                                    } else {
                                        localSelectedCategories - category
                                    }
                                }
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.divider_height)),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                // Группа "Доходы" с выпадающим списком
                CategoryGroupHeader(
                    title = stringResource(R.string.income),
                    isExpanded = isIncomeExpanded,
                    color = incomeColor,
                    onToggle = { isIncomeExpanded = !isIncomeExpanded },
                    onSelectAll = { isChecked ->
                        localSelectedCategories = if (isChecked) {
                            allCategories
                        } else {
                            emptyList()
                        }
                    },
                    isAllSelected = localSelectedCategories.size == allCategories.size && allCategories.isNotEmpty()
                )

                AnimatedVisibility(visible = isIncomeExpanded) {
                    Column {
                        incomeCategories.forEach { category ->
                            CategoryCheckboxItem(
                                category = category,
                                isSelected = localSelectedCategories.contains(category),
                                color = incomeColor,
                                onToggle = { isChecked ->
                                    localSelectedCategories = if (isChecked) {
                                        localSelectedCategories + category
                                    } else {
                                        localSelectedCategories - category
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCategoriesSelected(localSelectedCategories)
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
 * Заголовок группы категорий с возможностью раскрытия/скрытия содержимого.
 */
@Composable
private fun CategoryGroupHeader(
    title: String,
    isExpanded: Boolean,
    color: Color,
    onToggle: () -> Unit,
    onSelectAll: (Boolean) -> Unit,
    isAllSelected: Boolean
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Название группы и иконка раскрытия
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded)
                    stringResource(R.string.collapse)
                else
                    stringResource(R.string.expand),
                modifier = Modifier.rotate(rotation),
                tint = color
            )
        }

        // Чекбокс "Выбрать все"
        Checkbox(
            checked = isAllSelected,
            onCheckedChange = onSelectAll
        )
    }
}

/**
 * Элемент списка категорий с чекбоксом.
 *
 * @param category Название категории
 * @param isSelected Выбрана ли категория
 * @param color Цвет категории
 * @param onToggle Callback, вызываемый при выборе/отмене выбора категории
 */
@Composable
private fun CategoryCheckboxItem(
    category: String,
    isSelected: Boolean,
    color: Color,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small))
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { onToggle(!isSelected) })
                .padding(vertical = dimensionResource(R.dimen.spacing_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle
            )
            
            Text(
                text = category,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else color,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimensionResource(R.dimen.spacing_small))
            )
        }
    }
} 