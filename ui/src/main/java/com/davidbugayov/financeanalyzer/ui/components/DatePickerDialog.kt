package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R
import java.util.Date
import kotlinx.coroutines.launch

// Вспомогательная функция для конвертации Color в hex строку
fun Color.toHexString(): String {
    val argb = this.toArgb()
    return String.format("#%08X", argb)
}

/**
 * Диалог выбора даты.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Date,
    minDate: Date? = null,
    maxDate: Date? = null,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    // Логи для отладки timezone и цветов
    val currentTimeZone = java.util.TimeZone.getDefault()
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.time

    println("DatePickerDialog DEBUG:")
    println("  Current TimeZone: ${currentTimeZone.id} (${currentTimeZone.displayName})")
    println("  Today: $today (${today.time})")
    println("  Today formatted: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", java.util.Locale.getDefault()).format(today)}")

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    println("  Colors:")
    println("    Primary: ${primaryColor.toHexString()}")
    println("    Surface: ${surfaceColor.toHexString()}")
    println("    OnSurface: ${onSurfaceColor.toHexString()}")
    println("    Error: ${errorColor.toHexString()}")

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            headlineContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    // Исправляем timezone проблему при обработке выбранной даты
                    val selectedDate = java.util.Calendar.getInstance().apply {
                        timeInMillis = millis
                        // Устанавливаем время в начало дня в текущем timezone
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.time
                    onDateSelected(selectedDate)
                }
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxSize(),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
            )
        )
    }
}

/**
 * Диалог выбора диапазона дат.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    initialStartDate: Date? = null,
    initialEndDate: Date? = null,
    minDate: Date? = null,
    maxDate: Date? = null,
    onDateRangeSelected: (startDate: Date, endDate: Date) -> Unit,
    onDismiss: () -> Unit,
) {
    // Логи для отладки timezone и цветов в DateRangePicker
    val currentTimeZone = java.util.TimeZone.getDefault()
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.time

    println("DateRangePickerDialog DEBUG:")
    println("  Current TimeZone: ${currentTimeZone.id} (${currentTimeZone.displayName})")
    println("  Today: $today (${today.time})")
    println("  Today formatted: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", java.util.Locale.getDefault()).format(today)}")

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    println("  Colors:")
    println("    Primary: ${primaryColor.toHexString()}")
    println("    Surface: ${surfaceColor.toHexString()}")
    println("    OnSurface: ${onSurfaceColor.toHexString()}")
    println("    Error: ${errorColor.toHexString()}")
    println("  Initial dates:")
    println("    Start: $initialStartDate ${initialStartDate?.let { "(${it.time})" } ?: ""}")
    println("    End: $initialEndDate ${initialEndDate?.let { "(${it.time})" } ?: ""}")

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate?.time,
        initialSelectedEndDateMillis = initialEndDate?.time,
        initialDisplayMode = DisplayMode.Picker
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            headlineContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        confirmButton = {
            TextButton(onClick = {
                val startMillis = dateRangePickerState.selectedStartDateMillis
                val endMillis = dateRangePickerState.selectedEndDateMillis

                println("DateRangePicker CONFIRM button clicked:")
                println("  Raw startMillis: $startMillis")
                println("  Raw endMillis: $endMillis")

                if (startMillis != null) {
                    // Исправляем timezone проблему при обработке выбранных дат
                    val startDate = java.util.Calendar.getInstance().apply {
                        timeInMillis = startMillis
                        // Устанавливаем время в начало дня в текущем timezone
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.time

                    println("  Corrected startDate: $startDate")
                    println("  StartDate time: ${startDate.time}")

                    // Если конечная дата не выбрана, устанавливаем её автоматически
                    val finalEndDate = if (endMillis != null) {
                        java.util.Calendar.getInstance().apply {
                            timeInMillis = endMillis
                            // Устанавливаем время в конец дня в текущем timezone
                            set(java.util.Calendar.HOUR_OF_DAY, 23)
                            set(java.util.Calendar.MINUTE, 59)
                            set(java.util.Calendar.SECOND, 59)
                            set(java.util.Calendar.MILLISECOND, 999)
                        }.time.also {
                            println("  Corrected endDate: $it")
                            println("  EndDate time: ${it.time}")
                        }
                    } else {
                        // Автоматически устанавливаем конечную дату через 7 дней
                        val calendar = java.util.Calendar.getInstance().apply {
                            time = startDate
                            add(java.util.Calendar.DAY_OF_MONTH, 7)
                        }
                        // Убеждаемся, что конечная дата не превышает максимальную
                        val calculatedEndDate = if (maxDate != null && calendar.time > maxDate) {
                            maxDate
                        } else {
                            calendar.time
                        }
                        println("  Auto-calculated endDate: $calculatedEndDate")
                        calculatedEndDate
                    }

                    println("  Final dates to return:")
                    println("    Start: $startDate (${startDate.time})")
                    println("    End: $finalEndDate (${finalEndDate.time})")

                    onDateRangeSelected(startDate, finalEndDate)
                }
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.fillMaxSize(),
            headline = {
                Row(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    )
                ) {
                    val startDate = dateRangePickerState.selectedStartDateMillis?.let { Date(it) }
                    val endDate = dateRangePickerState.selectedEndDateMillis?.let { Date(it) }

                    if (startDate != null && endDate != null) {
                        Text(
                            text = "${android.text.format.DateFormat.format("dd/MM/yyyy", startDate)} - " +
                                  android.text.format.DateFormat.format("dd/MM/yyyy", endDate),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else if (startDate != null) {
                        val previewEndDate = java.util.Calendar.getInstance().apply {
                            time = startDate
                            add(java.util.Calendar.DAY_OF_MONTH, 7)
                        }.time
                        val adjustedEndDate = if (maxDate != null && previewEndDate > maxDate) maxDate else previewEndDate

                        Text(
                            text = "${android.text.format.DateFormat.format("dd/MM/yyyy", startDate)} - " +
                                  "${android.text.format.DateFormat.format("dd/MM/yyyy", adjustedEndDate)} " +
                                  "(можно изменить)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "Выберите начальную дату диапазона",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        )
    }
}