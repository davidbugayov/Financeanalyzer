package com.davidbugayov.financeanalyzer.presentation.util

import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Утилитарный класс для работы с UI компонентами
 */
object UiUtils {

    /**
     * Форматирует период для отображения в пользовательском интерфейсе
     * * @param periodType Тип периода
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Строка с форматированным периодом
     */
    fun formatPeriod(periodType: PeriodType, startDate: Date, endDate: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))

        return when (periodType) {
            PeriodType.ALL -> "Все время"
            PeriodType.DAY -> "День: ${dateFormat.format(startDate)}"
            PeriodType.WEEK -> "Неделя: ${dateFormat.format(startDate)} - ${dateFormat.format(
                endDate
            )}"
            PeriodType.MONTH -> "Месяц: ${dateFormat.format(startDate)} - ${dateFormat.format(
                endDate
            )}"
            PeriodType.QUARTER -> "Квартал: ${dateFormat.format(startDate)} - ${dateFormat.format(
                endDate
            )}"
            PeriodType.YEAR -> "Год: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            PeriodType.CUSTOM -> "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
    }

    /**
     * Форматирует период в краткой форме для компактного отображения
     * * @param periodType Тип периода
     * @param startDate Начальная дата периода
     * @param endDate Конечная дата периода
     * @return Строка с кратким форматированным периодом
     */
    fun formatPeriodCompact(periodType: PeriodType, startDate: Date, endDate: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))

        return when (periodType) {
            PeriodType.ALL -> "Все время"
            PeriodType.DAY -> dateFormat.format(startDate)
            PeriodType.WEEK, PeriodType.MONTH, PeriodType.QUARTER, PeriodType.YEAR, PeriodType.CUSTOM ->
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
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
