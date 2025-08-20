package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.ui.R as UiR
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Диалог выбора периода для фильтрации транзакций.
 *
 * @param selectedPeriod Выбранный тип периода
 * @param startDate Начальная дата для произвольного периода
 * @param endDate Конечная дата для произвольного периода
 * @param onPeriodSelected Callback, вызываемый при выборе периода
 * @param onStartDateClick Callback, вызываемый при нажатии на поле начальной даты
 * @param onEndDateClick Callback, вызываемый при нажатии на поле конечной даты
 * @param onConfirm Callback, вызываемый при подтверждении выбора произвольного периода
 * @param onDismiss Callback, вызываемый при закрытии диалога
 */
@Composable
fun PeriodSelectionDialog(
    selectedPeriod: PeriodType,
    startDate: Date,
    endDate: Date,
    onPeriodSelected: (PeriodType) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun rangeFor(type: PeriodType): Pair<Date, Date> {
        val now = java.util.Calendar.getInstance()
        val end = now.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.time

        val startCal = java.util.Calendar.getInstance()
        when (type) {
            PeriodType.DAY -> {
                startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startCal.set(java.util.Calendar.MINUTE, 0)
                startCal.set(java.util.Calendar.SECOND, 0)
                startCal.set(java.util.Calendar.MILLISECOND, 0)
            }
            PeriodType.WEEK -> {
                startCal.add(java.util.Calendar.DAY_OF_MONTH, -6)
                startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startCal.set(java.util.Calendar.MINUTE, 0)
                startCal.set(java.util.Calendar.SECOND, 0)
                startCal.set(java.util.Calendar.MILLISECOND, 0)
            }
            PeriodType.MONTH -> {
                startCal.add(java.util.Calendar.DAY_OF_MONTH, -29)
                startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startCal.set(java.util.Calendar.MINUTE, 0)
                startCal.set(java.util.Calendar.SECOND, 0)
                startCal.set(java.util.Calendar.MILLISECOND, 0)
            }
            PeriodType.QUARTER -> {
                startCal.add(java.util.Calendar.DAY_OF_MONTH, -89)
                startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startCal.set(java.util.Calendar.MINUTE, 0)
                startCal.set(java.util.Calendar.SECOND, 0)
                startCal.set(java.util.Calendar.MILLISECOND, 0)
            }
            PeriodType.YEAR -> {
                startCal.add(java.util.Calendar.DAY_OF_YEAR, -364)
                startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                startCal.set(java.util.Calendar.MINUTE, 0)
                startCal.set(java.util.Calendar.SECOND, 0)
                startCal.set(java.util.Calendar.MILLISECOND, 0)
            }
            PeriodType.ALL -> {
                startCal.set(2000, 0, 1, 0, 0, 0)
                startCal.set(java.util.Calendar.MILLISECOND, 0)
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
                        title = stringResource(UiR.string.period_day, dateFormat.format(s)),
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
                    val (s, e) = rangeFor(PeriodType.MONTH)
                    PeriodOption(
                        periodType = PeriodType.MONTH,
                        selectedPeriod = selectedPeriod,
                        title = stringResource(UiR.string.period_month, dateFormat.format(s), dateFormat.format(e)),
                        onPeriodSelected = onPeriodSelected,
                    )
                }

                PeriodOption(
                    periodType = PeriodType.QUARTER,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(UiR.string.period_quarter),
                    onPeriodSelected = onPeriodSelected,
                )

                run {
                    val (s, e) = rangeFor(PeriodType.YEAR)
                    PeriodOption(
                        periodType = PeriodType.YEAR,
                        selectedPeriod = selectedPeriod,
                        title = stringResource(UiR.string.period_year, dateFormat.format(s), dateFormat.format(e)),
                        onPeriodSelected = onPeriodSelected,
                    )
                }
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

