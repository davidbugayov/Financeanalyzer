package com.davidbugayov.financeanalyzer.feature.profile.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.profile.R

/**
 * Утилитный класс для получения строковых ресурсов в модуле profile
 */
object StringProvider {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
    ): String {
        return context?.getString(resId) ?: "String not found"
    }

    fun getString(
        @androidx.annotation.StringRes resId: Int,
        vararg formatArgs: Any,
    ): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }

    // Основные строки профиля
    val profileTitle: String get() = getString(R.string.profile_title)
    val budget: String get() = getString(R.string.budget)
    val profileBudgetSubtitle: String get() = getString(R.string.profile_budget_subtitle)
    val exportImport: String get() = getString(R.string.export_import)
    val profileExportImportSubtitle: String get() = getString(R.string.profile_export_import_subtitle)
    val achievements: String get() = getString(R.string.achievements)
    val profileAchievementsSubtitle: String get() = getString(R.string.profile_achievements_subtitle)

    // Настройки
    val profileSettingsTitle: String get() = getString(R.string.profile_settings_title)
    val settingsThemeTitle: String get() = getString(R.string.settings_theme_title)
    val settingsThemeLight: String get() = getString(R.string.settings_theme_light)
    val settingsThemeDark: String get() = getString(R.string.settings_theme_dark)
    val settingsThemeSystem: String get() = getString(R.string.settings_theme_system)
    val settingsLanguageTitle: String get() = getString(R.string.settings_language_title)
    val settingsLanguageCurrentValue: String get() = getString(R.string.settings_language_current_value)
    val profileCurrencyTitle: String get() = getString(R.string.profile_currency_title)

    // Уведомления
    val profileTransactionRemindersTitle: String get() = getString(R.string.profile_transaction_reminders_title)
    val notificationDisabledDescription: String get() = getString(R.string.notification_disabled_description)
    val off: String get() = getString(R.string.off)

    val unknown: String get() = getString(R.string.unknown)

    val done: String get() = getString(R.string.done)


    // Методы с параметрами
    fun settingsReminderTimeFormat(
        hour: Int,
        minute: Int,
    ): String = getString(R.string.settings_reminder_time_format, hour, minute)

}
