package com.davidbugayov.financeanalyzer.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AchievementsViewModel(
    repository: AchievementsRepository = AchievementsRepository()
) : ViewModel() {

    val achievements: StateFlow<List<Achievement>> = repository.achievements.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.achievements.value
    )
} 
