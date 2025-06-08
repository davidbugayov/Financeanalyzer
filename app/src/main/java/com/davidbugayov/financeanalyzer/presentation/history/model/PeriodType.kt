package com.davidbugayov.financeanalyzer.presentation.history.model

/**
 * Типы периодов для фильтрации транзакций в истории.
 * Используется в диалоге выбора периода и для фильтрации списка транзакций.
 */
enum class PeriodType {
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    ALL,
    CUSTOM,
}
