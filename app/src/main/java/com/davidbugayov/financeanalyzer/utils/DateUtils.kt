package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Утилитарный класс для работы с датами
 */
object DateUtils {

    /**
     * Форматирует дату в строку для отображения
     */
    fun formatDate(date: Date, pattern: String = "dd.MM.yyyy"): String {
        val format = SimpleDateFormat(pattern, Locale("ru"))
        return format.format(date)
    }

    /**
     * Возвращает даты начала и конца периода в зависимости от типа периода
     */
    fun getPeriodDates(periodType: PeriodType): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val now = calendar.time

        // Устанавливаем время в конец дня для конечной даты
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)

        when (periodType) {
            PeriodType.DAY -> {
                // Устанавливаем время в начало дня для начальной даты
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                val startDate = calendar.time

                // Возвращаем в конец дня для конечной даты
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)

                val endDate = calendar.time

                return Pair(startDate, endDate)
            }

            PeriodType.WEEK -> {
                // Устанавливаем первый день недели (понедельник)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                val startDate = calendar.time

                // Устанавливаем последний день недели (воскресенье)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)

                val endDate = calendar.time

                return Pair(startDate, endDate)
            }

            PeriodType.MONTH -> {
                // Устанавливаем первый день месяца
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                val startDate = calendar.time

                // Устанавливаем последний день месяца
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)

                val endDate = calendar.time

                return Pair(startDate, endDate)
            }

            PeriodType.QUARTER -> {
                // Определяем текущий квартал
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentQuarter = currentMonth / 3

                // Устанавливаем первый месяц квартала
                calendar.set(Calendar.MONTH, currentQuarter * 3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                val startDate = calendar.time

                // Устанавливаем последний день последнего месяца квартала
                calendar.add(Calendar.MONTH, 3)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)

                val endDate = calendar.time

                return Pair(startDate, endDate)
            }

            PeriodType.YEAR -> {
                // Устанавливаем первый день года
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                val startDate = calendar.time

                // Устанавливаем последний день года
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)

                val endDate = calendar.time

                return Pair(startDate, endDate)
            }

            PeriodType.ALL -> {
                // Для "всех времен" устанавливаем очень раннюю дату как начало и текущую дату как конец
                calendar.set(Calendar.YEAR, 2020)
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                val startDate = calendar.time

                // Текущая дата (конец дня)
                calendar.time = now
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)

                val endDate = calendar.time

                return Pair(startDate, endDate)
            }

            PeriodType.CUSTOM -> {
                // Для пользовательского периода возвращаем текущие даты из состояния
                return Pair(now, now)
            }
        }
    }
} 