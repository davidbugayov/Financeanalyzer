package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.history.components.CategoryButton
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Диалог выбора периода для фильтрации транзакций.
 * Позволяет выбрать предустановленный период или указать произвольный диапазон дат.
 *
 * @param selectedPeriod Текущий выбранный период
 * @param startDate Начальная дата для произвольного периода
 * @param endDate Конечная дата для произвольного периода
 * @param onPeriodSelected Callback, вызываемый при выборе периода
 * @param onStartDateClick Callback для открытия диалога выбора начальной даты
 * @param onEndDateClick Callback для открытия диалога выбора конечной даты
 * @param onDismiss Callback для закрытия диалога
 */
@Composable
fun PeriodSelectionDialog(
    selectedPeriod: PeriodType,
    startDate: Date,
    endDate: Date,
    onPeriodSelected: (PeriodType) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_period)) },
        text = {
            Column {
                PeriodRadioButton(
                    text = stringResource(R.string.period_all),
                    selected = selectedPeriod == PeriodType.ALL,
                    onClick = {
                        onPeriodSelected(PeriodType.ALL)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_month),
                    selected = selectedPeriod == PeriodType.MONTH,
                    onClick = {
                        onPeriodSelected(PeriodType.MONTH)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_quarter),
                    selected = selectedPeriod == PeriodType.QUARTER,
                    onClick = {
                        onPeriodSelected(PeriodType.QUARTER)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_half_year),
                    selected = selectedPeriod == PeriodType.HALF_YEAR,
                    onClick = {
                        onPeriodSelected(PeriodType.HALF_YEAR)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_year),
                    selected = selectedPeriod == PeriodType.YEAR,
                    onClick = {
                        onPeriodSelected(PeriodType.YEAR)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_custom),
                    selected = selectedPeriod == PeriodType.CUSTOM,
                    onClick = {
                        onPeriodSelected(PeriodType.CUSTOM)
                    }
                )

                if (selectedPeriod == PeriodType.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))

                    DateSelectionRow(
                        label = stringResource(R.string.from_date).split(":")[0],
                        date = startDate,
                        onClick = onStartDateClick
                    )

                    DateSelectionRow(
                        label = stringResource(R.string.to_date).split(":")[0],
                        date = endDate,
                        onClick = onEndDateClick
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (selectedPeriod == PeriodType.CUSTOM) R.string.apply else R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Компонент радиокнопки для выбора периода.
 * Отображает радиокнопку с текстом и обрабатывает нажатия.
 *
 * @param text Текст рядом с радиокнопкой
 * @param selected Выбрана ли радиокнопка
 * @param onClick Callback, вызываемый при нажатии
 */
@Composable
private fun PeriodRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(48.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Строка выбора даты в диалоге периода.
 * Отображает метку и текущую дату с иконкой календаря.
 * При нажатии открывает диалог выбора даты.
 *
 * @param label Метка (например, "С" или "По")
 * @param date Выбранная дата
 * @param onClick Callback для открытия календаря
 */
@Composable
private fun DateSelectionRow(
    label: String,
    date: Date,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null
            )
        }
    }
}

/**
 * Диалог выбора даты.
 * Использует стандартный DatePicker из Material3.
 *
 * @param initialDate Начальная дата для отображения в календаре
 * @param onDateSelected Callback, вызываемый при выборе даты
 * @param onDismiss Callback для закрытия диалога
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Date(it))
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Диалог выбора категории для фильтрации транзакций.
 * Отображает список доступных категорий и возможность удалить категорию по долгому нажатию.
 *
 * @param selectedCategory Текущая выбранная категория
 * @param categories Список доступных категорий
 * @param onCategorySelected Callback, вызываемый при выборе категории
 * @param onCategoryLongClick Callback, вызываемый при долгом нажатии на категорию
 * @param onDismiss Callback для закрытия диалога
 */
@Composable
fun CategorySelectionDialog(
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    onCategoryLongClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_category)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                CategoryButton(
                    text = stringResource(R.string.all),
                    selected = selectedCategory == null,
                    onClick = {
                        onCategorySelected(null)
                        onDismiss()
                    }
                )

                categories.forEach { category ->
                    CategoryButton(
                        text = category,
                        selected = category == selectedCategory,
                        onClick = {
                            onCategorySelected(category)
                            onDismiss()
                        },
                        onLongClick = {
                            onCategoryLongClick(category)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

/**
 * Диалог добавления новой категории.
 * Позволяет пользователю ввести название новой категории.
 *
 * @param categoryText Текущий текст в поле ввода
 * @param onCategoryTextChange Callback, вызываемый при изменении текста
 * @param onConfirm Callback для подтверждения добавления категории
 * @param onDismiss Callback для закрытия диалога
 */
@Composable
fun AddCategoryDialog(
    categoryText: String,
    onCategoryTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category_title)) },
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

/**
 * Диалог подтверждения удаления категории.
 *
 * @param category Название категории для удаления
 * @param onConfirm Callback для подтверждения удаления
 * @param onDismiss Callback для отмены удаления
 * @param isDefaultCategory Флаг, указывающий, является ли категория стандартной
 */
@Composable
fun DeleteCategoryConfirmDialog(
    category: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDefaultCategory: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_category)) },
        text = {
            Text(
                stringResource(
                    if (isDefaultCategory) R.string.delete_default_category_confirmation
                    else R.string.delete_custom_category_confirmation,
                    category
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 