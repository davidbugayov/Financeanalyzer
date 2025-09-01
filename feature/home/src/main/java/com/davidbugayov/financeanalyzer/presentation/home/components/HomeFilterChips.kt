package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.ui.R as UiR
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
            FilterChipData(
                TransactionFilter.TODAY,
                stringResource(UiR.string.filter_today),
                Icons.Default.Today,
            ),
            FilterChipData(
                TransactionFilter.WEEK,
                stringResource(UiR.string.filter_week),
                Icons.Default.DateRange,
            ),
            FilterChipData(
                TransactionFilter.MONTH,
                stringResource(UiR.string.filter_month),
                Icons.Default.CalendarMonth,
            ),
            FilterChipData(
                TransactionFilter.QUARTER,
                stringResource(UiR.string.filter_quarter),
                Icons.Default.DateRange,
            ),
            FilterChipData(
                TransactionFilter.YEAR,
                stringResource(UiR.string.filter_year),
                Icons.Default.DateRange,
            ),
            FilterChipData(
                TransactionFilter.ALL,
                stringResource(UiR.string.filter_all),
                Icons.Default.Timeline,
            ),
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 0.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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

private data class FilterChipData(
    val filter: TransactionFilter,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun FilterChipItem(
    filterData: FilterChipData,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val unselectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(
                alpha = 0.5f,
            )
        }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                filterData.label,
                fontSize = dimensionResource(UiR.dimen.text_size_14sp).value.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        },
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = filterData.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) selectedLabelColor else unselectedLabelColor,
            )
        },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = selectedContainerColor,
                selectedLabelColor = selectedLabelColor,
                containerColor = unselectedContainerColor,
                labelColor = unselectedLabelColor,
            ),
        border = BorderStroke(dimensionResource(UiR.dimen.border_width_1_2dp), borderColor),
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
