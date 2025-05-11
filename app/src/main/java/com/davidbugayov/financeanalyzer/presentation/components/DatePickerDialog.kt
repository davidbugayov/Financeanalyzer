package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Диалог выбора даты.
 *
 * @param initialDate Начальная дата для отображения в календаре
 * @param minDate Минимальная дата для ограничения выбора
 * @param maxDate Максимальная дата для ограничения выбора
 * @param onDateSelected Callback, вызываемый при выборе даты
 * @param onDismiss Callback, вызываемый при закрытии диалога
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Date,
    minDate: Date? = null,
    maxDate: Date? = null,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val dateBeforeMinimumMessage = stringResource(R.string.date_before_minimum)
    val dateAfterMaximumMessage = stringResource(R.string.date_after_maximum)
    val invalidDateMessage = stringResource(R.string.invalid_date)

    // Показывать Snackbar сразу при выборе недопустимой даты
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val selectedDate = Date(millis)
            val isValid = (minDate == null || selectedDate >= minDate) &&
                    (maxDate == null || selectedDate <= maxDate)
            if (!isValid) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (minDate != null && selectedDate < minDate) {
                            dateBeforeMinimumMessage
                        } else if (maxDate != null && selectedDate > maxDate) {
                            dateAfterMaximumMessage
                        } else {
                            invalidDateMessage
                        }
                    )
                }
            } else {
                snackbarHostState.currentSnackbarData?.dismiss()
            }
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Date(millis)
                        val isValid = (minDate == null || selectedDate >= minDate) &&
                                (maxDate == null || selectedDate <= maxDate)
                        if (isValid) {
                            onDateSelected(selectedDate)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (minDate != null && selectedDate < minDate) {
                                        dateBeforeMinimumMessage
                                    } else if (maxDate != null && selectedDate > maxDate) {
                                        dateAfterMaximumMessage
                                    } else {
                                        invalidDateMessage
                                    }
                                )
                            }
                        }
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
        },
        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        androidx.compose.foundation.layout.Column {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = MaterialTheme.shapes.medium,
                        content = { Text(data.visuals.message) }
                    )
                }
            )
        }
    }
} 