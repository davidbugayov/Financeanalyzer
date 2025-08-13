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
            onConfirm = {
                onChangePeriod(PeriodType.CUSTOM, currentStart, currentEnd)
                showPeriodDialog = false
            },
            onDismiss = { showPeriodDialog = false },
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = currentStart,
            maxDate = runCatching { minOf(currentEnd, Calendar.getInstance().time) }.getOrNull(),
            onDateSelected = { date ->
                currentStart = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = currentEnd,
            minDate = currentStart,
            maxDate = Calendar.getInstance().time,
            onDateSelected = { date ->
                currentEnd = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
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
