package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AchievementsViewModel : ViewModel() {

    private val initialAchievements = listOf(
        Achievement(
            id = "first_transaction",
            title = "Первые шаги",
            description = "Добавьте первую транзакцию",
            iconRes = 0,
            isUnlocked = false,
        ),
        Achievement(
            id = "week_no_coffee",
            title = "Неделя без кофе на вынос",
            description = "Не тратьте на кофе 7 дней подряд",
            iconRes = 0,
            isUnlocked = false,
        ),
    )

    private val _achievements = MutableStateFlow(initialAchievements)

    val achievements: StateFlow<List<Achievement>> = _achievements.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialAchievements,
    )
}
