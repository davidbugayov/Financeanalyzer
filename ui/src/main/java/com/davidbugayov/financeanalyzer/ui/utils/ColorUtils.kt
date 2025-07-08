package com.davidbugayov.financeanalyzer.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorUtils {
    /**
     * Получает эффективный цвет источника для транзакции.
     * ВАЖНО: Эта функция возвращает Color, а не Int.
     * Для получения цвета в зависимости от темы (доход/расход), используйте LocalIncomeColor/LocalExpenseColor.current в Composable.
     * Эта утилита может быть полезна для не-Composable контекстов или если нужна логика по умолчанию.
     *
     * @param sourceName Название источника
     * @param sourceColorHex HEX-строка цвета источника из БД (например, "#RRGGBB") или null
     * @param isExpense Флаг расхода
     * @param isDarkTheme Текущая тема (для выбора цвета по умолчанию)
     * @return androidx.compose.ui.graphics.Color
     */
    fun getEffectiveSourceColor(
        sourceName: String,
        sourceColorHex: String?, // Ожидаем HEX-строку или null
        isExpense: Boolean,
        isDarkTheme: Boolean, // Нужно передавать состояние темы
    ): Color {
        // Пытаемся получить цвет из sourceColorHex, если он есть
        sourceColorHex?.let { hex ->
            try {
                if (hex.isNotBlank()) return Color(hex.toColorInt())
            } catch (_: IllegalArgumentException) {
                // Log error or handle invalid hex
            }
        }

        // В крайнем случае, цвет по умолчанию в зависимости от типа транзакции и темы
        return when {
            isExpense && isDarkTheme -> Color(0xFFE57373) // ExpenseColorDark
            isExpense && !isDarkTheme -> Color(0xFFE53935) // ExpenseColorLight
            !isExpense && isDarkTheme -> Color(0xFF81C784) // IncomeColorDark
            else -> Color(0xFF43A047) // IncomeColorLight
        }
    }

    /**
     * Преобразует целочисленное представление цвета в HEX-строку.
     * @param colorInt Целочисленное представление цвета (ARGB)
     * @return HEX-строка цвета в формате "#RRGGBB"
     */
    fun colorToHex(colorInt: Int): String {
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

    /**
     * Преобразует HEX-строку в целочисленное представление цвета.
     * @param hexColor HEX-строка цвета в формате "#RRGGBB" или "RRGGBB"
     * @return Целочисленное представление цвета (ARGB)
     */
    fun parseHexColor(hexColor: String): Int {
        val cleanHex = if (hexColor.startsWith("#")) hexColor else "#$hexColor"
        return try {
            cleanHex.toColorInt()
        } catch (e: IllegalArgumentException) {
            // Возвращаем черный цвет по умолчанию в случае ошибки
            0xFF000000.toInt()
        }
    }
}
