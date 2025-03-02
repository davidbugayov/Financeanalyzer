package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import com.davidbugayov.financeanalyzer.domain.model.GoalAlert
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface SavingGoalRepository {
    fun getAllGoals(): Flow<List<SavingGoal>>
    fun getActiveGoals(): Flow<List<SavingGoal>>
    suspend fun insertGoal(goal: SavingGoal): Long
    suspend fun updateGoal(goal: SavingGoal)
    suspend fun deleteGoal(goal: SavingGoal)
    fun getGoalById(goalId: Long): Flow<SavingGoal>
    fun getGoalProgress(goalId: Long): Flow<Double> // Returns percentage
    suspend fun updateGoalProgress(goalId: Long, amount: Double)
    fun getGoalAlerts(): Flow<List<GoalAlert>>
    suspend fun calculateProjectedCompletion(goalId: Long): Date? // Returns estimated completion date
    fun getGoalsByDeadline(deadline: Date): Flow<List<SavingGoal>>
} 