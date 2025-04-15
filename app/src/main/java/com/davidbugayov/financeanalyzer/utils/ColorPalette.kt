package com.davidbugayov.financeanalyzer.utils

import androidx.core.graphics.toColorInt

/**
 * Утилита для работы с цветовой палитрой источников
 */
object ColorPalette {
    
    /**
     * Возвращает список предопределенных цветов для палитры
     */
    fun getColors(): List<Int> {
        return listOf(
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
} 