package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.PieChartItemData

/**
 * Displays a scrollable legend for the pie chart categories
 * 
 * @param categories List of categories to display in the legend
 * @param colors List of colors corresponding to each category
 * @param selectedIndex Currently selected category index
 * @param onCategorySelected Callback when a category is selected
 * @param modifier Modifier for the legend
 */
@Composable
fun PieChartLegend(
    categories: List<PieChartItemData>,
    colors: List<Color>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(categories) { index, item ->
            // Use the color from the colors list, or a default if the index is out of bounds
            val color = if (index < colors.size) colors[index] else Color.Gray
            
            CategoryLegendItem(
                item = item,
                color = color,
                isSelected = index == selectedIndex,
                onClick = { onCategorySelected(index) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 