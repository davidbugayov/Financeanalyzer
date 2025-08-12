package com.davidbugayov.financeanalyzer.shared.repository

import com.davidbugayov.financeanalyzer.shared.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementsRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getAchievementById(id: String): Flow<Achievement?>
    suspend fun updateAchievement(achievement: Achievement)
}


