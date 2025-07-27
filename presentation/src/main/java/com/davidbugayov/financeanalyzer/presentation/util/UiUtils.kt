package com.davidbugayov.financeanalyzer.presentation.util

import com.davidbugayov.financeanalyzer.domain.util.StringProvider
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Утилитарный класс для работы с UI компонентами
 */
object UiUtils {
    /**
     * Форматирует период для отображения в пользовательском интерфейсе
     * @param periodType Тип периода
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Строка с форматированным периодом
     */
    fun formatPeriod(
        periodType: PeriodType,
        startDate: Date,
        endDate: Date,
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))

        return when (periodType) {
            PeriodType.ALL -> StringProvider.periodAllTime
            PeriodType.DAY -> StringProvider.periodDay(dateFormat.format(startDate))
            PeriodType.WEEK -> StringProvider.periodWeek(dateFormat.format(startDate), dateFormat.format(endDate))
            PeriodType.MONTH -> StringProvider.periodMonth(dateFormat.format(startDate), dateFormat.format(endDate))
            PeriodType.QUARTER -> StringProvider.periodQuarter(dateFormat.format(startDate), dateFormat.format(endDate))
            PeriodType.YEAR -> StringProvider.periodYear(dateFormat.format(startDate), dateFormat.format(endDate))
            PeriodType.CUSTOM -> StringProvider.periodCustom(dateFormat.format(startDate), dateFormat.format(endDate))
        }
    }

    /**
     * Форматирует период в краткой форме для компактного отображения
     * @param periodType Тип периода
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Строка с кратким форматированным периодом
     */
    fun formatPeriodCompact(
        periodType: PeriodType,
        startDate: Date,
        endDate: Date,
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))

        return when (periodType) {
            PeriodType.ALL -> StringProvider.periodAllTime
            PeriodType.DAY -> dateFormat.format(startDate)
            PeriodType.WEEK, PeriodType.MONTH, PeriodType.QUARTER, PeriodType.YEAR, PeriodType.CUSTOM ->
                dateFormat.format(startDate) + " - " + dateFormat.format(endDate)
        }
    }

    /**
     * Форматирует дату для отображения в формате дд.мм.гггг
     */
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))
        return dateFormat.format(date)
    }

    /**
     * Форматирует дату для отображения в формате дд.мм.гггг
     */
    fun formatDateTime(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))
        return dateFormat.format(date)
    }
}
