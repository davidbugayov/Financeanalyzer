package com.davidbugayov.financeanalyzer.data.repository

import com.davidbugayov.financeanalyzer.data.local.dao.SavingGoalDao
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import com.davidbugayov.financeanalyzer.domain.model.GoalAlert
import com.davidbugayov.financeanalyzer.domain.repository.SavingGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.Calendar

class SavingGoalRepositoryImpl(
    private val savingGoalDao: SavingGoalDao
) : SavingGoalRepository {
    override fun getAllGoals(): Flow<List<SavingGoal>> =
        savingGoalDao.getAllGoals()

    override fun getActiveGoals(): Flow<List<SavingGoal>> =
        savingGoalDao.getActiveGoals()

    override suspend fun insertGoal(goal: SavingGoal): Long =
        savingGoalDao.insertGoal(goal)

    override suspend fun updateGoal(goal: SavingGoal) =
        savingGoalDao.updateGoal(goal)

    override suspend fun deleteGoal(goal: SavingGoal) =
        savingGoalDao.deleteGoal(goal)

    override fun getGoalById(goalId: Long): Flow<SavingGoal> =
        savingGoalDao.getGoalById(goalId)

    override fun getGoalProgress(goalId: Long): Flow<Double> =
        savingGoalDao.getGoalProgress(goalId)

    override suspend fun updateGoalProgress(goalId: Long, amount: Double) =
        savingGoalDao.updateGoalProgress(goalId, amount)

    override fun getGoalAlerts(): Flow<List<GoalAlert>> =
        savingGoalDao.getGoalAlerts()

    override suspend fun calculateProjectedCompletion(goalId: Long): Date? {
        val goal = savingGoalDao.getGoalById(goalId).first()
        val currentAmount = goal.currentAmount
        val targetAmount = goal.targetAmount
        val startDate = goal.startDate
        val daysActive = (Date().time - startDate.time) / (1000 * 60 * 60 * 24)
        
        if (daysActive <= 0 || currentAmount <= 0) return null
        
        val dailyRate = currentAmount / daysActive
        val remainingAmount = targetAmount - currentAmount
        val daysToComplete = (remainingAmount / dailyRate).toLong()
        
        return Calendar.getInstance().apply {
            time = Date()
            add(Calendar.DAY_OF_YEAR, daysToComplete.toInt())
        }.time
    }

    override fun getGoalsByDeadline(deadline: Date): Flow<List<SavingGoal>> =
        savingGoalDao.getGoalsByDeadline(deadline)
} 