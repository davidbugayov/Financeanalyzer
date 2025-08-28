package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Компонент с чипами для выбора типа группировки транзакций.
 * Позволяет пользователю выбрать группировку по дням, неделям или месяцам.
 * Отображает текущий выбранный тип группировки.
 *
 * @param currentGrouping Текущий выбранный тип группировки
 * @param onGroupingSelected Callback, вызываемый при выборе типа группировки
 */
@Composable
fun GroupingChips(
    currentGrouping: GroupingType,
    onGroupingSelected: (GroupingType) -> Unit,
) {
    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val unselectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilterChip(
            selected = currentGrouping == GroupingType.DAY,
            onClick = { onGroupingSelected(GroupingType.DAY) },
            label = { Text(stringResource(UiR.string.group_by_days)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarViewDay,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (currentGrouping == GroupingType.DAY) selectedLabelColor else unselectedLabelColor
                )
            },
            colors =
                FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedContainerColor,
                    selectedLabelColor = selectedLabelColor,
                    containerColor = unselectedContainerColor,
                    labelColor = unselectedLabelColor,
                ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (currentGrouping == GroupingType.DAY) MaterialTheme.colorScheme.primary else borderColor
            ),
        )

        FilterChip(
            selected = currentGrouping == GroupingType.WEEK,
            onClick = { onGroupingSelected(GroupingType.WEEK) },
            label = { Text(stringResource(UiR.string.group_by_weeks)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarViewWeek,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (currentGrouping == GroupingType.WEEK) selectedLabelColor else unselectedLabelColor
                )
            },
            colors =
                FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedContainerColor,
                    selectedLabelColor = selectedLabelColor,
                    containerColor = unselectedContainerColor,
                    labelColor = unselectedLabelColor,
                ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (currentGrouping == GroupingType.WEEK) MaterialTheme.colorScheme.primary else borderColor
            ),
        )

        FilterChip(
            selected = currentGrouping == GroupingType.MONTH,
            onClick = { onGroupingSelected(GroupingType.MONTH) },
            label = { Text(stringResource(UiR.string.group_by_months)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (currentGrouping == GroupingType.MONTH) selectedLabelColor else unselectedLabelColor
                )
            },
            colors =
                FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedContainerColor,
                    selectedLabelColor = selectedLabelColor,
                    containerColor = unselectedContainerColor,
                    labelColor = unselectedLabelColor,
                ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (currentGrouping == GroupingType.MONTH) MaterialTheme.colorScheme.primary else borderColor
            ),
        )
    }
}
