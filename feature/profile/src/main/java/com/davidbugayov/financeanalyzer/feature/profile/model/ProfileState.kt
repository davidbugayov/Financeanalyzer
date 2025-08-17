package com.davidbugayov.financeanalyzer.feature.profile.model

import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.Time

/**
 * Состояние экрана профиля.
 * Следует принципам MVI (Model-View-Intent).
 */
data class ProfileState(
    // Общие состояния
    val isLoading: Boolean = false,
    val error: String? = null,
    // Состояния экспорта
    val exportSuccess: String? = null,
    val exportError: String? = null,
    val isExporting: Boolean = false,
    val exportedFilePath: String? = null,
    // Настройки приложения
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isEditingTheme: Boolean = false,
    val selectedLanguage: String = "Русский",
    val showLanguageDialog: Boolean = false,
    val selectedCurrency: Currency = Currency.RUB,
    val showCurrencyDialog: Boolean = false,
    // Настройки уведомлений о транзакциях
    val isTransactionReminderEnabled: Boolean = false,
    val transactionReminderTime: Time? = null,
    val isEditingNotifications: Boolean = false,
    // Безопасность
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isEditingPinCode: Boolean = false,
    val hasNotificationPermission: Boolean = true,
)
