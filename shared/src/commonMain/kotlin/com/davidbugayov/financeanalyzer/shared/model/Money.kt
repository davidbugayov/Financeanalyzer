package com.davidbugayov.financeanalyzer.shared.model

import kotlin.math.abs
import java.math.BigDecimal
import java.math.RoundingMode

data class Money(
    val amount: BigDecimal,
    val currency: Currency = Currency.RUB,
) : Comparable<Money> {

    init {
        require(currency.fractionDigits in 0..6) { "Unsupported fraction digits: ${currency.fractionDigits}" }
    }

    companion object {
        fun zero(currency: Currency = Currency.RUB): Money = Money(BigDecimal.ZERO, currency)
        fun fromMajor(major: Double, currency: Currency = Currency.RUB): Money {
            return Money(BigDecimal.valueOf(major), currency)
        }
        fun fromMajor(major: BigDecimal, currency: Currency = Currency.RUB): Money {
            return Money(major, currency)
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Different currencies" }
        return Money(amount.add(other.amount), currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Different currencies" }
        return Money(amount.subtract(other.amount), currency)
    }

    operator fun times(multiplier: Int): Money = Money(amount.multiply(BigDecimal.valueOf(multiplier.toLong())), currency)
    operator fun times(multiplier: Double): Money = Money(amount.multiply(BigDecimal.valueOf(multiplier)), currency)
    operator fun times(multiplier: BigDecimal): Money = Money(amount.multiply(multiplier), currency)

    operator fun div(divisor: Int): Money = Money(amount.divide(BigDecimal.valueOf(divisor.toLong()), 10, RoundingMode.HALF_EVEN), currency)
    operator fun div(divisor: Double): Money = Money(amount.divide(BigDecimal.valueOf(divisor), 10, RoundingMode.HALF_EVEN), currency)
    operator fun div(divisor: BigDecimal): Money = Money(amount.divide(divisor, 10, RoundingMode.HALF_EVEN), currency)

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Different currencies" }
        return amount.compareTo(other.amount)
    }

    fun abs(): Money = if (amount < BigDecimal.ZERO) Money(amount.abs(), currency) else this
    fun negate(): Money = Money(amount.negate(), currency)

    fun isPositive(): Boolean = amount > BigDecimal.ZERO
    fun isNegative(): Boolean = amount < BigDecimal.ZERO
    fun isZero(): Boolean = amount == BigDecimal.ZERO

    fun toMajorDouble(): Double = amount.toDouble()
    fun toPlainString(): String = amount.toPlainString()
    
    // Для обратной совместимости
    val formatted: String get() = toPlainString()
    
    // Методы для обратной совместимости
    fun toDouble(): Double = toMajorDouble()
    fun toInt(): Int = toMajorDouble().toInt()
    fun toLong(): Long = toMajorDouble().toLong()
    
    // Методы для работы с BigDecimal
    fun multiply(multiplier: Double): Money = this * multiplier
    fun divide(divisor: Double): Money = this / divisor
    fun multiply(multiplier: BigDecimal): Money = this * multiplier
    fun divide(divisor: BigDecimal): Money = this / divisor
    fun subtract(other: BigDecimal): Money = this - Money.fromMajor(other, currency)
    fun setScale(scale: Int, roundingMode: RoundingMode): Money = Money(amount.setScale(scale, roundingMode), currency)
    fun compareTo(other: BigDecimal): Int = amount.compareTo(other)
}
