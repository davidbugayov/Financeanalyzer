package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.davidbugayov.financeanalyzer.data.converter.Converters
import java.util.Date

@Entity(tableName = "saving_goals")
@TypeConverters(Converters::class)
data class SavingGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val deadline: Date,
    val category: String,
    val description: String? = null,
    val priority: Int = 0, // 0 = Low, 1 = Medium, 2 = High
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val currency: String = "RUB",
    val notificationThresholds: List<Int> = listOf(25, 50, 75, 90, 100) // Percentage thresholds for notifications
) 