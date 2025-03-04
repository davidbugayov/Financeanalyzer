package com.davidbugayov.financeanalyzer.util

import kotlin.math.abs

/**
 * Форматирует число для отображения в компактном или сокращенном виде
 * @param number Число для форматирования
 * @param useDecimals Использовать ли десятичные знаки (по умолчанию false)
 * @param decimals Количество знаков после запятой (по умолчанию 1)
 * @return Отформатированное число в виде строки
 */
fun formatNumber(
    number: Double,
    useDecimals: Boolean = false,
    decimals: Int = 1
): String {
    val absNumber = abs(number)
    val prefix = if (number < 0) "-" else ""
    return when {
        absNumber >= 1_000_000 -> {
            if (useDecimals) {
                "${prefix}${String.format("%." + decimals + "fM", absNumber / 1_000_000)}"
            } else {
                "${prefix}${String.format("%.0fM", absNumber / 1_000_000)}"
            }
        }
        absNumber >= 1_000 -> {
            if (useDecimals) {
                "${prefix}${String.format("%." + decimals + "fK", absNumber / 1_000)}"
            } else {
                "${prefix}${String.format("%.0fK", absNumber / 1_000)}"
            }
        }
        else -> {
            if (useDecimals) {
                "${prefix}${String.format("%." + decimals + "f", absNumber)}"
            } else {
                "${prefix}${String.format("%.0f", absNumber)}"
            }
        }
    }
}

/**
 * Форматирует число для отображения с символом валюты
 * @param number Число для форматирования
 * @param currencySymbol Символ валюты (по умолчанию "₽")
 * @return Отформатированное число с символом валюты
 */
fun formatNumberWithCurrency(
    number: Double,
    currencySymbol: String = "₽"
): String {
    val absNumber = abs(number)
    val prefix = if (number < 0) "-" else ""

    return when {
        absNumber >= 1_000_000 -> {
            val millions = absNumber / 1_000_000
            "${prefix}${String.format("%.1f", millions)}М$currencySymbol"
        }
        absNumber >= 1_000 -> {
            val thousands = absNumber / 1_000
            "${prefix}${String.format("%.1f", thousands)}К$currencySymbol"
        }
        else -> {
            "${prefix}${String.format("%.0f", absNumber)}$currencySymbol"
        }
    }
}
