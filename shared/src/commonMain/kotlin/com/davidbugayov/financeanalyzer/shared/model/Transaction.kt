package com.davidbugayov.financeanalyzer.shared.model

import kotlinx.datetime.LocalDate

/**
 * Переносимая KMP-модель транзакции.
 */
data class Transaction(
    val id: String,
    val amount: Money,
    val category: String,
    val date: LocalDate,
    val isExpense: Boolean,
    val note: String? = null,
    val source: String,
    val subcategoryId: Long? = null,
)


