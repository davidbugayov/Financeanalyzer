package com.davidbugayov.financeanalyzer.util

import kotlin.math.abs

/**
 * Форматирует число для отображения в компактном или сокращенном виде
 * @param number Число для форматирования
 * @param maxLength Максимальная длина результата (по умолчанию без ограничений)
 * @return Отформатированное число в виде строки
 */
fun formatNumber(
    number: Double,
    maxLength: Int? = null
): String {
    val absNumber = abs(number)
    val prefix = if (number < 0) "-" else ""

    // Пробуем сначала отформатировать с десятичными знаками
    val fullFormatted = when {
        absNumber >= 1_000_000 -> "${prefix}${String.format("%.1fM", absNumber / 1_000_000)}"
        absNumber >= 1_000 -> "${prefix}${String.format("%.1fK", absNumber / 1_000)}"
        else -> "${prefix}${String.format("%.2f", absNumber)}"
    }

    // Если число помещается полностью или нет ограничения длины
    if (maxLength == null || fullFormatted.length <= maxLength) {
        return fullFormatted
    }

    // Иначе используем сокращенный формат
    return when {
        absNumber >= 1_000_000 -> "${prefix}${String.format("%.0fM", absNumber / 1_000_000)}"
        absNumber >= 1_000 -> "${prefix}${String.format("%.0fK", absNumber / 1_000)}"
        else -> "${prefix}${String.format("%.0f", absNumber)}"
    }
}

/**
 * Форматирует сумму транзакции, скрывая десятичные знаки, если они нулевые
 */
fun formatTransactionAmount(amount: Double): String {
    val absAmount = abs(amount)
    val prefix = if (amount < 0) "-" else ""

    return if (absAmount % 1.0 == 0.0) {
        // Если число целое, форматируем без десятичных знаков
        "${prefix}${String.format("%.0f", absAmount)}"
    } else {
        // Если есть десятичная часть, показываем два знака после запятой
        "${prefix}${String.format("%.2f", absAmount)}"
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
            val hasDecimals = absNumber % 1 != 0.0
            if (hasDecimals) {
                "${prefix}${String.format("%.2f", absNumber)}$currencySymbol"
            } else {
                "${prefix}${String.format("%.0f", absNumber)}$currencySymbol"
            }
        }
    }
}
