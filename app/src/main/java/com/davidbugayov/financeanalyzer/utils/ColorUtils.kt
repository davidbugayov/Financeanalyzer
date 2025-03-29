package com.davidbugayov.financeanalyzer.utils

import androidx.core.graphics.toColorInt
import com.davidbugayov.financeanalyzer.domain.model.Source

object ColorUtils {

    // Цвета для банков
    val SBER_COLOR = "#21A038".toColorInt()
    val TINKOFF_COLOR = "#FFDD2D".toColorInt()
    val ALFA_COLOR = "#EF3124".toColorInt()

    // Предустановленные источники
    val defaultSources = listOf(
        Source(name = "Сбер", color = SBER_COLOR),
        Source(name = "Т-Банк", color = TINKOFF_COLOR),
        Source(name = "Альфа", color = ALFA_COLOR)
    )

    val predefinedColors = listOf(
        "#F44336".toColorInt(), // Red
        "#E91E63".toColorInt(), // Pink
        "#9C27B0".toColorInt(), // Purple
        "#673AB7".toColorInt(), // Deep Purple
        "#3F51B5".toColorInt(), // Indigo
        "#2196F3".toColorInt(), // Blue
        "#03A9F4".toColorInt(), // Light Blue
        "#00BCD4".toColorInt(), // Cyan
        "#009688".toColorInt(), // Teal
        "#4CAF50".toColorInt(), // Green
        "#8BC34A".toColorInt(), // Light Green
        "#CDDC39".toColorInt(), // Lime
        "#FFEB3B".toColorInt(), // Yellow
        "#FFC107".toColorInt(), // Amber
        "#FF9800".toColorInt(), // Orange
        "#FF5722".toColorInt(), // Deep Orange
        "#795548".toColorInt(), // Brown
        "#9E9E9E".toColorInt(), // Grey
        "#607D8B".toColorInt()  // Blue Grey
    )
} 