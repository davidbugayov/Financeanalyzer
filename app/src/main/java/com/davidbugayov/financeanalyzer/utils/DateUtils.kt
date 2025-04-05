package com.davidbugayov.financeanalyzer.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Утилитарный класс для работы с датами
 */
object DateUtils {
    private val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    /**
     * Форматирует дату для отображения на экране
     *
     * @param date дата для форматирования
     * @return форматированная строка даты
     */
    fun formatForDisplay(date: Date): String {
        return displayFormat.format(date)
    }

    /**
     * Вспомогательный метод для преобразования строки в Double
     *
     * @return преобразованное значение или 0.0, если преобразование не удалось
     */
    fun String.toDouble(): Double {
        return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    /**
     * Преобразует форматированную строку суммы в число
     *
     * @param amount форматированная строка суммы
     * @return значение суммы в виде числа
     */
    fun parseFormattedAmount(amount: String): Double {
        return amount.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    }
}

/**
 * Расширение для форматирования даты
 */
fun Date.formatForDisplay(): String {
    return DateUtils.formatForDisplay(this)
}

/**
 * Расширение для преобразования строки в Double
 */
fun String.toDouble(): Double {
    return this.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
} 