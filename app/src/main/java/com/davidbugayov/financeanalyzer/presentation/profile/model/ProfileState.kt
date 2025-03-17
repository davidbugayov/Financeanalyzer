package com.davidbugayov.financeanalyzer.presentation.profile.model

import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal

/**
 * Состояние экрана профиля.
 * Следует принципам MVI (Model-View-Intent).
 */
data class ProfileState(
    // Общие состояния
    val isLoading: Boolean = false,
    
    // Информация о пользователе
    val userName: String = "Пользователь",
    val userEmail: String = "user@example.com",
    val userPhone: String? = null,
    val isEditingProfile: Boolean = false,
    
    // Состояния экспорта
    val exportSuccess: String? = null,
    val exportError: String? = null,
    val isExporting: Boolean = false,
    
    // Финансовые цели
    val financialGoals: List<FinancialGoal> = emptyList(),
    val activeGoals: List<FinancialGoal> = emptyList(),
    val completedGoals: List<FinancialGoal> = emptyList(),
    val selectedGoal: FinancialGoal? = null,
    val isAddingGoal: Boolean = false,
    val isEditingGoal: Boolean = false,
    val goalError: String? = null,
    
    // Настройки приложения
    val isDarkTheme: Boolean = false,
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
    
    // Безопасность
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false
) 