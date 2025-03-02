package com.davidbugayov.financeanalyzer.domain.model

import java.util.Date

/**
 * Доменная модель транзакции.
 * Чистая модель данных, не зависящая от фреймворков и библиотек.
 */
data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String? = null
) 