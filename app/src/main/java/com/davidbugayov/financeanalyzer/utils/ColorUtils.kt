package com.davidbugayov.financeanalyzer.utils

import android.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Source
import kotlin.random.Random

object ColorUtils {

    // Цвета для банков
    val SBER_COLOR = Color.parseColor("#21A038")
    val TINKOFF_COLOR = Color.parseColor("#FFDD2D")
    val ALFA_COLOR = Color.parseColor("#EF3124")

    // Предустановленные источники
    val defaultSources = listOf(
        Source(name = "Сбер", color = SBER_COLOR),
        Source(name = "Тинькофф", color = TINKOFF_COLOR),
        Source(name = "Альфа", color = ALFA_COLOR)
    )

    val predefinedColors = listOf(
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#673AB7"), // Deep Purple
        Color.parseColor("#3F51B5"), // Indigo
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#03A9F4"), // Light Blue
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#009688"), // Teal
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#8BC34A"), // Light Green
        Color.parseColor("#CDDC39"), // Lime
        Color.parseColor("#FFEB3B"), // Yellow
        Color.parseColor("#FFC107"), // Amber
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#FF5722"), // Deep Orange
        Color.parseColor("#795548"), // Brown
        Color.parseColor("#9E9E9E"), // Grey
        Color.parseColor("#607D8B")  // Blue Grey
    )

    /**
     * Генерирует случайный цвет из предопределенного списка
     */
    fun getRandomColor(): Int {
        return predefinedColors[Random.nextInt(predefinedColors.size)]
    }
} 