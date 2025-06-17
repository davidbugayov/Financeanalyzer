package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import timber.log.Timber

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
     * Форматирует диапазон дат в виде строки
     *
     * @param startDate начальная дата диапазона
     * @param endDate конечная дата диапазона
     * @param pattern шаблон форматирования для отдельных дат
     * @return отформатированная строка с диапазоном дат
     */
    fun formatDateRange(startDate: Date, endDate: Date, pattern: String = "dd.MM.yyyy"): String {
        return "${formatDate(startDate, pattern)} - ${formatDate(endDate, pattern)}"
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

}
