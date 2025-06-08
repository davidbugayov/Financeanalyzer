package com.davidbugayov.financeanalyzer.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.davidbugayov.financeanalyzer.ui.theme.BankAlfa
import com.davidbugayov.financeanalyzer.ui.theme.BankGazprom
import com.davidbugayov.financeanalyzer.ui.theme.BankOzon
import com.davidbugayov.financeanalyzer.ui.theme.BankPochta
import com.davidbugayov.financeanalyzer.ui.theme.BankRaiffeisen
import com.davidbugayov.financeanalyzer.ui.theme.BankSber
import com.davidbugayov.financeanalyzer.ui.theme.BankTinkoff
import com.davidbugayov.financeanalyzer.ui.theme.BankVTB
import com.davidbugayov.financeanalyzer.ui.theme.BankYoomoney
import com.davidbugayov.financeanalyzer.ui.theme.CashColor

object ColorUtils {

    /**
     * Возвращает цвет для источника по его названию из ui.theme.Color.
     * Если цвет для источника не найден, возвращает null.
     *
     * @param sourceName Название источника
     * @return Цвет источника (androidx.compose.ui.graphics.Color) или null
     */
    fun getSourceColorByName(sourceName: String): Color? {
        val name = sourceName.lowercase()
        return when {
            name.contains("сбер") -> BankSber
            name.contains("тинькофф") || name.contains("т-банк") -> BankTinkoff
            name.contains("альфа") -> BankAlfa
            name.contains("озон") -> BankOzon
            name.contains("втб") -> BankVTB
            name.contains("газпромбанк") -> BankGazprom
            name.contains("райффайзен") -> BankRaiffeisen
            name.contains("почта банк") -> BankPochta
            name.contains("юмани") || name.contains("yoomoney") -> BankYoomoney
            name.contains("наличные") || name.contains("кэш") || name.contains("cash") -> CashColor
            // "перевод" теперь обрабатывается через TransferColorLight/Dark в теме
            // Если нужен специфичный цвет для источника "Перевод", его надо добавить в BankColors
            else -> null
        }
    }

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
        // Затем пытаемся получить по имени источника
        getSourceColorByName(sourceName)?.let { return it }

        // В крайнем случае, цвет по умолчанию в зависимости от типа транзакции и темы
        // Эту логику лучше вынести в Composable, используя LocalIncomeColor/LocalExpenseColor
        return when {
            isExpense && isDarkTheme -> com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorDark
            isExpense && !isDarkTheme -> com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorLight
            !isExpense && isDarkTheme -> com.davidbugayov.financeanalyzer.ui.theme.IncomeColorDark
            else -> com.davidbugayov.financeanalyzer.ui.theme.IncomeColorLight
        }
    }

    /**
     * Эта функция больше не нужна для получения Compose Color из XML.
     * Цвета должны быть определены в ui.theme.Color.kt
     * Оставлена для возможного редкого использования, если нужно получить Int цвет из XML.
     */
    @Deprecated("Prefer defining colors in ui.theme.Color.kt for Compose.")
    fun getIntColorFromXml(context: android.content.Context, resId: Int): Int {
        return androidx.core.content.ContextCompat.getColor(context, resId)
    }
}
