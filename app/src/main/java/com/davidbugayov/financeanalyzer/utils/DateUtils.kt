package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Утилитарный класс для работы с датами
 */
object DateUtils {

    /**
     * Форматирует дату по указанному шаблону
     *
     * @param date дата для форматирования
     * @param pattern шаблон форматирования
     * @return отформатированная строка
     */
    fun formatDate(date: Date, pattern: String = "dd.MM.yyyy"): String {
        val format = SimpleDateFormat(pattern, Locale.forLanguageTag("ru"))
        return format.format(date)
    }

    /**
     * Возвращает даты начала и конца периода в зависимости от типа периода
     */
    fun getPeriodDates(periodType: PeriodType): Pair<Date, Date> {
        return calculateDatesForPeriod(periodType)
    }

    /**
     * Рассчитывает даты начала и конца периода на основе типа периода.
     * Этот метод должен использоваться во всем приложении для обеспечения
     * согласованности расчета периодов между различными экранами.
     * * @param periodType Тип периода (день, неделя, месяц и т.д.)
     * @return Пара дат (начало, конец) для указанного периода
     */
    fun calculateDatesForPeriod(periodType: PeriodType): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val startDate = when (periodType) {
            PeriodType.ALL -> calendar.apply {
                add(Calendar.YEAR, -5)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            PeriodType.DAY -> calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                Timber.d("DAY период: начало дня = ${formatDate(time, "dd.MM.yyyy")}")
            }.time

            PeriodType.WEEK -> calendar.apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                Timber.d("WEEK период: неделя назад = ${formatDate(time, "dd.MM.yyyy")}")
            }.time

            PeriodType.MONTH -> calendar.apply {
                add(Calendar.MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            PeriodType.QUARTER -> calendar.apply {
                add(Calendar.MONTH, -3)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            PeriodType.YEAR -> calendar.apply {
                add(Calendar.YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            PeriodType.CUSTOM -> calendar.apply {
                add(Calendar.MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }

        return Pair(startDate, endDate)
    }

    /**
     * Возвращает текущую дату
     */
    fun getTodayDate(): LocalDate {
        return LocalDate.now()
    }

    /**
     * Возвращает дату начала текущего года
     */
    fun getYearStartDate(): LocalDate {
        return LocalDate.now().withDayOfYear(1)
    }

    /**
     * Возвращает дату начала текущего месяца
     */
    fun getMonthStartDate(): LocalDate {
        return LocalDate.now().withDayOfMonth(1)
    }

    /**
     * Возвращает дату начала предыдущего месяца
     */
    fun getPreviousMonthStartDate(): LocalDate {
        return LocalDate.now().minusMonths(1).withDayOfMonth(1)
    }

    /**
     * Возвращает дату начала текущей недели (понедельник)
     */
    fun getWeekStartDate(): LocalDate {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value
        return today.minusDays((dayOfWeek - 1).toLong())
    }

    /**
     * Обрезает дату до дня и возвращает объект Date, содержащий только день
     */
    fun truncateToDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Обновляет тип периода и возвращает рассчитанные даты начала и конца.
     * Единая логика работы с периодами для всего приложения.
     * * @param periodType Тип периода (день, неделя, месяц и т.д.)
     * @param currentStartDate Текущая дата начала (для CUSTOM периода)
     * @param currentEndDate Текущая дата конца (для CUSTOM периода)
     * @return Пара дат (начало, конец) для указанного периода
     */
    fun updatePeriodDates(
        periodType: PeriodType,
        currentStartDate: Date = Date(),
        currentEndDate: Date = Date(),
    ): Pair<Date, Date> {
        return when (periodType) {
            PeriodType.CUSTOM -> {
                // Для пользовательского периода сохраняем текущие даты
                Timber.d("Период CUSTOM: $currentStartDate - $currentEndDate")
                Pair(currentStartDate, currentEndDate)
            }
            else -> {
                // Для всех других периодов используем рассчитанные даты
                calculateDatesForPeriod(periodType)
            }
        }
    }

    /**
     * Вычисляет разницу между датами в днях
     *
     * @param date1 первая дата
     * @param date2 вторая дата
     * @return разница в днях
     */
    fun getDaysBetween(date1: Date, date2: Date): Int {
        val diffInMillis = Math.abs(date2.time - date1.time)
        return (diffInMillis / TimeUnit.DAYS.toMillis(1)).toInt()
    }
}
