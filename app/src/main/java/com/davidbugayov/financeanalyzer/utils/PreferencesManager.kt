package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    companion object {

        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TOTAL_INCOME = "total_income"
        private const val KEY_TOTAL_EXPENSE = "total_expense"
        private const val KEY_BALANCE = "balance"
        private const val KEY_STATS_LAST_UPDATE = "stats_last_update"
        private const val KEY_TRANSACTION_REMINDER_ENABLED = "transaction_reminder_enabled"
        private const val KEY_APP_INSTALLATION_TIME = "app_installation_time"
        private const val KEY_FIRST_LAUNCH_DONE = "first_launch_done"
        private const val KEY_IMPORT_INFO_DISMISSED = "import_info_dismissed"
    }

    /**
     * Сохраняет финансовую статистику в SharedPreferences
     * @param totalIncome Общий доход
     * @param totalExpense Общий расход
     * @param balance Текущий баланс
     */
    fun saveFinancialStats(totalIncome: Money, totalExpense: Money, balance: Money) {
        sharedPreferences.edit {
            putString(KEY_TOTAL_INCOME, totalIncome.amount.toString())
            putString(KEY_TOTAL_EXPENSE, totalExpense.amount.toString())
            putString(KEY_BALANCE, balance.amount.toString())
            putLong(KEY_STATS_LAST_UPDATE, System.currentTimeMillis())
        }
    }

    /**
     * Загружает финансовую статистику из SharedPreferences
     * @return Triple с общим доходом, общим расходом и балансом
     */
    fun getFinancialStats(): Triple<Money, Money, Money> {
        val totalIncome = Money(sharedPreferences.getString(KEY_TOTAL_INCOME, "0.0") ?: "0.0")
        val totalExpense = Money(sharedPreferences.getString(KEY_TOTAL_EXPENSE, "0.0") ?: "0.0")
        val balance = Money(sharedPreferences.getString(KEY_BALANCE, "0.0") ?: "0.0")
        
        return Triple(totalIncome, totalExpense, balance)
    }

    /**
     * Проверяет актуальность сохраненной статистики
     * @return true, если статистика актуальна (обновлена недавно)
     */
    fun isStatsUpToDate(): Boolean {
        val lastUpdate = sharedPreferences.getLong(KEY_STATS_LAST_UPDATE, 0)
        // Считаем статистику актуальной, если она обновлена не более 24 часов назад
        return System.currentTimeMillis() - lastUpdate < 24 * 60 * 60 * 1000
    }

    /**
     * Возвращает, включены ли уведомления о транзакциях.
     * @return true, если уведомления о транзакциях включены
     */
    fun isTransactionReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_TRANSACTION_REMINDER_ENABLED, true)
    }
    
    /**
     * Отмечает время первой установки приложения
     */
    fun markInstallationTime() {
        if (!sharedPreferences.contains(KEY_APP_INSTALLATION_TIME)) {
            sharedPreferences.edit {
                putLong(KEY_APP_INSTALLATION_TIME, System.currentTimeMillis())
            }
        }
    }
    
    /**
     * Отмечает, что первый запуск приложения завершен
     */
    fun markFirstLaunchDone() {
        sharedPreferences.edit {
            putBoolean(KEY_FIRST_LAUNCH_DONE, true)
        }
    }
    
    /**
     * Проверяет, прошло ли достаточно времени после установки для показа уведомлений
     * Возвращает true, если:
     * 1. Прошло более 24 часов с момента установки, ИЛИ
     * 2. Первый запуск уже был завершен (не установка, а обновление)
     */
    fun shouldShowNotifications(): Boolean {
        val installTime = sharedPreferences.getLong(KEY_APP_INSTALLATION_TIME, 0)
        val firstLaunchDone = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH_DONE, false)
        
        // Если первый запуск уже был завершен, то можно показывать уведомления
        if (firstLaunchDone) return true
        
        // Если прошло более 24 часов с момента установки, можно показывать уведомления
        val timeSinceInstall = System.currentTimeMillis() - installTime
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        
        // Если прошло более 24 часов, отмечаем, что первый запуск завершен
        if (timeSinceInstall > oneDayInMillis) {
            markFirstLaunchDone()
            return true
        }
        
        return false
    }

    fun getImportInfoDismissed(): Boolean {
        return sharedPreferences.getBoolean(KEY_IMPORT_INFO_DISMISSED, false)
    }

    fun setImportInfoDismissed(value: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_IMPORT_INFO_DISMISSED, value) }
    }
} 