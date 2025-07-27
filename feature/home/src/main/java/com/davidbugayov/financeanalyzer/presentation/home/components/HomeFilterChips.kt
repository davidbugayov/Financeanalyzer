package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import androidx.compose.ui.res.stringResource
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
    onFilterSelected: (TransactionFilter) -> Unit,
) {
    val filters =
        listOf(
            FilterChipData(TransactionFilter.TODAY, stringResource(R.string.filter_today)),
            FilterChipData(TransactionFilter.WEEK, stringResource(R.string.filter_week)),
            FilterChipData(TransactionFilter.MONTH, stringResource(R.string.filter_month)),
            FilterChipData(TransactionFilter.ALL, stringResource(R.string.filter_all)),
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filterData ->
            FilterChipItem(
                filterData = filterData,
                isSelected = currentFilter == filterData.filter,
                onClick = {
                    Timber.d("Выбран фильтр ${filterData.filter}")
                    onFilterSelected(filterData.filter)
                },
            )
        }
    }
}

private data class FilterChipData(val filter: TransactionFilter, val label: String)

@Composable
private fun FilterChipItem(
    filterData: FilterChipData,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val unselectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                filterData.label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = selectedContainerColor,
                selectedLabelColor = selectedLabelColor,
                containerColor = unselectedContainerColor,
                labelColor = unselectedLabelColor,
            ),
        border = BorderStroke(1.2.dp, borderColor),
    )
}

@Composable
fun HomeFilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
) {
    PermissionUtilsHomeFilterChips(
        currentFilter = currentFilter,
        onFilterSelected = onFilterSelected,
    )
}
