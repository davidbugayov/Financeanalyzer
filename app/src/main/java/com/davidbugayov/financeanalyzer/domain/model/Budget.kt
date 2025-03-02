package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val limit: Double,
    val currency: String = "RUB",
    val period: String = "MONTHLY", // WEEKLY, MONTHLY, YEARLY
    val startDate: Date,
    val endDate: Date? = null,
    val notificationThreshold: Double = 0.8, // Notify at 80% by default
    val isActive: Boolean = true,
    val tags: List<String> = emptyList() // For more granular budgeting
) 