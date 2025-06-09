package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Менеджер для управления статусом онбординга.
 * Сохраняет и проверяет, был ли пройден онбординг пользователем.
 */
class OnboardingManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Проверяет, был ли пройден онбординг.
     * @return true, если онбординг завершен, иначе false
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Устанавливает статус онбординга как завершенный.
     */
    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    /**
     * Сбрасывает статус онбординга (для тестирования или повторного прохождения).
     */
    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
