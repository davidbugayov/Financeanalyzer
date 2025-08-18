package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
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
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorDark
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorLight
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorDark
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorLight

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
            // Russian
            name.contains("сбер") -> BankSber
            name.contains("тинькофф") || name.contains("т-банк") -> BankTinkoff
            name.contains("альфа") -> BankAlfa
            name.contains("озон") -> BankOzon
            name.contains("втб") -> BankVTB
            name.contains("газпромбанк") -> BankGazprom
            name.contains("райффайзен") -> BankRaiffeisen
            name.contains("почта банк") -> BankPochta
            name.contains("юмани") -> BankYoomoney
            name.contains("наличные") || name.contains("кэш") -> CashColor

            // English
            name.contains("sber") || name.contains("sberbank") -> BankSber
            name.contains("tinkoff") || name.contains("t-bank") -> BankTinkoff
            name.contains("alfa") || name.contains("alfabank") -> BankAlfa
            name.contains("ozon") -> BankOzon
            name.contains("vtb") -> BankVTB
            name.contains("gazprombank") -> BankGazprom
            name.contains("raiffeisen") -> BankRaiffeisen
            name.contains("post bank") || name.contains("postbank") -> BankPochta
            name.contains("yoomoney") || name.contains("yumoney") -> BankYoomoney
            name.contains("cash") -> CashColor

            // Chinese (basic aliases used in arrays.xml-zh-rCN)
            name.contains("俄储") -> BankSber
            name.contains("阿尔法") -> BankAlfa
            name.contains("邮政银行") -> BankPochta
            name.contains("现金") -> CashColor

            // Default
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
        sourceColorHex: String?,
        isExpense: Boolean,
        isDarkTheme: Boolean,
    ): Color {
        // Пытаемся получить цвет из sourceColorHex, если он есть
        sourceColorHex?.let { hex ->
            try {
                if (hex.isNotBlank()) return Color(hex.toColorInt())
            } catch (_: IllegalArgumentException) {
            }
        }
        // Затем пытаемся получить по имени источника
        getSourceColorByName(sourceName)?.let { return it }

        // В крайнем случае, цвет по умолчанию в зависимости от типа транзакции и темы
        return when {
            isExpense && isDarkTheme -> ExpenseColorDark
            isExpense && !isDarkTheme -> ExpenseColorLight
            !isExpense && isDarkTheme -> IncomeColorDark
            else -> IncomeColorLight
        }
    }

    /**
     * Преобразует целочисленное представление цвета в HEX-строку.
     * @param colorInt Целочисленное представление цвета (ARGB)
     * @return HEX-строка цвета в формате "#RRGGBB"
     */
    fun colorToHex(colorInt: Int): String = String.format("#%06X", 0xFFFFFF and colorInt)

    /**
     * Преобразует HEX-строку в целочисленное представление цвета.
     * @param hexColor HEX-строка цвета в формате "#RRGGBB" или "RRGGBB"
     * @return Целочисленное представление цвета (ARGB)
     */
    fun parseHexColor(hexColor: String): Int {
        val cleanHex = if (hexColor.startsWith("#")) hexColor else "#$hexColor"
        return try {
            cleanHex.toColorInt()
        } catch (_: IllegalArgumentException) {
            // Возвращаем черный цвет по умолчанию в случае ошибки
            0xFF000000.toInt()
        }
    }

    /**
     * Эта функция больше не нужна для получения Compose Color из XML.
     * Цвета должны быть определены в ui.theme.Color.kt
     * Оставлена для возможного редкого использования, если нужно получить Int цвет из XML.
     */
    @Deprecated("Prefer defining colors in ui.theme.Color.kt for Compose.")
    fun getIntColorFromXml(
        context: Context,
        resId: Int,
    ): Int = ContextCompat.getColor(context, resId)
}
