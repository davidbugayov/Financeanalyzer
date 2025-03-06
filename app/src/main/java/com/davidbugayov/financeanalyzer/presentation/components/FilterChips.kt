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
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
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
                            PeriodType.ALL -> "All"
                            PeriodType.MONTH -> "Month"
                            PeriodType.QUARTER -> "Quarter"
                            PeriodType.HALF_YEAR -> "Half Year"
                            PeriodType.YEAR -> "Year"
                            PeriodType.CUSTOM -> "Custom"
                        }
                    )
                }
            )
        }
    }
} 