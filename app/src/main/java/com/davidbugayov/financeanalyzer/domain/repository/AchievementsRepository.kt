package com.davidbugayov.financeanalyzer.domain.repository

import com.davidbugayov.financeanalyzer.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AchievementsRepository {

    // Пример достижений
    private val initialAchievements = listOf(
        Achievement(
            id = "first_transaction",
            title = "Первые шаги",
            description = "Добавьте первую транзакцию",
            iconRes = 0,
            isUnlocked = false
        ),
        Achievement(
            id = "week_no_coffee",
            title = "Неделя без кофе на вынос",
            description = "Не тратьте на кофе 7 дней подряд",
            iconRes = 0,
            isUnlocked = false
        )
    )

    private val _achievements = MutableStateFlow(initialAchievements)
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    fun unlockAchievement(id: String) {
        _achievements.value = _achievements.value.map {
            if (it.id == id && !it.isUnlocked) {
                it.copy(
                    isUnlocked = true,
                    dateUnlocked = System.currentTimeMillis()
                )
            } else {
                it
            }
        }
    }
} 
