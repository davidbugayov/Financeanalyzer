package com.davidbugayov.financeanalyzer.shared.model

import kotlin.math.abs
import kotlin.math.round

data class Money(
    val amount: Double,
    val currency: Currency = Currency.RUB,
) : Comparable<Money> {

    init {
        require(currency.fractionDigits in 0..6) { "Unsupported fraction digits: ${currency.fractionDigits}" }
    }

    companion object {
        fun zero(currency: Currency = Currency.RUB): Money = Money(0.0, currency)
        fun fromMajor(major: Double, currency: Currency = Currency.RUB): Money {
            return Money(major, currency)
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Different currencies" }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Different currencies" }
        return Money(amount - other.amount, currency)
    }

    operator fun times(multiplier: Int): Money = Money(amount * multiplier, currency)
    operator fun times(multiplier: Double): Money = Money(amount * multiplier, currency)

    operator fun div(divisor: Int): Money = Money(roundToDecimal(amount / divisor, currency.fractionDigits), currency)
    operator fun div(divisor: Double): Money = Money(roundToDecimal(amount / divisor, currency.fractionDigits), currency)

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Different currencies" }
        return amount.compareTo(other.amount)
    }

    fun abs(): Money = Money(kotlin.math.abs(amount), currency)
    fun negate(): Money = Money(-amount, currency)

    fun isPositive(): Boolean = amount > 0.0
    fun isNegative(): Boolean = amount < 0.0
    fun isZero(): Boolean = amount == 0.0

    fun toMajorDouble(): Double = amount
    fun toPlainString(): String = String.format("%.2f", amount)

    // Для обратной совместимости
    val formatted: String get() = toPlainString()

    // Методы для обратной совместимости
    fun toDouble(): Double = toMajorDouble()
    fun toInt(): Int = toMajorDouble().toInt()
    fun toLong(): Long = toMajorDouble().toLong()

    // Методы для работы с Double (для совместимости с BigDecimal API)
    fun multiply(multiplier: Double): Money = this * multiplier
    fun divide(divisor: Double): Money = this / divisor
    fun subtract(other: Double): Money = Money(amount - other, currency)
    fun add(other: Double): Money = Money(amount + other, currency)

    private fun roundToDecimal(value: Double, decimals: Int): Double {
        var factor = 1.0
        for (i in 1..decimals) {
            factor *= 10.0
        }
        return round(value * factor) / factor
    }
}
