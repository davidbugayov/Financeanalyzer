package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model

import androidx.compose.ui.geometry.Offset
import com.davidbugayov.financeanalyzer.domain.model.Category

/**
 * Data class representing drawing parameters for a pie chart sector.
 *
 * @param category The category associated with this sector
 * @param center The center point of the pie chart
 * @param outerRadius The outer radius of this sector
 * @param innerRadius The inner radius of this sector (for donut charts)
 * @param startAngle The starting angle of this sector in degrees
 * @param sweepAngle The angular size of this sector in degrees
 */
data class SectorDrawData(
    val category: Category,
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val startAngle: Float,
    val sweepAngle: Float
) 