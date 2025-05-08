package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode

/**
 * Менеджер для работы с SharedPreferences.
 * Отвечает за сохранение и загрузку пользовательских настроек и данных.
 */
class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    /**
     * Сохраняет тему приложения в SharedPreferences
     * @param themeMode Режим темы для сохранения
     */
    fun saveThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit { putString(KEY_THEME_MODE, themeMode.name) }
    }

    /**
     * Загружает тему приложения из SharedPreferences
     * @return Режим темы или SYSTEM, если ничего не сохранено
     */
    fun getThemeMode(): ThemeMode {
        val themeName = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    companion object {

        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TRANSACTION_REMINDER_ENABLED = "transaction_reminder_enabled"
    }


    /**
     * Возвращает, включены ли уведомления о транзакциях.
     * @return true, если уведомления о транзакциях включены
     */
    fun isTransactionReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_TRANSACTION_REMINDER_ENABLED, true)
    }

    /**
     * Получает boolean-значение из SharedPreferences
     * @param key Ключ, по которому хранится значение
     * @param defaultValue Значение по умолчанию
     * @return Сохраненное значение или defaultValue
     */
    fun getBooleanPreference(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Сохраняет boolean-значение в SharedPreferences
     * @param key Ключ, по которому будет храниться значение
     * @param value Значение для сохранения
     */
    fun setBooleanPreference(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    fun setTransactionReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_TRANSACTION_REMINDER_ENABLED, enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        sharedPreferences.edit {
            putInt("reminder_hour", hour)
            putInt("reminder_minute", minute)
        }
    }

    fun getReminderTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt("reminder_hour", 20)
        val minute = sharedPreferences.getInt("reminder_minute", 0)
        return Pair(hour, minute)
    }
} 