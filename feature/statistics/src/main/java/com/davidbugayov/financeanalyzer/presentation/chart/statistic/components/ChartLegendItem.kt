package com.davidbugayov.financeanalyzer.presentation.chart.statistic.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.davidbugayov.financeanalyzer.feature.statistics.R

/**
 * Компонент элемента легенды для графиков
 *
 * @param color Цвет элемента легенды
 * @param text Текст элемента легенды
 */
@Composable
fun ChartLegendItem(color: Color, text: String) {
    val legendItemWidth = dimensionResource(id = R.dimen.chart_legend_item_width)
    val legendItemHeight = dimensionResource(id = R.dimen.chart_legend_item_height)
    val legendSpacing = dimensionResource(id = R.dimen.chart_legend_spacing)
    val cornerRadius = dimensionResource(id = R.dimen.chart_corner_radius)

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(legendItemWidth)
                .height(legendItemHeight)
                .background(color, RoundedCornerShape(cornerRadius)),
        )

        Spacer(modifier = Modifier.width(legendSpacing))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
