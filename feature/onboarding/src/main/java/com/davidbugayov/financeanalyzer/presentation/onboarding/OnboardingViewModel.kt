package com.davidbugayov.financeanalyzer.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.davidbugayov.financeanalyzer.utils.OnboardingManager

/**
 * ViewModel для экрана онбординга.
 * Управляет состоянием онбординга, проверяет был ли он показан ранее
 * и отмечает его как завершенный при необходимости.
 */
class OnboardingViewModel(
    private val onboardingManager: OnboardingManager,
) : ViewModel() {
    /**
     * Проверяет, нужно ли показывать онбординг пользователю.
     * @return true, если онбординг уже был завершен ранее
     */
    fun isOnboardingCompleted(): Boolean {
        return onboardingManager.isOnboardingCompleted()
    }

    /**
     * Отмечает онбординг как завершенный.
     * Вызывается при завершении онбординга пользователем.
     */
    fun completeOnboarding() {
        onboardingManager.setOnboardingCompleted()
    }
}
