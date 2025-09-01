package com.davidbugayov.financeanalyzer.presentation.chart.statistic.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.statistics.dialogs.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.util.UiUtils
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.ui.components.DateRangePickerDialog
import com.davidbugayov.financeanalyzer.utils.DateUtils
import java.util.Calendar
import java.util.Date

/**
 * Универсальная панель фильтра периода с локализованным отображением.
 * Переиспользуется на экранах статистики.
 *
 * @param periodType Текущий тип периода
 * @param startDate Начальная дата периода
 * @param endDate Конечная дата периода
 * @param onChangePeriod Колбэк при подтверждении выбора периода
 */
@Composable
fun PeriodFilterBar(
    periodType: PeriodType,
    startDate: Date,
    endDate: Date,
    onChangePeriod: (PeriodType, Date, Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedPeriod by remember { mutableStateOf(periodType) }
    var currentStart by remember { mutableStateOf(startDate) }
    var currentEnd by remember { mutableStateOf(endDate) }

    LaunchedEffect(periodType, startDate, endDate) {
        selectedPeriod = periodType
        currentStart = startDate
        currentEnd = endDate
    }

    if (showPeriodDialog) {
        PeriodSelectionDialog(
            selectedPeriod = selectedPeriod,
            startDate = currentStart,
            endDate = currentEnd,
            onPeriodSelected = { type ->
                selectedPeriod = type
                if (type != PeriodType.CUSTOM) {
                    val (ns, ne) =
                        DateUtils.updatePeriodDates(
                            periodType = type,
                            currentStartDate = currentStart,
                            currentEndDate = currentEnd,
                        )
                    currentStart = ns
                    currentEnd = ne
                    onChangePeriod(type, ns, ne)
                    showPeriodDialog = false
                }
            },
            onStartDateClick = { showStartDatePicker = true },
            onEndDateClick = { showEndDatePicker = true },
            onResetDatesToToday = {
                // Сбрасываем даты на сегодняшний день, если они по умолчанию
                val startCal = java.util.Calendar.getInstance().apply { time = currentStart }
                val isDefaultOr2000 = startCal.get(java.util.Calendar.YEAR) <= 2000 ||
                    java.util.Calendar.getInstance().apply { add(java.util.Calendar.YEAR, -5) }.time == currentStart

                if (isDefaultOr2000) {
                    val today = java.util.Calendar.getInstance().time
                    currentStart = today
                    currentEnd = today
                }
            },
            onConfirm = {
                onChangePeriod(PeriodType.CUSTOM, currentStart, currentEnd)
                showPeriodDialog = false
            },
            onDismiss = { showPeriodDialog = false },
        )
    }

    if (showStartDatePicker) {
        // Определяем начальные даты для DateRangePicker
        val (initialStart, initialEnd) = remember(currentStart, currentEnd) {
            val today = Calendar.getInstance()
            val startDateCal = Calendar.getInstance().apply { time = currentStart }
            val endDateCal = Calendar.getInstance().apply { time = currentEnd }

            // Если даты по умолчанию (5 лет назад), используем разумный диапазон
            if (startDateCal.get(Calendar.YEAR) <= 2000 ||
                Calendar.getInstance().apply { add(Calendar.YEAR, -4) }.time <= currentStart) {
                // Начало месяца назад, конец - сегодня
                val startOfMonth = today.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
                val todayEnd = today.time
                startOfMonth to todayEnd
            } else {
                // Используем текущие даты из состояния
                currentStart to currentEnd
            }
        }

        DateRangePickerDialog(
            initialStartDate = initialStart,
            initialEndDate = initialEnd,
            maxDate = Calendar.getInstance().time,
            onDateRangeSelected = { startDate, endDate ->
                println("PeriodFilterBar: onDateRangeSelected called")
                println("  Received startDate: $startDate (${startDate.time})")
                println("  Received endDate: $endDate (${endDate.time})")

                currentStart = startDate
                currentEnd = endDate
                showStartDatePicker = false
                showEndDatePicker = false
            },
            onDismiss = {
                showStartDatePicker = false
                showEndDatePicker = false
            },
        )
    }

    val formatted =
        UiUtils.formatPeriod(
            context = androidx.compose.ui.platform.LocalContext.current,
            periodType = selectedPeriod,
            startDate = currentStart,
            endDate = currentEnd,
        )

    Row(
        modifier = modifier.clickable { showPeriodDialog = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = stringResource(UiR.string.select_period),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(
            text = formatted,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        )
    }
}
