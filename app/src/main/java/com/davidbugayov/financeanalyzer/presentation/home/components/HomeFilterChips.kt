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
fun PermissionUtilsHomeFilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    val filters = listOf(
        FilterChipData(TransactionFilter.TODAY, R.string.filter_today),
        FilterChipData(TransactionFilter.WEEK, R.string.filter_week),
        FilterChipData(TransactionFilter.MONTH, R.string.filter_month),
        FilterChipData(TransactionFilter.ALL, R.string.all)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filterData ->
            FilterChipItem(
                filterData = filterData,
                isSelected = currentFilter == filterData.filter,
                onClick = {
                    Timber.d("Выбран фильтр ${filterData.filter}")
                    onFilterSelected(filterData.filter)
                }
            )
        }
    }
}

private data class FilterChipData(val filter: TransactionFilter, val labelRes: Int)

@Composable
private fun FilterChipItem(
    filterData: FilterChipData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                stringResource(filterData.labelRes),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun HomeFilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    PermissionUtilsHomeFilterChips(
        currentFilter = currentFilter,
        onFilterSelected = onFilterSelected
    )
} 