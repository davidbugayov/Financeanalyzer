package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == TransactionFilter.TODAY,
            onClick = { onFilterSelected(TransactionFilter.TODAY) },
            label = {
                Text(
                    stringResource(R.string.filter_today),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        )

        FilterChip(
            selected = currentFilter == TransactionFilter.WEEK,
            onClick = { onFilterSelected(TransactionFilter.WEEK) },
            label = {
                Text(
                    stringResource(R.string.filter_week),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        )

        FilterChip(
            selected = currentFilter == TransactionFilter.MONTH,
            onClick = { onFilterSelected(TransactionFilter.MONTH) },
            label = {
                Text(
                    stringResource(R.string.filter_month),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        )
    }
} 