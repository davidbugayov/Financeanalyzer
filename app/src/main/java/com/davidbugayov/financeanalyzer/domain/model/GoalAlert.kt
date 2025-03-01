package com.davidbugayov.financeanalyzer.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "goal_alerts",
    foreignKeys = [
        ForeignKey(
            entity = SavingGoal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class GoalAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val goalId: Int,
    val message: String,
    val date: Date,
    val isRead: Boolean = false,
    val type: GoalAlertType = GoalAlertType.PROGRESS
)

enum class GoalAlertType {
    PROGRESS,
    MILESTONE,
    DEADLINE_APPROACHING,
    GOAL_ACHIEVED
} 