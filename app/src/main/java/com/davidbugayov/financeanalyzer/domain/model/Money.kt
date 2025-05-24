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

    init {
        require(amount.scale() <= currency.decimalPlaces) {
            "Amount scale (${amount.scale()}) exceeds currency decimal places (${currency.decimalPlaces})"
        }
    }

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
        require(currency == other.currency) {
            "Cannot add money with different currencies: $currency and ${other.currency}"
        }
        return Money(amount.add(other.amount), currency)
    }

    /**
     * Вычитание денежных значений
     * @param other Другое денежное значение
     * @return Результат вычитания
     * @throws IllegalArgumentException если валюты не совпадают
     */
    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot subtract money with different currencies: $currency and ${other.currency}"
        }
        return Money(amount.subtract(other.amount), currency)
    }

    /**
     * Умножение денежного значения на число
     * @param multiplier Множитель
     * @return Результат умножения
     */
    operator fun times(multiplier: BigDecimal): Money {
        require(multiplier >= BigDecimal.ZERO) {
            "Multiplier must be non-negative: $multiplier"
        }
        return Money(amount.multiply(multiplier).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN), currency)
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
     * 
     * @param showCurrency Показывать ли символ валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @param useMinimalDecimals Если true и число целое, не показывать десятичные знаки (например, .00)
     * @return Отформатированная строка
     */
    fun format(
        showCurrency: Boolean = true,
        showSign: Boolean = false,
        useMinimalDecimals: Boolean = false
    ): String {
        val locale = Locale.getDefault()
        val symbols = DecimalFormatSymbols(locale)

        val strippedAmount = amount.stripTrailingZeros()

        if (useMinimalDecimals && strippedAmount.scale() <= 0) {
            val integerPattern = StringBuilder()
            if (showSign && amount > BigDecimal.ZERO) {
                integerPattern.append('+')
            }
            integerPattern.append("#,##0")

            val formatter = DecimalFormat(integerPattern.toString(), symbols)
            val formatted = formatter.format(strippedAmount)

            return if (showCurrency) {
                "$formatted ${currency.symbol}"
            } else {
                formatted
            }
        } else {
            val positivePattern = StringBuilder()
            if (showSign && amount > BigDecimal.ZERO) {
                positivePattern.append('+')
            }
            positivePattern.append("#,##0")

            if (currency.decimalPlaces > 0) {
                positivePattern.append('.')
                repeat(currency.decimalPlaces) { positivePattern.append('0') }
            }

            val fullPattern = positivePattern.toString()

            val formatter = DecimalFormat(fullPattern, symbols)
            formatter.roundingMode = RoundingMode.HALF_EVEN

            val formattedNum = formatter.format(amount)

            return if (showCurrency) {
                "$formattedNum ${currency.symbol}"
            } else {
                formattedNum
            }
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
     * Вычисляет процентное соотношение между текущим значением и другим
     * @param other Другое денежное значение
     * @return Процентное соотношение (текущее / другое * 100)
     * @throws IllegalArgumentException если валюты не совпадают
     */
    fun percentageOf(other: Money): Double {
        require(currency == other.currency) { "Cannot calculate percentage with different currencies" }
        if (other.isZero()) return 0.0
        return (amount.toDouble() / other.amount.toDouble()) * 100.0
    }

    /**
     * Вычисляет процентную разницу между текущим значением и другим
     * @param other Другое денежное значение
     * @return Процентная разница ((текущее - другое) / другое * 100)
     * @throws IllegalArgumentException если валюты не совпадают
     */
    fun percentageDifference(other: Money): Double {
        require(currency == other.currency) { "Cannot calculate percentage difference with different currencies" }
        if (other.isZero()) return if (isZero()) 0.0 else 100.0
        return ((amount.toDouble() - other.amount.toDouble()) / other.amount.toDouble()) * 100.0
    }

    override fun toString(): String {
        return format()
    }

    companion object {

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