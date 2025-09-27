package com.davidbugayov.financeanalyzer.feature.statistics.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.ui.R as UiR
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PeriodSelectionDialog(
    selectedPeriod: PeriodType,
    startDate: Date,
    endDate: Date,
    onPeriodSelected: (PeriodType) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onResetDatesToToday: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    // Проверяем, являются ли даты значениями по умолчанию (5 лет назад)
    val isDefaultStartDate =
        remember(startDate) {
            val defaultStart = Calendar.getInstance().apply { add(Calendar.YEAR, -5) }.time
            // Сравниваем даты с точностью до дня
            val startCal = Calendar.getInstance().apply { time = startDate }
            val defaultCal = Calendar.getInstance().apply { time = defaultStart }

            startCal.get(Calendar.YEAR) == defaultCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == defaultCal.get(Calendar.MONTH) &&
                startCal.get(Calendar.DAY_OF_MONTH) == defaultCal.get(Calendar.DAY_OF_MONTH)
        }

    // Также проверяем на дату 2000 года (используется в ALL периоде)
    val is2000YearDate =
        remember(startDate) {
            val startCal = Calendar.getInstance().apply { time = startDate }
            startCal.get(Calendar.YEAR) == 2000
        }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dayMonth = SimpleDateFormat("d MMMM", Locale.getDefault())
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault())
    val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun rangeFor(type: PeriodType): Pair<Date, Date> {
        val now = Calendar.getInstance()
        val end =
            now.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

        val startCal = Calendar.getInstance()
        when (type) {
            PeriodType.DAY -> {
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            PeriodType.WEEK -> {
                startCal.add(Calendar.DAY_OF_MONTH, -6)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            PeriodType.MONTH -> {
                startCal.add(Calendar.DAY_OF_MONTH, -29)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            PeriodType.QUARTER -> {
                startCal.add(Calendar.DAY_OF_MONTH, -89)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            PeriodType.YEAR -> {
                startCal.add(Calendar.DAY_OF_YEAR, -364)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            PeriodType.ALL -> {
                startCal.set(2000, 0, 1, 0, 0, 0)
                startCal.set(Calendar.MILLISECOND, 0)
            }
            PeriodType.CUSTOM -> return startDate to endDate
        }
        return startCal.time to end
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(UiR.string.dialog_select_period_title)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                PeriodOption(
                    periodType = PeriodType.ALL,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(UiR.string.period_all_time),
                    onPeriodSelected = onPeriodSelected,
                )

                run {
                    val (s, _) = rangeFor(PeriodType.DAY)
                    PeriodOption(
                        periodType = PeriodType.DAY,
                        selectedPeriod = selectedPeriod,
                        title = stringResource(UiR.string.period_day, dayMonth.format(s), dayOfWeek.format(s)),
                        onPeriodSelected = onPeriodSelected,
                    )
                }
                run {
                    val (s, e) = rangeFor(PeriodType.WEEK)
                    PeriodOption(
                        periodType = PeriodType.WEEK,
                        selectedPeriod = selectedPeriod,
                        title = stringResource(UiR.string.period_week, dateFormat.format(s), dateFormat.format(e)),
                        onPeriodSelected = onPeriodSelected,
                    )
                }

                run {
                    val (s, _) = rangeFor(PeriodType.MONTH)
                    PeriodOption(
                        periodType = PeriodType.MONTH,
                        selectedPeriod = selectedPeriod,
                        title = stringResource(UiR.string.period_month, monthYear.format(s)),
                        onPeriodSelected = onPeriodSelected,
                    )
                }
                run {
                    val now = Calendar.getInstance()
                    val currentQuarter = ((now.get(Calendar.MONTH) / 3) + 1)
                    val quarterNames = arrayOf("", "I", "II", "III", "IV")
                    val currentYear = now.get(Calendar.YEAR)
                    PeriodOption(
                        periodType = PeriodType.QUARTER,
                        selectedPeriod = selectedPeriod,
                        title = stringResource(UiR.string.period_quarter, quarterNames[currentQuarter], currentYear),
                        onPeriodSelected = onPeriodSelected,
                    )
                }

                run {
                    val now = Calendar.getInstance()
                    val currentYear = now.get(Calendar.YEAR)
                    PeriodOption(
                        periodType = PeriodType.YEAR,
                        selectedPeriod = selectedPeriod,
                        title =
                            LocalContext.current.resources.getQuantityString(
                                UiR.plurals.period_year,
                                currentYear,
                                currentYear,
                            ),
                        onPeriodSelected = onPeriodSelected,
                    )
                }

                // Опция для выбора произвольного периода
                PeriodOption(
                    periodType = PeriodType.CUSTOM,
                    selectedPeriod = selectedPeriod,
                    title =
                        if (selectedPeriod == PeriodType.CUSTOM) {
                            stringResource(
                                UiR.string.period_custom,
                                dateFormat.format(startDate),
                                dateFormat.format(endDate),
                            )
                        } else {
                            stringResource(UiR.string.period_select_custom)
                        },
                    onPeriodSelected = {
                        onPeriodSelected(PeriodType.CUSTOM)
                        // При выборе CUSTOM периода открываем date picker для начальной даты
                        if (it == PeriodType.CUSTOM) {
                            // Если даты по умолчанию (5 лет назад) или 2000 год, сбрасываем их на сегодняшний день
                            if (isDefaultStartDate || is2000YearDate) {
                                onResetDatesToToday()
                            }
                            onStartDateClick()
                        }
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(UiR.string.dialog_close)) }
        },
        dismissButton = {},
    )
}

@Composable
private fun PeriodOption(
    periodType: PeriodType,
    selectedPeriod: PeriodType,
    title: String,
    onPeriodSelected: (PeriodType) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onPeriodSelected(periodType) }
                .padding(vertical = dimensionResource(UiR.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selectedPeriod == periodType,
            onClick = { onPeriodSelected(periodType) },
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = dimensionResource(UiR.dimen.spacing_small)),
        )
    }
}
