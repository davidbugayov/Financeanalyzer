package com.davidbugayov.financeanalyzer.presentation.history.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
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
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_period)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Все время
                PeriodOption(
                    periodType = PeriodType.ALL,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.all_time),
                    onPeriodSelected = onPeriodSelected
                )

                // День
                PeriodOption(
                    periodType = PeriodType.DAY,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.day),
                    onPeriodSelected = onPeriodSelected
                )

                // Неделя
                PeriodOption(
                    periodType = PeriodType.WEEK,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.week),
                    onPeriodSelected = onPeriodSelected
                )

                // Месяц
                PeriodOption(
                    periodType = PeriodType.MONTH,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.month),
                    onPeriodSelected = onPeriodSelected
                )

                // Квартал
                PeriodOption(
                    periodType = PeriodType.QUARTER,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.period_quarter),
                    onPeriodSelected = onPeriodSelected
                )

                // Год
                PeriodOption(
                    periodType = PeriodType.YEAR,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.year),
                    onPeriodSelected = onPeriodSelected
                )

                // Произвольный период
                PeriodOption(
                    periodType = PeriodType.CUSTOM,
                    selectedPeriod = selectedPeriod,
                    title = stringResource(R.string.custom_period),
                    onPeriodSelected = onPeriodSelected
                )

                // Если выбран произвольный период, показываем поля для выбора дат
                if (selectedPeriod == PeriodType.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Начальная дата
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartDateClick() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.start_date),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = dateFormat.format(startDate),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Конечная дата
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEndDateClick() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.end_date),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = dateFormat.format(endDate),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

/**
 * Опция выбора периода с радиокнопкой.
 *
 * @param periodType Тип периода
 * @param selectedPeriod Выбранный тип периода
 * @param title Название периода
 * @param onPeriodSelected Callback, вызываемый при выборе периода
 */
@Composable
private fun PeriodOption(
    periodType: PeriodType,
    selectedPeriod: PeriodType,
    title: String,
    onPeriodSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPeriodSelected(periodType) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selectedPeriod == periodType,
            onClick = { onPeriodSelected(periodType) }
        )
        Text(text = title)
    }
} 