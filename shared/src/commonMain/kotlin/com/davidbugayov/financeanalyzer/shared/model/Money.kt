package com.davidbugayov.financeanalyzer.shared.model

import kotlin.math.abs

/**
 * Упрощённая KMP-версия Money без BigDecimal.
 * Хранит сумму в минимальных единицах (cents) для точности.
 */
data class Money(
    val minor: Long,
    val currency: Currency = Currency.RUB,
) : Comparable<Money> {

    init {
        require(currency.fractionDigits in 0..6) { "Unsupported fraction digits: ${currency.fractionDigits}" }
    }

    companion object {
        fun zero(currency: Currency = Currency.RUB): Money = Money(0, currency)
        fun fromMajor(major: Double, currency: Currency = Currency.RUB): Money {
            val factor = 10.0.powInt(currency.fractionDigits)
            return Money((major * factor).toLong(), currency)
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Different currencies" }
        return Money(minor + other.minor, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Different currencies" }
        return Money(minor - other.minor, currency)
    }

    operator fun times(multiplier: Int): Money = Money(minor * multiplier, currency)

    operator fun div(divisor: Int): Money = Money(minor / divisor, currency)

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Different currencies" }
        return minor.compareTo(other.minor)
    }

    fun abs(): Money = if (minor < 0) Money(abs(minor), currency) else this

    fun isPositive(): Boolean = minor > 0
    fun isNegative(): Boolean = minor < 0
    fun isZero(): Boolean = minor == 0L

    fun toMajorDouble(): Double = minor.toDouble() / 10.0.powInt(currency.fractionDigits)
    fun toPlainString(): String {
        val frac = currency.fractionDigits
        if (frac == 0) return minor.toString()
        val base = 10.0.powInt(frac).toLong()
        val sign = if (minor < 0) "-" else ""
        val absMinor = kotlin.math.abs(minor)
        val intPart = absMinor / base
        val fracPart = (absMinor % base).toString().padStart(frac, '0')
        return "$sign$intPart.$fracPart"
    }
}

private fun Double.powInt(exp: Int): Double {
    var result = 1.0
    repeat(exp) { result *= 10.0 }
    return result
}


