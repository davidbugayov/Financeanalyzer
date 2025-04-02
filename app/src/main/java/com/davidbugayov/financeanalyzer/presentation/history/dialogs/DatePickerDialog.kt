package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import java.util.Date

/**
 * Диалог выбора даты.
 *
 * @param initialDate Начальная дата для отображения в календаре
 * @param limitDate Ограничение для выбора даты (для диапазона дат)
 * @param isStartDate Является ли дата начальной в диапазоне (для определения ограничений)
 * @param onDateSelected Callback, вызываемый при выборе даты
 * @param onDismiss Callback, вызываемый при закрытии диалога
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Date,
    limitDate: Date? = null,
    isStartDate: Boolean = true,
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
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
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