package com.davidbugayov.financeanalyzer.shared.model

/**
 * Статистика по категории (KMP).
 */
data class CategoryStats(
    val category: String,
    val amount: Money,
    val percentage: Double,
    val count: Int,
    val isExpense: Boolean,
)


