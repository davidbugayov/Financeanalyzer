package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "transaction_tags",
    primaryKeys = ["transaction_id", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("transaction_id"),
        Index("tag")
    ]
)
data class TransactionTag(
    val transaction_id: Long,
    val tag: String
) 