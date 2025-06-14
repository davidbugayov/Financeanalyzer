package com.davidbugayov.financeanalyzer.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import timber.log.Timber

/**
 * Перечисление для позиции символа валюты
 */
enum class SymbolPosition {

    BEFORE, AFTER
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
     * Проверка, является ли денежное значение положительным или равным нулю
     * @return true, если значение положительное или равно нулю
     */
    fun isPositive(): Boolean {
        return amount >= BigDecimal.ZERO
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
     * * @param showCurrency Показывать ли символ валюты
     * @param showSign Показывать ли знак + для положительных значений
     * @param useMinimalDecimals Если true и число целое, не показывать десятичные знаки (например, .00)
     * @return Отформатированная строка
     */
    fun format(showCurrency: Boolean = true, showSign: Boolean = false, useMinimalDecimals: Boolean = true): String {
        // Используем русскую локаль для гарантированного форматирования с пробелами
        val locale = Locale.forLanguageTag("ru-RU")
        val symbols = DecimalFormatSymbols(locale)

        // Явно устанавливаем пробел как разделитель групп (тысяч)
        symbols.groupingSeparator = ' '
        // Используем разделитель десятичных знаков из настроек валюты
        symbols.decimalSeparator = currency.decimalSeparator

        // Добавляем логирование для отладки
        Timber.d(
            "Money.format: amount=$amount, locale=$locale, groupingSeparator='${symbols.groupingSeparator}'",
        )

        // Для нулевых значений возвращаем просто "0"
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return if (showCurrency) {
                if (currency.symbolPosition == SymbolPosition.BEFORE) {
                    "${currency.symbol}0"
                } else {
                    "0 ${currency.symbol}"
                }
            } else {
                "0"
            }
        }

        val strippedAmount = amount.stripTrailingZeros()
        val isWholeNumber = strippedAmount.scale() <= 0 || strippedAmount.remainder(BigDecimal.ONE).compareTo(
            BigDecimal.ZERO,
        ) == 0

        // Используем шаблон с запятой как стандартным местозаполнителем для группировки
        val groupingPattern = "#,##0"

        if (useMinimalDecimals && isWholeNumber) {
            val integerPattern = StringBuilder()
            if (showSign && amount > BigDecimal.ZERO) {
                integerPattern.append('+')
            }
            integerPattern.append(groupingPattern)

            val formatter = DecimalFormat(integerPattern.toString(), symbols)
            formatter.isGroupingUsed = true
            formatter.groupingSize = 3
            val formatted = formatter.format(strippedAmount)

            // Логирование результата
            Timber.d("Money.format result (integer): $formatted")

            return if (showCurrency) {
                if (currency.symbolPosition == SymbolPosition.BEFORE) {
                    "${currency.symbol}$formatted"
                } else {
                    "$formatted ${currency.symbol}"
                }
            } else {
                formatted
            }
        } else {
            val decimalPattern = StringBuilder()
            if (showSign && amount > BigDecimal.ZERO) {
                decimalPattern.append('+')
            }
            decimalPattern.append(groupingPattern)
            // Добавляем десятичную часть в соответствии с количеством знаков валюты
            if (currency.decimalPlaces > 0) {
                decimalPattern.append('.')
                repeat(currency.decimalPlaces) {
                    decimalPattern.append('0')
                }
            }

            val formatter = DecimalFormat(decimalPattern.toString(), symbols)
            formatter.isGroupingUsed = true
            formatter.groupingSize = 3
            val formatted = formatter.format(amount)

            // Логирование результата
            Timber.d("Money.format result (decimal): $formatted")

            return if (showCurrency) {
                if (currency.symbolPosition == SymbolPosition.BEFORE) {
                    "${currency.symbol}$formatted"
                } else {
                    "$formatted ${currency.symbol}"
                }
            } else {
                formatted
            }
        }
    }

    override fun toString(): String {
        return format()
    }
}

/**
 * Функция-расширение для форматирования денежной суммы для отображения в UI.
 *
 * @param showCurrency Показывать ли символ валюты.
 * @param showSign Показывать ли знак "+" для положительных значений.
 * @param useMinimalDecimals Если true и сумма целая, не показывать десятичные знаки.
 * @return Отформатированная строка.
 */
fun Money.formatForDisplay(showCurrency: Boolean = true, showSign: Boolean = false, useMinimalDecimals: Boolean = true): String {
    return this.format(showCurrency, showSign, useMinimalDecimals)
}

/**
 * Функция-расширение для форматирования денежной суммы.
 *
 * @return Отформатированная строка.
 */
val Money.formatted: String
    get() = this.format()

/**
 * Рассчитывает процентное соотношение одной денежной суммы от другой.
 *
 * @param total Общая сумма, от которой считается процент.
 * @return Процентное соотношение (от 0.0 до 100.0).
 * @throws IllegalArgumentException если валюты не совпадают или total равен нулю.
 */
fun Money.percentageOf(total: Money): Double {
    require(currency == total.currency) { "Cannot calculate percentage of money with different currencies" }
    require(!total.isZero()) { "Cannot calculate percentage of zero total" }
    return amount.divide(total.amount, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal(100)).toDouble()
}

/**
 * Рассчитывает процентную разницу между двумя денежными суммами.
 *
 * @param other Сумма, с которой сравнивается текущая.
 * @return Процентная разница.
 * @throws IllegalArgumentException если валюты не совпадают или other равен нулю.
 */
fun Money.percentageDifference(other: Money): Double {
    require(currency == other.currency) { "Cannot calculate percentage difference of money with different currencies" }
    require(!other.isZero()) { "Cannot calculate percentage difference from zero" }
    return (amount.subtract(other.amount)).divide(other.amount, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal(100)).toDouble()
}

/**
 * Функции-расширения для работы с денежными значениями
 */

/**
 * Форматирует BigDecimal как денежную сумму
 */
val BigDecimal.formatted: String
    get() = formatForDisplay()

/**
 * Форматирует BigDecimal как денежную сумму с дополнительными опциями
 * 
 * @param showCurrency показывать ли символ валюты
 * @param showSign показывать ли знак + для положительных чисел
 * @return отформатированная строка
 */
fun BigDecimal.formatted(showCurrency: Boolean = false, showSign: Boolean = false): String {
    return formatForDisplay(showCurrency, showSign)
}

/**
 * Форматирует BigDecimal как денежную сумму с дополнительными опциями
 * 
 * @param showCurrency показывать ли символ валюты
 * @param showSign показывать ли знак + для положительных чисел
 * @return отформатированная строка
 */
fun BigDecimal.formatForDisplay(showCurrency: Boolean = false, showSign: Boolean = false): String {
    val formatter = if (showCurrency) {
        NumberFormat.getCurrencyInstance(Locale.getDefault())
    } else {
        DecimalFormat("#,##0.00")
    }
    
    val formattedValue = formatter.format(this)
    
    return if (showSign && this >= BigDecimal.ZERO) {
        "+$formattedValue"
    } else {
        formattedValue
    }
}

/**
 * Вычисляет процентное соотношение между двумя суммами
 * 
 * @param total общая сумма
 * @return процент от общей суммы
 */
fun BigDecimal.percentageOf(total: BigDecimal): Float {
    if (total == BigDecimal.ZERO) return 0f
    return (this.toFloat() / total.toFloat()) * 100f
}

/**
 * Вычисляет процентную разницу между двумя суммами
 * 
 * @param previous предыдущая сумма
 * @return процентная разница
 */
fun BigDecimal.percentageDifference(previous: BigDecimal): Float {
    if (previous == BigDecimal.ZERO) return if (this > BigDecimal.ZERO) 100f else 0f
    return ((this.toFloat() - previous.toFloat()) / previous.toFloat()) * 100f
}
