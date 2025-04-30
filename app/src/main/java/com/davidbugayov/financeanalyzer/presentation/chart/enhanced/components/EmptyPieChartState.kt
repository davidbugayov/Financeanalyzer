package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.davidbugayov.financeanalyzer.R

/**
 * Component for displaying empty state in pie charts
 *
 * @param isIncome Flag for data type (income/expense)
 * @param modifier Modifier for appearance customization
 */
@Composable
fun EmptyPieChartState(
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val chartSpacingNormal = dimensionResource(id = R.dimen.chart_spacing_normal)
    val chartEmptyStateElevation = dimensionResource(id = R.dimen.chart_empty_state_elevation)
    
    val color = if (isIncome) {
        colorResource(id = R.color.income)
    } else {
        colorResource(id = R.color.expense)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(chartSpacingNormal),
        elevation = CardDefaults.cardElevation(defaultElevation = chartEmptyStateElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(chartSpacingNormal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.chart_empty_pie_data),
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
} 