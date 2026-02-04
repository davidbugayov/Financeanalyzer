package com.davidbugayov.financeanalyzer.presentation.util

import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.utils.AppLocale
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Утилитарный класс для работы с UI компонентами
 */
object UiUtils {
    /**
     * Форматирует период для отображения в пользовательском интерфейсе
     * @param context Контекст для доступа к ресурсам
     * @param periodType Тип периода
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Строка с форматированным периодом
     */
    fun formatPeriod(
        context: android.content.Context,
        periodType: PeriodType,
        startDate: Date,
        endDate: Date,
    ): String {
        val locale = AppLocale.getCurrentLocale()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", locale)
        val dayMonth = SimpleDateFormat("d MMMM", locale)
        val dayOfWeek = SimpleDateFormat("EEEE", locale)
        val monthYear = SimpleDateFormat("MMMM yyyy", locale)

        return when (periodType) {
            PeriodType.ALL -> context.getString(UiR.string.period_all_time)
            PeriodType.DAY ->
                context.getString(
                    UiR.string.period_day,
                    dayMonth.format(startDate),
                    dayOfWeek.format(startDate),
                )
            PeriodType.WEEK ->
                context.getString(
                    UiR.string.period_week,
                    dateFormat.format(startDate),
                    dateFormat.format(endDate),
                )
            PeriodType.MONTH ->
                context.getString(UiR.string.period_month, monthYear.format(startDate))
            PeriodType.QUARTER -> {
                val now = java.util.Calendar.getInstance()
                val currentQuarter = ((now.get(java.util.Calendar.MONTH) / 3) + 1)
                val quarterNames = arrayOf("", "I", "II", "III", "IV")
                val currentYear = now.get(java.util.Calendar.YEAR)
                context.getString(UiR.string.period_quarter, quarterNames[currentQuarter], currentYear)
            }
            PeriodType.YEAR -> {
                val now = java.util.Calendar.getInstance()
                val currentYear = now.get(java.util.Calendar.YEAR)
                context.resources.getQuantityString(UiR.plurals.period_year, currentYear, currentYear)
            }
            PeriodType.CUSTOM ->
                context.getString(
                    UiR.string.period_custom,
                    dateFormat.format(startDate),
                    dateFormat.format(endDate),
                )
        }
    }

    /**
     * Форматирует период в краткой форме для компактного отображения
     * @param context Контекст для доступа к ресурсам
     * @param periodType Тип периода
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Строка с кратким форматированным периодом
     */
    fun formatPeriodCompact(
        context: android.content.Context,
        periodType: PeriodType,
        startDate: Date,
        endDate: Date,
    ): String {
        val locale = AppLocale.getCurrentLocale()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", locale)

        return when (periodType) {
            PeriodType.ALL -> context.getString(UiR.string.period_all_time)
            PeriodType.DAY -> dateFormat.format(startDate)
            PeriodType.WEEK, PeriodType.MONTH, PeriodType.QUARTER, PeriodType.YEAR, PeriodType.CUSTOM ->
                dateFormat.format(startDate) + " - " + dateFormat.format(endDate)
        }
    }

    /**
     * Форматирует дату для отображения в формате дд.мм.гггг
     */
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", AppLocale.getCurrentLocale())
        return dateFormat.format(date)
    }

    /**
     * Форматирует дату для отображения в формате дд.мм.гггг
     */
    fun formatDateTime(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", AppLocale.getCurrentLocale())
        return dateFormat.format(date)
    }
}
