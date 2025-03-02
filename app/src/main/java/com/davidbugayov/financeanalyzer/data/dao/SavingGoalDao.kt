package com.davidbugayov.financeanalyzer.data.dao

import androidx.room.*
import com.davidbugayov.financeanalyzer.domain.model.GoalAlert
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SavingGoalDao {
    @Insert
    suspend fun insert(goal: SavingGoal): Long

    @Insert
    suspend fun insertAlert(alert: GoalAlert)

    @Update
    suspend fun update(goal: SavingGoal)

    @Delete
    suspend fun delete(goal: SavingGoal)

    @Query("SELECT * FROM saving_goals ORDER BY deadline")
    fun getAllGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE id = :goalId")
    fun getGoalById(goalId: Int): Flow<SavingGoal?>

    @Transaction
    @Query("""
        SELECT 
            g.id, g.title, g.targetAmount, g.deadline, g.category, g.description,
            g.priority, g.isActive, g.createdAt, g.updatedAt, g.currency,
            g.notificationThresholds,
            COALESCE(SUM(CASE WHEN t.isExpense = 0 THEN t.amount ELSE -t.amount END), 0) as currentAmount
        FROM saving_goals g
        LEFT JOIN transactions t ON t.category = g.category AND t.date <= :currentDate
        WHERE g.id = :goalId
        GROUP BY g.id
    """)
    fun getGoalProgress(goalId: Int, currentDate: Date = Date()): Flow<SavingGoalWithProgress>

    @Query("SELECT * FROM goal_alerts WHERE goalId = :goalId AND isRead = 0 ORDER BY date DESC")
    fun getGoalAlerts(goalId: Int): Flow<List<GoalAlert>>

    @Query("UPDATE goal_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Int)

    @Query("DELETE FROM goal_alerts WHERE goalId = :goalId")
    suspend fun deleteAlertsForGoal(goalId: Int)
}

data class SavingGoalWithProgress(
    @Embedded
    val goal: SavingGoal,
    @ColumnInfo(name = "currentAmount")
    var currentAmount: Double = 0.0
) 