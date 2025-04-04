package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import java.util.Date

/**
 * Entity для хранения транзакций в базе данных Room.
 * Представляет таблицу transactions в базе данных.
 */
@Entity(
    tableName = "transactions",
    primaryKeys = ["id"]
)
data class TransactionEntity(
    val id: Long = 0,
    val amount: String,
    val category: String,
    val isExpense: Boolean,
    val date: Date,
    val note: String? = null,
    val source: String = "Наличные",
    val sourceColor: Int = 0
)