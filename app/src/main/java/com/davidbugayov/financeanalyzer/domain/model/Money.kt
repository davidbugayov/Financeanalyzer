package com.davidbugayov.financeanalyzer.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Класс для представления денежных значений.
 * Использует BigDecimal для точных вычислений с плавающей точкой.
 *
 * @property amount Сумма денег
 * @property currency Валюта
 */
data class Money(
    val amount: BigDecimal,
    val currency: Currency = Currency.RUB
) {

    constructor(amount: Double, currency: Currency = Currency.RUB) : this(
        BigDecimal.valueOf(amount).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency
    )

    constructor(amount: Int, currency: Currency = Currency.RUB) : this(
        BigDecimal.valueOf(amount.toLong()).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency
    )

    constructor(amount: Long, currency: Currency = Currency.RUB) : this(
        BigDecimal.valueOf(amount).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency
    )

    constructor(amount: String, currency: Currency = Currency.RUB) : this(
        BigDecimal(amount).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency
    )

    /**
     * Сложение денежных значений
     * @param other Другое денежное значение
     * @return Результат сложения
     * @throws IllegalArgumentException если валюты не совпадают
     */
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add Money with different currencies" }
        return Money(amount.add(other.amount), currency)
    }

    /**
     * Вычитание денежных значений
     * @param other Другое денежное значение
     * @return Результат вычитания
     * @throws IllegalArgumentException если валюты не совпадают
     */
    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract Money with different currencies" }
        return Money(amount.subtract(other.amount), currency)
    }

    /**
     * Умножение денежного значения на число
     * @param multiplier Множитель
     * @return Результат умножения
     */
    operator fun times(multiplier: BigDecimal): Money {
        return Money(amount.multiply(multiplier), currency)
    }

    /**
     * Умножение денежного значения на число
     * @param multiplier Множитель
     * @return Результат умножения
     */
    operator fun times(multiplier: Double): Money {
        return times(BigDecimal.valueOf(multiplier))
    }

    /**
     * Умножение денежного значения на число
     * @param multiplier Множитель
     * @return Результат умножения
     */
    operator fun times(multiplier: Int): Money {
        return times(BigDecimal.valueOf(multiplier.toLong()))
    }

    /**
     * Деление денежного значения на число
     * @param divisor Делитель
     * @return Результат деления
     */
    operator fun div(divisor: BigDecimal): Money {
        return Money(amount.divide(divisor, currency.decimalPlaces, RoundingMode.HALF_EVEN), currency)
    }

    /**
     * Деление денежного значения на число
     * @param divisor Делитель
     * @return Результат деления
     */
    operator fun div(divisor: Double): Money {
        return div(BigDecimal.valueOf(divisor))
    }

    /**
     * Деление денежного значения на число
     * @param divisor Делитель
     * @return Результат деления
     */
    operator fun div(divisor: Int): Money {
        return div(BigDecimal.valueOf(divisor.toLong()))
    }

    /**
     * Сравнение денежных значений
     * @param other Другое денежное значение
     * @return true, если текущее значение меньше other
     * @throws IllegalArgumentException если валюты не совпадают
     */
    operator fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Cannot compare Money with different currencies" }
        return amount.compareTo(other.amount)
    }

    /**
     * Проверка, является ли денежное значение отрицательным
     * @return true, если значение отрицательное
     */
    fun isNegative(): Boolean {
        return amount < BigDecimal.ZERO
    }

    /**
     * Проверка, является ли денежное значение положительным
     * @return true, если значение положительное
     */
    fun isPositive(): Boolean {
        return amount > BigDecimal.ZERO
    }

    /**
     * Проверка, равно ли денежное значение нулю
     * @return true, если значение равно нулю
     */
    fun isZero(): Boolean {
        return amount.compareTo(BigDecimal.ZERO) == 0
    }

    /**
     * Возвращает абсолютное значение денежной суммы
     * @return Абсолютное значение
     */
    fun abs(): Money {
        return Money(amount.abs(), currency)
    }

    /**
     * Форматирует денежное значение в строку с учетом валюты
     * @param showCurrency Показывать ли символ валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun format(showCurrency: Boolean = true, showSign: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = currency.groupingSeparator
        symbols.decimalSeparator = currency.decimalSeparator

        val pattern = buildString {
            if (showSign) append("+;-")
            append("#,##0")
            if (currency.decimalPlaces > 0) {
                append(".")
                repeat(currency.decimalPlaces) { append("#") }
            }
        }

        val formatter = DecimalFormat(pattern, symbols)
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = currency.decimalPlaces
        val formattedAmount = formatter.format(amount)

        return when {
            !showCurrency -> formattedAmount
            currency.symbolPosition == SymbolPosition.BEFORE -> "${currency.symbol}$formattedAmount"
            else -> "$formattedAmount ${currency.symbol}"
        }
    }

    /**
     * Форматирует денежное значение для отображения в интерфейсе
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun formatForDisplay(showSign: Boolean = false): String {
        return format(true, showSign)
    }

    /**
     * Алиас для метода format для совместимости
     * @param showCurrency Показывать ли символ валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @return Отформатированная строка
     */
    fun formatted(showCurrency: Boolean = true, showSign: Boolean = false): String {
        return format(showCurrency, showSign)
    }

    /**
     * Преобразует денежное значение в другую валюту
     * @param targetCurrency Целевая валюта
     * @param exchangeRate Обменный курс
     * @return Денежное значение в новой валюте
     */
    fun convertTo(targetCurrency: Currency, exchangeRate: BigDecimal): Money {
        val convertedAmount = amount.multiply(exchangeRate)
            .setScale(targetCurrency.decimalPlaces, RoundingMode.HALF_EVEN)
        return Money(convertedAmount, targetCurrency)
    }

    /**
     * Преобразует денежное значение в другую валюту
     * @param targetCurrency Целевая валюта
     * @param exchangeRate Обменный курс
     * @return Денежное значение в новой валюте
     */
    fun convertTo(targetCurrency: Currency, exchangeRate: Double): Money {
        return convertTo(targetCurrency, BigDecimal.valueOf(exchangeRate))
    }

    override fun toString(): String {
        return format()
    }

    companion object {

        /**
         * Создает денежное значение из строки
         * @param value Строка с денежным значением
         * @param currency Валюта
         * @return Денежное значение
         */
        fun parse(value: String, currency: Currency = Currency.RUB): Money {
            val cleanValue = value
                .replace(currency.symbol, "")
                .replace(currency.groupingSeparator.toString(), "")
                .replace(currency.decimalSeparator.toString(), ".")
                .trim()

            return try {
                Money(BigDecimal(cleanValue), currency)
            } catch (e: NumberFormatException) {
                Money(BigDecimal.ZERO, currency)
            }
        }

        /**
         * Создает нулевое денежное значение
         * @param currency Валюта
         * @return Нулевое денежное значение
         */
        fun zero(currency: Currency = Currency.RUB): Money {
            return Money(BigDecimal.ZERO, currency)
        }
    }
} 