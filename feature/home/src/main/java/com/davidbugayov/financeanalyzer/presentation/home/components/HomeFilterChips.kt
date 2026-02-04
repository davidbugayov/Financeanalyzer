package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
 * Использует современный Material Design 3 с плавными анимациями и улучшенной иерархией.
 * Каждый фильтр имеет:
 * - Анимированные переходы цветов при выборе
 * - Масштабирование при нажатии
 * - Улучшенную визуальную иерархию
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
                TransactionFilter.ALL,
                stringResource(UiR.string.filter_all),
                Icons.Default.Timeline,
            ),
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
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
    modifier: Modifier = Modifier,
) {
    // Анимированные цвета для плавного перехода между состояниями
    val animationSpec = tween<androidx.compose.ui.graphics.Color>(durationMillis = 200)

    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val unselectedContainerColor = MaterialTheme.colorScheme.surface
    val animatedContainerColor =
        animateColorAsState(
            targetValue = if (isSelected) selectedContainerColor else unselectedContainerColor,
            animationSpec = animationSpec,
            label = "containerColor",
        ).value

    val selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val animatedLabelColor =
        animateColorAsState(
            targetValue = if (isSelected) selectedLabelColor else unselectedLabelColor,
            animationSpec = animationSpec,
            label = "labelColor",
        ).value

    val selectedBorderColor = MaterialTheme.colorScheme.primary
    val unselectedBorderColor = MaterialTheme.colorScheme.outlineVariant
    val animatedBorderColor =
        animateColorAsState(
            targetValue = if (isSelected) selectedBorderColor else unselectedBorderColor,
            animationSpec = animationSpec,
            label = "borderColor",
        ).value

    // Масштабирование при выборе для визуального отклика
    val scale = if (isSelected) 1.05f else 1f

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                filterData.label,
                fontSize = dimensionResource(UiR.dimen.text_size_14sp).value.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = animatedLabelColor,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = filterData.icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = animatedLabelColor,
            )
        },
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = animatedContainerColor,
                labelColor = animatedLabelColor,
                iconColor = animatedLabelColor,
                selectedContainerColor = animatedContainerColor,
                selectedLabelColor = animatedLabelColor,
            ),
        border =
            BorderStroke(
                width = if (isSelected) 2.dp else 1.5.dp,
                color = animatedBorderColor,
            ),
        modifier =
            modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                ),
        interactionSource = remember { MutableInteractionSource() },
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
