package com.davidbugayov.financeanalyzer.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.navigation.AppNavigation
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана онбординга.
 * Управляет состоянием онбординга и навигацией после его завершения.
 */
class OnboardingViewModel(
    private val onboardingManager: OnboardingManager,
    private val appNavigation: AppNavigation
) : ViewModel() {
    
    /**
     * Завершает онбординг и переходит на главный экран.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingManager.setOnboardingCompleted()
            appNavigation.navigateToHome()
        }
    }
    
    /**
     * Проверяет, завершен ли онбординг.
     * @return true, если онбординг уже пройден
     */
    fun isOnboardingCompleted(): Boolean {
        return onboardingManager.isOnboardingCompleted()
    }
    
    /**
     * Сбрасывает статус онбординга (для тестирования или повторного прохождения).
     */
    fun resetOnboarding() {
        onboardingManager.resetOnboarding()
    }
}
