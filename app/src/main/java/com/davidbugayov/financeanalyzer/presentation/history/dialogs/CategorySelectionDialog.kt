package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R

/**
 * Диалог выбора категории для фильтрации транзакций.
 *
 * @param selectedCategory Выбранная категория или null, если не выбрана
 * @param expenseCategories Список категорий расходов
 * @param incomeCategories Список категорий доходов
 * @param onCategorySelected Callback, вызываемый при выборе категории
 * @param onCategoryDelete Callback, вызываемый при удалении категории
 * @param onDismiss Callback, вызываемый при закрытии диалога
 */
@Composable
fun CategorySelectionDialog(
    selectedCategory: String?,
    expenseCategories: List<String>,
    incomeCategories: List<String>,
    onCategorySelected: (String?) -> Unit,
    onCategoryDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
                        .clickable { onCategorySelected(null) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.all_categories),
                        color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Заголовок "Расходы"
                Text(
                    text = stringResource(R.string.expenses),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Список категорий расходов
                expenseCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategory == category,
                        onCategorySelected = { onCategorySelected(category) },
                        onCategoryDelete = { onCategoryDelete(category) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Заголовок "Доходы"
                Text(
                    text = stringResource(R.string.income),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Список категорий доходов
                incomeCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategory == category,
                        onCategorySelected = { onCategorySelected(category) },
                        onCategoryDelete = { onCategoryDelete(category) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

/**
 * Элемент списка категорий с возможностью удаления.
 *
 * @param category Название категории
 * @param isSelected Выбрана ли категория
 * @param onCategorySelected Callback, вызываемый при выборе категории
 * @param onCategoryDelete Callback, вызываемый при удалении категории
 */
@Composable
private fun CategoryItem(
    category: String,
    isSelected: Boolean,
    onCategorySelected: () -> Unit,
    onCategoryDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onCategorySelected() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onCategoryDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_category),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 