package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.lifecycle.ViewModel
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// State для очереди достижений
data class AchievementsUiState(
    val queue: List<Achievement> = emptyList(),
    val current: Achievement? = null,
)

class AchievementsUiViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState

    fun onAchievementUnlocked(achievement: Achievement) {
        _uiState.update { state ->
            val newQueue = state.queue + achievement
            state.copy(queue = newQueue, current = state.current ?: achievement)
        }
    }

    fun onAchievementShown() {
        _uiState.update { state ->
            val newQueue = state.queue.drop(1)
            state.copy(
                queue = newQueue,
                current = newQueue.firstOrNull(),
            )
        }
    }
}
