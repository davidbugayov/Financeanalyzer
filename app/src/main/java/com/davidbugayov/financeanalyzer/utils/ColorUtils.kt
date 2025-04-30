package com.davidbugayov.financeanalyzer.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.davidbugayov.financeanalyzer.domain.model.Source
import kotlin.random.Random

object ColorUtils {

    // Цвета для банков
    val SBER_COLOR = "#21A038".toColorInt()
    val TINKOFF_COLOR = "#FFDD2D".toColorInt()
    val ALFA_COLOR = "#EF3124".toColorInt()
    
    // Другие цвета для источников
    val CASH_COLOR = "#9E9E9E".toColorInt() // Серый цвет для наличных

    // Цвета для доходов и расходов
    val INCOME_COLOR = "#4CAF50".toColorInt() // Зеленый
    val EXPENSE_COLOR = "#F44336".toColorInt() // Красный
    val TRANSFER_COLOR = "#9E9E9E".toColorInt() // Серый для переводов

    // Предустановленные источники
    val defaultSources = listOf(
        Source(name = "Сбер", color = SBER_COLOR),
        Source(name = "Т-Банк", color = TINKOFF_COLOR),
        Source(name = "Альфа", color = ALFA_COLOR),
        Source(name = "Наличные", color = CASH_COLOR)
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
    
    /**
     * Возвращает цвет для источника по его названию.
     * Если цвет для источника не найден, возвращает null.
     * 
     * @param sourceName Название источника
     * @return Цвет источника или null, если цвет не найден
     */
    fun getSourceColor(sourceName: String): Int? {
        val name = sourceName.lowercase()
        return when {
            name.contains("сбер") -> SBER_COLOR
            name.contains("тинькофф") || name.contains("т-банк") -> TINKOFF_COLOR
            name.contains("альфа") -> ALFA_COLOR
            name.contains("озон") -> "#0066FF".toColorInt() // Синий цвет для Озона
            name.contains("втб") -> "#00AEEF".toColorInt() 
            name.contains("газпромбанк") -> "#0079C2".toColorInt()
            name.contains("райффайзен") -> "#FFED00".toColorInt()
            name.contains("почта банк") -> "#74397E".toColorInt()
            name.contains("юмани") || name.contains("yoomoney") -> "#8F2FE2".toColorInt()
            name.contains("наличные") || name.contains("кэш") || name.contains("cash") -> CASH_COLOR
            name.contains("перевод") -> TRANSFER_COLOR
            else -> null
        }
    }

    /**
     * Получает эффективный цвет источника для транзакции.
     * 
     * @param source Название источника
     * @param sourceColor Заданный цвет источника (0 если не задан)
     * @param isExpense Флаг расхода
     * @return Цвет источника или цвет по умолчанию (для расхода/дохода)
     */
    fun getEffectiveSourceColor(sourceName: String, sourceColor: Int, isExpense: Boolean): Int {
        return sourceColor.takeIf { it != 0 }
            ?: getSourceColor(sourceName) 
            ?: if (isExpense) EXPENSE_COLOR else INCOME_COLOR
    }
    
    /**
     * Generates a random color for a category based on whether it's income or expense
     * 
     * @param isIncome Whether the category is for income or expense
     * @return A random color that fits with the income/expense color scheme
     */
    fun generateRandomCategoryColor(isIncome: Boolean): Color {
        // Base colors for income (greens) and expense (reds)
        val baseHue = if (isIncome) 120f else 0f // Green for income, Red for expense
        val hueVariation = 30f // Allow some variation in hue
        
        // Generate a random hue within the range
        val hue = baseHue + Random.nextFloat() * hueVariation - (hueVariation / 2)
        
        // Generate saturation and lightness
        val saturation = 0.7f + Random.nextFloat() * 0.3f // 70-100% saturation
        val lightness = 0.4f + Random.nextFloat() * 0.3f // 40-70% lightness
        
        return hslToColor(hue, saturation, lightness)
    }
    
    /**
     * Converts HSL (Hue, Saturation, Lightness) values to a Color
     * 
     * @param hue Hue value (0-360)
     * @param saturation Saturation value (0-1)
     * @param lightness Lightness value (0-1)
     * @return The resulting Color
     */
    private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
        val h = (hue % 360) / 60f
        val s = saturation.coerceIn(0f, 1f)
        val l = lightness.coerceIn(0f, 1f)
        
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs(h % 2f - 1f))
        val m = l - c / 2f
        
        val (r1, g1, b1) = when {
            h < 1f -> Triple(c, x, 0f)
            h < 2f -> Triple(x, c, 0f)
            h < 3f -> Triple(0f, c, x)
            h < 4f -> Triple(0f, x, c)
            h < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        return Color(
            (r1 + m).coerceIn(0f, 1f),
            (g1 + m).coerceIn(0f, 1f),
            (b1 + m).coerceIn(0f, 1f)
        )
    }
    
    private fun abs(value: Float): Float = if (value < 0) -value else value
} 