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
 * Общий компонент с фильтрами периодов для экрана истории.
 * Отображает периоды в виде чипов с понятными названиями.
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
        // Используем более крупные и понятные чипы для каждого периода
        FilterChip(
            selected = currentFilter == PeriodType.ALL,
            onClick = { onFilterSelected(PeriodType.ALL) },
            label = { Text(stringResource(R.string.period_all)) },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = currentFilter == PeriodType.DAY,
            onClick = { onFilterSelected(PeriodType.DAY) },
            label = { Text(stringResource(R.string.period_day)) },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = currentFilter == PeriodType.MONTH,
            onClick = { onFilterSelected(PeriodType.MONTH) },
            label = { Text(stringResource(R.string.period_month)) },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = currentFilter == PeriodType.YEAR,
            onClick = { onFilterSelected(PeriodType.YEAR) },
            label = { Text(stringResource(R.string.period_year)) },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = currentFilter == PeriodType.CUSTOM,
            onClick = { onFilterSelected(PeriodType.CUSTOM) },
            label = { Text(stringResource(R.string.period_custom)) },
            modifier = Modifier.weight(1f)
        )
    }
} 