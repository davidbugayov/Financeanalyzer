package com.davidbugayov.financeanalyzer.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.davidbugayov.financeanalyzer.utils.OnboardingManager
import com.davidbugayov.financeanalyzer.utils.PermissionManager

/**
 * ViewModel для экрана онбординга.
 * Управляет состоянием онбординга, проверяет был ли он показан ранее
 * и отмечает его как завершенный при необходимости.
 */
class OnboardingViewModel(
    private val onboardingManager: OnboardingManager,
    private val permissionManager: PermissionManager,
) : ViewModel() {
    /**
     * Проверяет, нужно ли показывать онбординг пользователю.
     * @return true, если онбординг уже был завершен ранее
     */
    fun isOnboardingCompleted(): Boolean = onboardingManager.isOnboardingCompleted()

    /**
     * Отмечает онбординг как завершенный.
     * Вызывается при завершении онбординга пользователем.
     */
    fun completeOnboarding() {
        onboardingManager.setOnboardingCompleted()
        // Отмечаем завершение онбординга в PermissionManager
        permissionManager.processEvent(PermissionManager.PermissionEvent.FINISH_ONBOARDING)
    }
}
