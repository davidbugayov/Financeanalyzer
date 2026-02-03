package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана достижений
 *
 * @param achievementsRepository Репозиторий для управления достижениями
 */
class AchievementsViewModel(
    private val achievementsRepository: AchievementsRepository,
) : ViewModel() {
    // Используем репозиторий как единственный источник истины для достижений
    // Все достижения с ресурсными именами определены в AchievementsRepositoryImpl
    val achievements: StateFlow<List<Achievement>> =
        achievementsRepository.getAllAchievements().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )


    /**
     * Разблокирует случайное достижение для тестирования
     */
    fun unlockRandomAchievement() {
        viewModelScope.launch {
            val lockedAchievements = achievements.first().filter { !it.isUnlocked }
            if (lockedAchievements.isNotEmpty()) {
                val randomAchievement = lockedAchievements.random()
                achievementsRepository.unlockAchievement(randomAchievement.id)
            }
        }
    }
}
