package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date
import java.util.UUID

/**
 * Модель данных для транзакции.
 * Представляет финансовую операцию (доход, расход или перевод).
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Money,
    val category: String,
    val date: Date = Date(),
    val isExpense: Boolean,
    val note: String? = null,
    val source: String,
    val sourceColor: Int,
    val categoryId: String = "",
    val title: String = "",
    val isTransfer: Boolean = false,
    val walletIds: List<String>? = null,
)
