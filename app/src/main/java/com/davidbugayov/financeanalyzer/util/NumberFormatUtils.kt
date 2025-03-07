package com.davidbugayov.financeanalyzer.util

import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Money
import kotlin.math.abs

/**
 * Форматирует число для отображения в компактном или сокращенном виде.
 * Использует суффиксы K и M для тысяч и миллионов соответственно.
 * Автоматически определяет количество десятичных знаков.
 *
 * Примеры:
 * - 1234.56 -> "1.2K"
 * - 1000000 -> "1.0M"
 * - 123.456 -> "123.46"
 * 
 * @param number Число для форматирования
 * @param maxLength Максимальная длина результата (по умолчанию без ограничений)
 * @return Отформатированное число в виде строки
 */
fun formatNumber(
    number: Double,
    maxLength: Int? = null
): String {
    val absNumber = abs(number)
    val prefix = if (number < 0) "-" else ""

    // Пробуем сначала отформатировать с десятичными знаками
    val fullFormatted = when {
        absNumber >= 1_000_000 -> "${prefix}${String.format("%.1fM", absNumber / 1_000_000)}"
        absNumber >= 1_000 -> "${prefix}${String.format("%.1fK", absNumber / 1_000)}"
        else -> "${prefix}${String.format("%.2f", absNumber)}"
    }

    // Если число помещается полностью или нет ограничения длины
    if (maxLength == null || fullFormatted.length <= maxLength) {
        return fullFormatted
    }

    // Иначе используем сокращенный формат
    return when {
        absNumber >= 1_000_000 -> "${prefix}${String.format("%.0fM", absNumber / 1_000_000)}"
        absNumber >= 1_000 -> "${prefix}${String.format("%.0fK", absNumber / 1_000)}"
        else -> "${prefix}${String.format("%.0f", absNumber)}"
    }
}

/**
 * Форматирует сумму транзакции для отображения в списках и графиках.
 * Скрывает десятичные знаки, если они нулевые.
 * Использует суффиксы K, M и B для больших чисел.
 * Сохраняет знак числа.
 *
 * Примеры:
 * - 1234 -> "1234"
 * - 1234.56 -> "1234.56"
 * - 1000000 -> "1.0M"
 * - -1234.56 -> "-1234.56"
 *
 * @param amount Сумма транзакции
 * @param currency Валюта (по умолчанию RUB)
 * @return Отформатированная сумма в виде строки
 */
fun formatTransactionAmount(amount: Double, currency: Currency = Currency.RUB): String {
    return CurrencyFormatter.format(amount, currency, false)
}

/**
 * Форматирует сумму транзакции для отображения в списках и графиках.
 * @param money Денежное значение
 * @return Отформатированная сумма в виде строки
 */
fun formatTransactionAmount(money: Money): String {
    return CurrencyFormatter.format(money.amount, money.currency, false)
}

/**
 * Форматирует число для отображения с символом валюты.
 * Использует суффиксы К и М для тысяч и миллионов.
 * Автоматически определяет необходимость десятичных знаков.
 * Добавляет символ валюты после числа.
 *
 * @param number Число для форматирования
 * @param currency Валюта (по умолчанию RUB)
 * @return Отформатированное число с символом валюты
 */
fun formatNumberWithCurrency(
    number: Double,
    currency: Currency = Currency.RUB
): String {
    return CurrencyFormatter.formatForDisplay(number, currency)
}

/**
 * Форматирует денежное значение для отображения с символом валюты.
 * @param money Денежное значение
 * @return Отформатированное число с символом валюты
 */
fun formatNumberWithCurrency(money: Money): String {
    return CurrencyFormatter.formatForDisplay(money)
}

/**
 * Форматирует денежное значение для отображения со знаком (доход/расход)
 * @param amount Сумма
 * @param isExpense Является ли расходом
 * @param currency Валюта (по умолчанию RUB)
 * @return Отформатированная строка
 */
fun formatWithSign(
    amount: Double,
    isExpense: Boolean,
    currency: Currency = Currency.RUB
): String {
    return CurrencyFormatter.formatWithSign(amount, isExpense, currency)
}

/**
 * Форматирует денежное значение для отображения со знаком (доход/расход)
 * @param money Денежное значение
 * @param isExpense Является ли расходом
 * @return Отформатированная строка
 */
fun formatWithSign(
    money: Money,
    isExpense: Boolean
): String {
    return CurrencyFormatter.formatWithSign(money, isExpense)
}
