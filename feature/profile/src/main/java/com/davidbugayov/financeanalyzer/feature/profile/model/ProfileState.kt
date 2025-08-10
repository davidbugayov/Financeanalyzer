package com.davidbugayov.financeanalyzer.feature.profile.model

import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.ThemeMode
import com.davidbugayov.financeanalyzer.utils.Time
import org.koin.core.context.GlobalContext

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
    // Финансовая аналитика
    val totalIncome: Money = Money.zero(),
    val totalExpense: Money = Money.zero(),
    val balance: Money = Money.zero(),
    val savingsRate: Double = 0.0,
    val totalTransactions: Int = 0,
    val totalExpenseCategories: Int = 0,
    val totalIncomeCategories: Int = 0,
    val averageExpense: String = "0 ₽",
    val totalSourcesUsed: Int = 0,
    val dateRange: String = GlobalContext.get().get<ResourceProvider>().getString(UiR.string.period_all_time),
    // Безопасность
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isEditingPinCode: Boolean = false,
    val hasNotificationPermission: Boolean = true,
)
