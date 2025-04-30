package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartItemData

/**
 * Legend component for the pie chart that displays categories with their values
 * and allows selecting individual categories
 * 
 * @param categories List of categories to display in the legend
 * @param selectedIndex Currently selected category index or null if none
 * @param onCategorySelected Callback when a category is selected
 * @param modifier Modifier for the component
 */
@Composable
fun PieChartLegend(
    categories: List<PieChartItemData>,
    selectedIndex: Int?,
    onCategorySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(categories) { index, item ->
            val isSelected = index == selectedIndex
            
            CategoryLegendItem(
                item = item,
                color = item.color,
                isSelected = isSelected,
                onClick = { onCategorySelected(index) }
            )
        }
    }
} 