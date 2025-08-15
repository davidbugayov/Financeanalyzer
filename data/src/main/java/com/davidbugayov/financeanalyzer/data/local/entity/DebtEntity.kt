package com.davidbugayov.financeanalyzer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.davidbugayov.financeanalyzer.core.model.Money

@Entity(
    tableName = "debts",
    indices = [Index(value = ["id_string"], unique = true)],
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_string") val idString: String,
    val title: String,
    val counterparty: String,
    val type: String,
    val status: String,
    val principal: Money,
    val remaining: Money,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "due_at") val dueAt: Long? = null,
    val note: String? = null,
)


