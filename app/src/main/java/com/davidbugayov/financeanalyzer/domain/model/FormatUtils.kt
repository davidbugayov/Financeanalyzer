package com.davidbugayov.financeanalyzer.domain.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Форматирует число как денежную сумму в рублях.
 */
fun Double.formatAsCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    return format.format(this)
}

/**
 * Форматирует число как процент.
 */
fun Double.formatAsPercentage(): String {
    val format = NumberFormat.getPercentInstance(Locale("ru", "RU"))
    format.maximumFractionDigits = 1
    return format.format(this)
} 