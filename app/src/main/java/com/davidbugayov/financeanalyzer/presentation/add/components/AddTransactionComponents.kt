package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Селектор типа транзакции (доход/расход).
 * Позволяет пользователю выбрать тип добавляемой транзакции.
 *
 * @param isExpense Текущий выбранный тип (true - расход, false - доход)
 * @param onTypeSelected Callback, вызываемый при выборе типа
 */
@Composable
fun TransactionTypeSelector(
    isExpense: Boolean,
    onTypeSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.transaction_type),
            modifier = Modifier.padding(end = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onTypeSelected(true) }
                .padding(end = 16.dp)
        ) {
            RadioButton(
                selected = isExpense,
                onClick = { onTypeSelected(true) }
            )
            Text(
                text = stringResource(R.string.expense_type),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onTypeSelected(false) }
        ) {
            RadioButton(
                selected = !isExpense,
                onClick = { onTypeSelected(false) }
            )
            Text(
                text = stringResource(R.string.income_type),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Поле для ввода названия транзакции.
 * Позволяет пользователю ввести название или описание транзакции.
 *
 * @param title Текущее значение названия
 * @param onTitleChange Callback, вызываемый при изменении названия
 */
@Composable
fun TitleField(
    title: String,
    onTitleChange: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.title)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

/**
 * Поле для ввода суммы транзакции.
 * Позволяет пользователю ввести сумму транзакции.
 * Поддерживает только числовой ввод.
 *
 * @param amount Текущее значение суммы
 * @param onAmountChange Callback, вызываемый при изменении суммы
 */
@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text(stringResource(R.string.amount)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

/**
 * Поле для выбора даты транзакции.
 * При нажатии открывает диалог выбора даты.
 *
 * @param date Текущая выбранная дата
 * @param onClick Callback, вызываемый при нажатии на поле
 */
@Composable
fun DateField(
    date: Date,
    onClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    OutlinedTextField(
        value = dateFormatter.format(date),
        onValueChange = { },
        label = { Text(stringResource(R.string.date)) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.select_date))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick)
    )
}

/**
 * Поле для выбора категории транзакции.
 * При нажатии открывает диалог выбора категории.
 *
 * @param category Текущая выбранная категория
 * @param onClick Callback, вызываемый при нажатии на поле
 */
@Composable
fun CategoryField(
    category: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = category,
        onValueChange = { },
        label = { Text(stringResource(R.string.category)) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.select_category))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick)
    )
}

/**
 * Поле для ввода заметки к транзакции.
 * Позволяет пользователю добавить дополнительную информацию.
 *
 * @param note Текущий текст заметки
 * @param onNoteChange Callback, вызываемый при изменении заметки
 */
@Composable
fun NoteField(
    note: String,
    onNoteChange: (String) -> Unit
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text(stringResource(R.string.note_optional)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

/**
 * Кнопка для добавления транзакции.
 * Становится активной только когда все обязательные поля заполнены.
 *
 * @param enabled Доступность кнопки
 * @param onClick Callback, вызываемый при нажатии на кнопку
 */
@Composable
fun AddTransactionButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(stringResource(R.string.add_button))
    }
}

/**
 * Диалог выбора категории
 */
@Composable
fun CategoryPickerDialog(
    categories: List<CategoryItem>,
    onCategorySelected: (String) -> Unit,
    onCustomCategoryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_category)) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Кнопка категории в диалоге выбора
 */
@Composable
fun CategoryItemButton(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall
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
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category)) },
        text = {
            OutlinedTextField(
                value = categoryText,
                onValueChange = onCategoryTextChange,
                label = { Text(stringResource(R.string.category_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = categoryText.isNotBlank()
            ) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 