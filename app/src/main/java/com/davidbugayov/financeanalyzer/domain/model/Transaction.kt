package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val note: String? = null,
    val currency: String = "RUB",
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null, // DAILY, WEEKLY, MONTHLY, YEARLY
    val attachmentUri: String? = null, // For receipt photos
    val sourceType: String = "MANUAL", // MANUAL, VOICE, IMPORT, OCR
    val templateId: Long? = null, // Reference to template if created from one
    val syncStatus: String = "LOCAL" // LOCAL, SYNCED, PENDING
)

data class TransactionWithTags(
    @Embedded
    val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "transaction_id",
        entity = TransactionTag::class,
        projection = ["tag"]
    )
    val tags: List<String>
) 