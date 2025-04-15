package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import timber.log.Timber

/**
 * Компонент с фильтрами транзакций
 *
 * @param currentFilter Текущий выбранный фильтр
 * @param onFilterSelected Callback, вызываемый при выборе фильтра
 */
@Composable
fun HomeFilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    // Логирование текущего фильтра перед отрисовкой
    Timber.d("HomeFilterChips: отрисовка с текущим фильтром = $currentFilter")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Фильтр "Сегодня"
        FilterChip(
            selected = currentFilter == TransactionFilter.TODAY,
            onClick = { 
                Timber.d("Выбран фильтр TODAY")
                onFilterSelected(TransactionFilter.TODAY) 
            },
            label = {
                Text(
                    stringResource(R.string.filter_today),
                    fontSize = 14.sp,
                    fontWeight = if (currentFilter == TransactionFilter.TODAY) 
                        FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Фильтр "Неделя"
        FilterChip(
            selected = currentFilter == TransactionFilter.WEEK,
            onClick = { 
                Timber.d("Выбран фильтр WEEK")
                onFilterSelected(TransactionFilter.WEEK) 
            },
            label = {
                Text(
                    stringResource(R.string.filter_week),
                    fontSize = 14.sp,
                    fontWeight = if (currentFilter == TransactionFilter.WEEK) 
                        FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Фильтр "Месяц"
        FilterChip(
            selected = currentFilter == TransactionFilter.MONTH,
            onClick = { 
                Timber.d("Выбран фильтр MONTH")
                onFilterSelected(TransactionFilter.MONTH) 
            },
            label = {
                Text(
                    stringResource(R.string.filter_month),
                    fontSize = 14.sp,
                    fontWeight = if (currentFilter == TransactionFilter.MONTH) 
                        FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        // Фильтр "Все"
        FilterChip(
            selected = currentFilter == TransactionFilter.ALL,
            onClick = { 
                Timber.d("Выбран фильтр ALL")
                onFilterSelected(TransactionFilter.ALL) 
            },
            label = {
                Text(
                    stringResource(R.string.all),
                    fontSize = 14.sp,
                    fontWeight = if (currentFilter == TransactionFilter.ALL) 
                        FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
} 