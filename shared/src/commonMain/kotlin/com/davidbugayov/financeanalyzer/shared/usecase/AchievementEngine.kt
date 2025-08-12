package com.davidbugayov.financeanalyzer.shared.usecase

import com.davidbugayov.financeanalyzer.shared.model.Achievement
import com.davidbugayov.financeanalyzer.shared.repository.AchievementsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AchievementEngine(
    private val achievementsRepository: AchievementsRepository,
    private val scope: CoroutineScope,
) {
    private val _newAchievements = MutableSharedFlow<Achievement>()
    val newAchievements: SharedFlow<Achievement> = _newAchievements.asSharedFlow()

    private var transactionCount = 0

    init {
        scope.launch { achievementsRepository.getAllAchievements().first() }
    }

    fun onTransactionAdded() {
        scope.launch {
            transactionCount++
            updateAchievementProgress("first_transaction", 1)
            updateAchievementProgress("transaction_master", transactionCount)
        }
    }

    private suspend fun updateAchievementProgress(achievementId: String, newProgress: Int) {
        val achievement = achievementsRepository.getAchievementById(achievementId).first() ?: return
        if (!achievement.isUnlocked) {
            val updatedProgress = minOf(newProgress, achievement.targetProgress)
            val shouldUnlock = updatedProgress >= achievement.targetProgress
            val updated = achievement.copy(
                currentProgress = updatedProgress,
                isUnlocked = shouldUnlock,
                dateUnlocked = if (shouldUnlock) (achievement.dateUnlocked ?: 0L) else achievement.dateUnlocked,
            )
            achievementsRepository.updateAchievement(updated)
            if (shouldUnlock && !achievement.isUnlocked) _newAchievements.emit(updated)
        }
    }
}


