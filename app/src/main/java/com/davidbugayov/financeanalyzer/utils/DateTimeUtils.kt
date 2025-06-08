package com.davidbugayov.financeanalyzer.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Утилитарный класс для работы с датами и временем
 */
object DateTimeUtils {

    /**
     * Возвращает дату начала периода по умолчанию (первый день текущего месяца)
     */
    fun getDefaultStartDate(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return LocalDate(today.year, today.monthNumber, 1)
    }

    /**
     * Возвращает дату окончания периода по умолчанию (текущая дата)
     */
    fun getDefaultEndDate(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    /**
     * Возвращает дату начала предыдущего месяца
     */
    fun getPreviousMonthStartDate(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        // Если текущий месяц январь, переходим к декабрю предыдущего года
        val (year, month) = if (today.monthNumber == 1) {
            Pair(today.year - 1, 12)
        } else {
            Pair(today.year, today.monthNumber - 1)
        }
        return LocalDate(year, month, 1)
    }

    /**
     * Возвращает дату начала текущей недели (понедельник)
     */
    fun getWeekStartDate(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dayOfWeek = today.dayOfWeek.value
        // В LocalDate понедельник имеет индекс 1, воскресенье - 7
        val daysToSubtract = dayOfWeek - 1
        return LocalDate(
            year = today.year,
            monthNumber = today.monthNumber,
            dayOfMonth = today.dayOfMonth - daysToSubtract,
        )
    }

    /**
     * Преобразует строку в дату в формате "dd.MM.yyyy"
     */
    fun parseDate(dateString: String): LocalDate? {
        try {
            val parts = dateString.split(".")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                return LocalDate(year, month, day)
            }
        } catch (e: Exception) {
            // Ошибка парсинга
        }
        return null
    }

    /**
     * Форматирует дату в строку в формате "dd.MM.yyyy"
     */
    fun formatDate(date: LocalDate): String {
        return "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(
            2,
            '0',
        )}.${date.year}"
    }
}
