package com.davidbugayov.financeanalyzer.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Контейнер для размеров и отступов, используемых в графиках
 */
@Immutable
object ChartDimens {
    // Общие размеры графиков
    val chartHeight: Dp = 300.dp
    val chartPadding: Dp = 16.dp
    val chartCornerRadius: Dp = 8.dp
    val chartCardCornerRadius: Dp = 12.dp
    val chartCardElevation: Dp = 4.dp
    
    // Отступы
    val spacingSmall: Dp = 4.dp
    val spacingMedium: Dp = 8.dp
    val spacingNormal: Dp = 16.dp
    val spacingLarge: Dp = 24.dp
    
    // Размеры текста
    val textSizeSmall: TextUnit = 12.sp
    val textSizeMedium: TextUnit = 14.sp
    val textSizeLarge: TextUnit = 16.sp
    
    // Круговая диаграмма
    object PieChart {
        val size: Dp = 220.dp
        val strokeWidth: Dp = 40.dp
        val holeRadius: Dp = 60.dp
        val sectorSpacing: Dp = 2.dp
        val selectedStrokeWidth: Dp = 3.dp
        val tapThreshold: Dp = 30.dp
        val minSectorAngle: Float = 15f
        
        // Легенда
        val legendHeight: Dp = 48.dp
        val legendIconSize: Dp = 12.dp
        val legendSpacing: Dp = 4.dp
        val legendTextStartPadding: Dp = 8.dp
    }
    
    // Линейный график
    object LineChart {
        val height: Dp = 240.dp
        val padding: Dp = 20.dp
        val strokeWidth: Dp = 2.5.dp
        val pointRadius: Dp = 5.dp
        val selectedPointRadius: Dp = 7.dp
        val selectedPointStrokeWidth: Dp = 3.dp
        val gridStrokeWidth: Dp = 0.8.dp
        val tapThreshold: Dp = 24.dp
        val textSize: TextUnit = 14.sp
        val labelTextSize: TextUnit = 12.sp
        
        // Легенда
        val legendHeight: Dp = 48.dp
        val legendIndicatorSize: Dp = 16.dp
        val legendSpacing: Dp = 12.dp
    }
} 