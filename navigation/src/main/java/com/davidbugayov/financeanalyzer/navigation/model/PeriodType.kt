package com.davidbugayov.financeanalyzer.navigation.model

/**
 * Типы периодов для фильтрации транзакций.
 * Используется в навигации и для фильтрации списка транзакций.
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
