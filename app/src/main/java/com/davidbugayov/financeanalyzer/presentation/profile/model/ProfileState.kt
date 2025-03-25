package com.davidbugayov.financeanalyzer.presentation.profile.model

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
    
    // Настройки приложения
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isEditingTheme: Boolean = false,
    val selectedLanguage: String = "Русский",
    val selectedCurrency: String = "Рубль (₽)",
    val isNotificationsEnabled: Boolean = true,
    
    // Настройки уведомлений о транзакциях
    val isTransactionReminderEnabled: Boolean = false,
    val transactionReminderTime: Pair<Int, Int>? = null, // Часы и минуты
    val isEditingNotifications: Boolean = false,
    
    // Финансовая аналитика
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val savingsRate: Double = 0.0,
    val totalTransactions: Int = 0,
    val totalExpenseCategories: Int = 0,
    val totalIncomeCategories: Int = 0,
    val averageExpense: String = "0 ₽",
    val totalSourcesUsed: Int = 0,
    val dateRange: String = "Все время",
    
    // Безопасность
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false
) 