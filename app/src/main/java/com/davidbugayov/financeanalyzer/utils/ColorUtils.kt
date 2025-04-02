package com.davidbugayov.financeanalyzer.utils

import androidx.core.graphics.toColorInt
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction

object ColorUtils {

    // Цвета для банков
    val SBER_COLOR = "#21A038".toColorInt()
    val TINKOFF_COLOR = "#FFDD2D".toColorInt()
    val ALFA_COLOR = "#EF3124".toColorInt()

    // Цвета для доходов и расходов
    val INCOME_COLOR = "#4CAF50".toColorInt() // Зеленый
    val EXPENSE_COLOR = "#F44336".toColorInt() // Красный

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
    fun getEffectiveSourceColor(source: String, sourceColor: Int, isExpense: Boolean): Int {
        return sourceColor.takeIf { it != 0 } 
            ?: getSourceColor(source) 
            ?: if (isExpense) EXPENSE_COLOR else INCOME_COLOR
    }

    /**
     * Получает эффективный цвет источника транзакции.
     * Если цвет источника не задан явно, определяет его по названию источника.
     * Если и это не удалось, использует цвет по умолчанию для расхода/дохода.
     * 
     * @return Эффективный цвет источника
     */
    fun Transaction.getEffectiveSourceColor(): Int {
        return sourceColor.takeIf { it != 0 } 
            ?: getSourceColor(source) 
            ?: if (isExpense) EXPENSE_COLOR else INCOME_COLOR
    }
} 