package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date
import java.util.UUID

/**
 * Модель данных для транзакции.
 * Представляет финансовую операцию (доход или расход).
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: String,
    val date: Date = Date(),
    val isExpense: Boolean,
    val note: String? = null,
    val source: String? = null
) 