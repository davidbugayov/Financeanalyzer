package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Класс для управления состоянием онбординга.
 * Отвечает за хранение информации о том, был ли показан онбординг пользователю.
 */
class OnboardingManager(
    context: Context,
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )

    /**
     * Проверяет, был ли уже показан онбординг пользователю.
     * @return true, если онбординг уже был показан
     */
    fun isOnboardingCompleted(): Boolean = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    /**
     * Отмечает онбординг как завершенный.
     */
    fun setOnboardingCompleted() {
        sharedPreferences.edit {
            putBoolean(KEY_ONBOARDING_COMPLETED, true)
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "onboarding_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
