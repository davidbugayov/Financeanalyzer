package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

/**
 * Общий компонент с фильтрами транзакций для главного экрана
 */
@Composable
fun TransactionFilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == TransactionFilter.TODAY,
            onClick = { onFilterSelected(TransactionFilter.TODAY) },
            label = { Text(stringResource(R.string.filter_today)) }
        )

        FilterChip(
            selected = currentFilter == TransactionFilter.WEEK,
            onClick = { onFilterSelected(TransactionFilter.WEEK) },
            label = { Text(stringResource(R.string.filter_week)) }
        )

        FilterChip(
            selected = currentFilter == TransactionFilter.MONTH,
            onClick = { onFilterSelected(TransactionFilter.MONTH) },
            label = { Text(stringResource(R.string.filter_month)) }
        )
    }
}

/**
 * Общий компонент с фильтрами периодов для экрана истории
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodFilterChips(
    currentFilter: PeriodType,
    onFilterSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodType.values().forEach { periodType ->
            FilterChip(
                selected = currentFilter == periodType,
                onClick = { onFilterSelected(periodType) },
                label = {
                    Text(
                        text = when (periodType) {
                            PeriodType.ALL -> stringResource(R.string.period_all)
                            PeriodType.MONTH -> stringResource(R.string.period_month)
                            PeriodType.QUARTER -> stringResource(R.string.period_quarter)
                            PeriodType.HALF_YEAR -> stringResource(R.string.period_half_year)
                            PeriodType.YEAR -> stringResource(R.string.period_year)
                            PeriodType.CUSTOM -> stringResource(R.string.period_custom)
                        }
                    )
                }
            )
        }
    }
} 