package com.davidbugayov.financeanalyzer.domain.model.filter

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