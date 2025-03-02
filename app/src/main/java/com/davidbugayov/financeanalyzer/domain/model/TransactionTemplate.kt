package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_templates")
data class TransactionTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double? = null,
    val isExpense: Boolean,
    val tags: List<String> = emptyList(),
    val currency: String = "RUB",
    val note: String? = null,
    val isQuickAccess: Boolean = false // For frequently used templates
) 