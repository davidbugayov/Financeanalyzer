package com.davidbugayov.financeanalyzer.feature.profile.util

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.profile.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Утилитный класс для получения строковых ресурсов в модуле profile
 */
object StringProvider {
    
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int): String {
        return context?.getString(resId) ?: "String not found"
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int, vararg formatArgs: Any): String {
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
    val settingsCurrencyCurrentValue: String get() = getString(R.string.settings_currency_current_value)
    
    // Уведомления
    val profileTransactionRemindersTitle: String get() = getString(R.string.profile_transaction_reminders_title)
    val notificationDisabledDescription: String get() = getString(R.string.notification_disabled_description)
    val off: String get() = getString(R.string.off)
    val selectReminderTime: String get() = getString(R.string.select_reminder_time)
    val permissionRequiredTitle: String get() = getString(R.string.permission_required_title)
    val notificationPermissionRequired: String get() = getString(R.string.notification_permission_required)
    val requestPermission: String get() = getString(R.string.request_permission)
    val enableReminders: String get() = getString(R.string.enable_reminders)
    val reminderDescription: String get() = getString(R.string.reminder_description)
    val reminderTime: String get() = getString(R.string.reminder_time)
    val editTime: String get() = getString(R.string.edit_time)
    
    // Аналитика
    val analyticsTitle: String get() = getString(R.string.analytics_title)
    val income: String get() = getString(R.string.income)
    val expenses: String get() = getString(R.string.expenses)
    val balance: String get() = getString(R.string.balance)
    val savingsRate: String get() = getString(R.string.savings_rate)
    val expenseCategories: String get() = getString(R.string.expense_categories)
    val incomeCategories: String get() = getString(R.string.income_categories)
    val averageExpense: String get() = getString(R.string.average_expense)
    val sourcesUsed: String get() = getString(R.string.sources_used)
    val allTime: String get() = getString(R.string.all_time)
    val unknown: String get() = getString(R.string.unknown)
    
    // Информация о приложении
    val appInfo: String get() = getString(R.string.app_info)
    val appVersion: String get() = getString(R.string.app_version)
    val buildVersion: String get() = getString(R.string.build_version)
    
    // Общие
    val close: String get() = getString(UiR.string.close)
    val cancel: String get() = getString(UiR.string.cancel)
    val ok: String get() = getString(R.string.ok)
    val done: String get() = getString(R.string.done)
    val cdDone: String get() = getString(R.string.cd_done)
    val hours: String get() = getString(R.string.hours)
    val minutes: String get() = getString(R.string.minutes)
    
    // Настройки уведомлений
    val notificationSettings: String get() = getString(R.string.notification_settings)
    
    // Тема
    val profileThemeSelect: String get() = getString(R.string.profile_theme_select)
    val profileThemeLight: String get() = getString(R.string.profile_theme_light)
    val profileThemeDark: String get() = getString(R.string.profile_theme_dark)
    val profileThemeSystem: String get() = getString(R.string.profile_theme_system)
    
    // Методы с параметрами
    fun settingsReminderTimeFormat(hour: Int, minute: Int): String = getString(R.string.settings_reminder_time_format, hour, minute)
    fun reminderTimeDescription(time: String): String = getString(R.string.reminder_time_description, time)
    fun licenseColon(license: String): String = getString(R.string.license_colon, license)
} 