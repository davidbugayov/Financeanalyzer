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
import androidx.compose.material3.Button
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

@Composable
fun PeriodSelectionDialog(
    currentPeriodType: PeriodType,
    onPeriodTypeSelected: (PeriodType) -> Unit,
    onDismiss: () -> Unit,
    onShowStartDatePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit,
    startDate: Date,
    endDate: Date
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_period)) },
        text = {
            Column {
                PeriodRadioButton(
                    text = stringResource(R.string.period_all),
                    selected = currentPeriodType == PeriodType.ALL,
                    onClick = {
                        onPeriodTypeSelected(PeriodType.ALL)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_month),
                    selected = currentPeriodType == PeriodType.MONTH,
                    onClick = {
                        onPeriodTypeSelected(PeriodType.MONTH)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_quarter),
                    selected = currentPeriodType == PeriodType.QUARTER,
                    onClick = {
                        onPeriodTypeSelected(PeriodType.QUARTER)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_half_year),
                    selected = currentPeriodType == PeriodType.HALF_YEAR,
                    onClick = {
                        onPeriodTypeSelected(PeriodType.HALF_YEAR)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_year),
                    selected = currentPeriodType == PeriodType.YEAR,
                    onClick = {
                        onPeriodTypeSelected(PeriodType.YEAR)
                        onDismiss()
                    }
                )
                PeriodRadioButton(
                    text = stringResource(R.string.period_custom),
                    selected = currentPeriodType == PeriodType.CUSTOM,
                    onClick = {
                        onPeriodTypeSelected(PeriodType.CUSTOM)
                    }
                )

                if (currentPeriodType == PeriodType.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))

                    DateSelectionRow(
                        label = stringResource(R.string.from_date).split(":")[0],
                        date = startDate,
                        onClick = onShowStartDatePicker
                    )

                    DateSelectionRow(
                        label = stringResource(R.string.to_date).split(":")[0],
                        date = endDate,
                        onClick = onShowEndDatePicker
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (currentPeriodType == PeriodType.CUSTOM) R.string.apply else R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

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

@Composable
fun CategorySelectionDialog(
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    onAddCategory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_category)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Button(
                    onClick = onAddCategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.add_category))
                }

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
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCategorySelected(null)
                onDismiss()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    categoryText: String,
    onCategoryTextChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category_title)) },
        text = {
            OutlinedTextField(
                value = categoryText,
                onValueChange = onCategoryTextChanged,
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