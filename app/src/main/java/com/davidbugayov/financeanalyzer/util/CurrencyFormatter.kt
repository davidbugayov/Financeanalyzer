package com.davidbugayov.financeanalyzer.util

import com.davidbugayov.financeanalyzer.domain.model.Currency
import com.davidbugayov.financeanalyzer.domain.model.Money
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Утилитарный класс для форматирования денежных значений.
 */
object CurrencyFormatter {

    /**
     * Форматирует денежное значение в строку с учетом валюты
     * @param amount Сумма
     * @param currency Валюта
     * @param showCurrency Показывать ли символ валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun format(
        amount: BigDecimal,
        currency: Currency = Currency.RUB,
        showCurrency: Boolean = true,
        showSign: Boolean = false
    ): String {
        return Money(amount, currency).format(showCurrency, showSign)
    }

    /**
     * Форматирует денежное значение в строку с учетом валюты
     * @param amount Сумма
     * @param currency Валюта
     * @param showCurrency Показывать ли символ валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun format(
        amount: Double,
        currency: Currency = Currency.RUB,
        showCurrency: Boolean = true,
        showSign: Boolean = false
    ): String {
        return Money(amount, currency).format(showCurrency, showSign)
    }

    /**
     * Форматирует денежное значение для отображения в интерфейсе
     * @param amount Сумма
     * @param currency Валюта
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun formatForDisplay(
        amount: Double,
        currency: Currency = Currency.RUB,
        showSign: Boolean = false
    ): String {
        return Money(amount, currency).formatForDisplay(showSign)
    }

    /**
     * Форматирует денежное значение для отображения в интерфейсе
     * @param money Денежное значение
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun formatForDisplay(
        money: Money,
        showSign: Boolean = false
    ): String {
        return money.formatForDisplay(showSign)
    }

    /**
     * Форматирует денежное значение для отображения в интерфейсе с учетом знака (доход/расход)
     * @param amount Сумма
     * @param isExpense Является ли расходом
     * @param currency Валюта
     * @return Отформатированная строка
     */
    fun formatWithSign(
        amount: Double,
        isExpense: Boolean,
        currency: Currency = Currency.RUB
    ): String {
        val money = Money(amount, currency)
        val formattedAmount = money.format(false)

        return if (isExpense) {
            "-$formattedAmount ${currency.symbol}"
        } else {
            "+$formattedAmount ${currency.symbol}"
        }
    }

    /**
     * Форматирует денежное значение для отображения в интерфейсе с учетом знака (доход/расход)
     * @param money Денежное значение
     * @param isExpense Является ли расходом
     * @return Отформатированная строка
     */
    fun formatWithSign(
        money: Money,
        isExpense: Boolean
    ): String {
        val formattedAmount = money.format(false)

        return if (isExpense) {
            "-$formattedAmount ${money.currency.symbol}"
        } else {
            "+$formattedAmount ${money.currency.symbol}"
        }
    }

    /**
     * Форматирует процент
     * @param percent Процент
     * @param decimalPlaces Количество знаков после запятой
     * @return Отформатированная строка
     */
    fun formatPercent(
        percent: Double,
        decimalPlaces: Int = 1
    ): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            decimalSeparator = ','
        }

        val pattern = buildString {
            append("#,##0")
            if (decimalPlaces > 0) {
                append(".")
                repeat(decimalPlaces) { append("0") }
            }
            append("%")
        }

        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(percent)
    }

    /**
     * Форматирует денежное значение с процентом
     * @param amount Сумма
     * @param percent Процент
     * @param currency Валюта
     * @return Отформатированная строка
     */
    fun formatWithPercent(
        amount: Double,
        percent: Double,
        currency: Currency = Currency.RUB
    ): String {
        val formattedAmount = format(amount, currency)
        val formattedPercent = formatPercent(percent)

        return "$formattedAmount ($formattedPercent)"
    }
} 