package com.davidbugayov.financeanalyzer.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

/**
 * Перечисление для позиции символа валюты
 */
enum class SymbolPosition {
    BEFORE,
    AFTER,
}

/**
 * Перечисление для валют
 * @property symbol Символ валюты
 * @property code Код валюты по ISO 4217
 * @property decimalPlaces Количество знаков после запятой
 * @property decimalSeparator Разделитель десятичной части
 * @property symbolPosition Позиция символа валюты (до или после суммы)
 */
enum class Currency(
    val symbol: String,
    val code: String,
    val decimalPlaces: Int,
    val decimalSeparator: Char,
    val symbolPosition: SymbolPosition,
) {
    RUB("₽", "RUB", 2, ',', SymbolPosition.AFTER),
    USD("$", "USD", 2, '.', SymbolPosition.BEFORE),
    EUR("€", "EUR", 2, ',', SymbolPosition.AFTER),
    GBP("£", "GBP", 2, '.', SymbolPosition.BEFORE),
    JPY("¥", "JPY", 0, '.', SymbolPosition.BEFORE),
    CNY("¥", "CNY", 2, '.', SymbolPosition.BEFORE),
    KZT("₸", "KZT", 2, ',', SymbolPosition.AFTER),
    BYN("Br", "BYN", 2, ',', SymbolPosition.AFTER),
    ;

    companion object {
        /**
         * Получает валюту по коду
         * @param code Код валюты
         * @return Валюта или RUB, если валюта не найдена
         */
        fun fromCode(code: String): Currency {
            return values().find { it.code == code } ?: RUB
        }
    }
}

/**
 * Класс для представления денежных значений.
 * Использует BigDecimal для точных вычислений с плавающей точкой.
 *
 * @property amount Сумма денег
 * @property currency Валюта
 */
data class Money(
    val amount: BigDecimal,
    val currency: Currency = Currency.RUB,
) {
    init {
        require(amount.scale() <= currency.decimalPlaces) {
            "Amount scale (${amount.scale()}) exceeds currency decimal places (${currency.decimalPlaces})"
        }
    }

    constructor(amount: Double, currency: Currency = Currency.RUB) : this(
        BigDecimal.valueOf(amount).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency,
    )

    constructor(amount: Int, currency: Currency = Currency.RUB) : this(
        BigDecimal.valueOf(amount.toLong()).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency,
    )

    constructor(amount: Long, currency: Currency = Currency.RUB) : this(
        BigDecimal.valueOf(amount).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency,
    )

    constructor(amount: String, currency: Currency = Currency.RUB) : this(
        BigDecimal(amount).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
        currency,
    )

    companion object {
        /**
         * Создает нулевое денежное значение
         * @param currency Валюта (по умолчанию RUB)
         * @return Денежное значение, равное нулю
         */
        fun zero(currency: Currency = Currency.RUB): Money {
            return Money(BigDecimal.ZERO, currency)
        }
    }

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
        return Money(
            amount.multiply(multiplier).setScale(currency.decimalPlaces, RoundingMode.HALF_EVEN),
            currency,
        )
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
        return Money(
            amount.divide(divisor, currency.decimalPlaces, RoundingMode.HALF_EVEN),
            currency,
        )
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
        require(currency == other.currency) {
            "Cannot compare money with different currencies: $currency and ${other.currency}"
        }
        return amount.compareTo(other.amount)
    }

    /**
     * Проверяет, является ли денежное значение положительным
     * @return true, если значение больше нуля
     */
    fun isPositive(): Boolean = amount > BigDecimal.ZERO

    /**
     * Проверяет, является ли денежное значение отрицательным
     * @return true, если значение меньше нуля
     */
    fun isNegative(): Boolean = amount < BigDecimal.ZERO

    /**
     * Проверяет, является ли денежное значение нулевым
     * @return true, если значение равно нулю
     */
    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0

    /**
     * Возвращает абсолютное значение денежной суммы
     * @return Денежное значение с положительной суммой
     */
    fun abs(): Money = if (isNegative()) Money(amount.abs(), currency) else this

    /**
     * Возвращает отрицательное значение денежной суммы
     * @return Денежное значение с отрицательной суммой
     */
    fun negate(): Money = Money(amount.negate(), currency)

    /**
     * Возвращает значение суммы в виде Double
     * @return Сумма в виде Double
     */
    fun toDouble(): Double = amount.toDouble()

    /**
     * Возвращает значение суммы в виде Int
     * @return Сумма в виде Int (округленная до целого)
     */
    fun toInt(): Int = amount.setScale(0, RoundingMode.HALF_EVEN).toInt()

    /**
     * Возвращает значение суммы в виде Long
     * @return Сумма в виде Long (округленная до целого)
     */
    fun toLong(): Long = amount.setScale(0, RoundingMode.HALF_EVEN).toLong()

    /**
     * Возвращает значение суммы в виде строки без символа валюты
     * @return Строковое представление суммы
     */
    fun toPlainString(): String = amount.toPlainString()

    /**
     * Возвращает отформатированное значение суммы с символом валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @return Форматированная строка с суммой и символом валюты
     */
    fun format(showSign: Boolean = false): String {
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = currency.decimalSeparator
        symbols.groupingSeparator = ' '

        val formatter = NumberFormat.getInstance() as DecimalFormat
        formatter.decimalFormatSymbols = symbols
        formatter.minimumFractionDigits = currency.decimalPlaces
        formatter.maximumFractionDigits = currency.decimalPlaces

        if (showSign && amount.signum() > 0) {
            formatter.positivePrefix = "+"
        }

        val formattedAmount = formatter.format(amount)
        return when (currency.symbolPosition) {
            SymbolPosition.BEFORE -> "${currency.symbol}$formattedAmount"
            SymbolPosition.AFTER -> "$formattedAmount ${currency.symbol}"
        }
    }

    /**
     * Возвращает отформатированное значение суммы с символом валюты
     * @return Форматированная строка с суммой и символом валюты
     */
    val formatted: String
        get() {
            val symbols = DecimalFormatSymbols()
            symbols.decimalSeparator = currency.decimalSeparator
            symbols.groupingSeparator = ' '

            val formatter = NumberFormat.getInstance() as DecimalFormat
            formatter.decimalFormatSymbols = symbols
            formatter.minimumFractionDigits = currency.decimalPlaces
            formatter.maximumFractionDigits = currency.decimalPlaces

            val formattedAmount = formatter.format(amount)
            return when (currency.symbolPosition) {
                SymbolPosition.BEFORE -> "${currency.symbol}$formattedAmount"
                SymbolPosition.AFTER -> "$formattedAmount ${currency.symbol}"
            }
        }

    /**
     * Возвращает отформатированное значение суммы с кодом валюты
     * @return Форматированная строка с суммой и кодом валюты
     */
    val formattedWithCode: String
        get() {
            val symbols = DecimalFormatSymbols()
            symbols.decimalSeparator = currency.decimalSeparator
            symbols.groupingSeparator = ' '

            val formatter = NumberFormat.getInstance() as DecimalFormat
            formatter.decimalFormatSymbols = symbols
            formatter.minimumFractionDigits = currency.decimalPlaces
            formatter.maximumFractionDigits = currency.decimalPlaces

            return "${formatter.format(amount)} ${currency.code}"
        }
}
