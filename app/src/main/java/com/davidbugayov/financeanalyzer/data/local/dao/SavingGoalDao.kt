package com.davidbugayov.financeanalyzer.data.local.dao

import androidx.room.*
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import com.davidbugayov.financeanalyzer.domain.model.GoalAlert
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goals")
    fun getAllGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE isActive = 1")
    fun getActiveGoals(): Flow<List<SavingGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingGoal)

    @Query("SELECT * FROM saving_goals WHERE id = :goalId")
    fun getGoalById(goalId: Long): Flow<SavingGoal>

    @Query("SELECT (currentAmount / targetAmount) * 100 FROM saving_goals WHERE id = :goalId")
    fun getGoalProgress(goalId: Long): Flow<Double>

    @Query("UPDATE saving_goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Long, amount: Double)

    @Query("""
        SELECT ga.* FROM goal_alerts ga
        INNER JOIN saving_goals sg ON ga.goalId = sg.id
        WHERE sg.isActive = 1
        ORDER BY ga.date DESC
    """)
    fun getGoalAlerts(): Flow<List<GoalAlert>>

    @Query("SELECT * FROM saving_goals WHERE deadline <= :deadline AND isActive = 1")
    fun getGoalsByDeadline(deadline: Date): Flow<List<SavingGoal>>
} 