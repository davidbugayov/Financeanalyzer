package com.davidbugayov.financeanalyzer.presentation.profile.event

import com.davidbugayov.financeanalyzer.domain.model.FinancialGoal

/**
 * События экрана профиля.
 * Следует принципам MVI (Model-View-Intent).
 */
sealed class ProfileEvent {
    /**
     * Событие экспорта транзакций в CSV.
     */
    data object ExportTransactionsToCSV : ProfileEvent()
    
    /**
     * Событие сброса состояния экспорта.
     * Используется для очистки сообщений об успехе или ошибке.
     */
    data object ResetExportState : ProfileEvent()
    
    /**
     * Событие установки ошибки экспорта.
     * @param message Сообщение об ошибке.
     */
    data class SetExportError(val message: String) : ProfileEvent()
    
    /**
     * Событие загрузки финансовых целей.
     */
    data object LoadFinancialGoals : ProfileEvent()
    
    /**
     * Событие выбора финансовой цели.
     * @param goalId Идентификатор выбранной цели.
     */
    data class SelectGoal(val goalId: String) : ProfileEvent()
    
    /**
     * Событие открытия диалога добавления финансовой цели.
     */
    data object ShowAddGoalDialog : ProfileEvent()
    
    /**
     * Событие открытия диалога редактирования финансовой цели.
     * @param goalId Идентификатор цели для редактирования.
     */
    data class ShowEditGoalDialog(val goalId: String) : ProfileEvent()
    
    /**
     * Событие закрытия диалога финансовой цели.
     */
    data object HideGoalDialog : ProfileEvent()
    
    /**
     * Событие добавления финансовой цели.
     * @param goal Финансовая цель для добавления.
     */
    data class AddGoal(val goal: FinancialGoal) : ProfileEvent()
    
    /**
     * Событие обновления финансовой цели.
     * @param goal Финансовая цель для обновления.
     */
    data class UpdateGoal(val goal: FinancialGoal) : ProfileEvent()
    
    /**
     * Событие удаления финансовой цели.
     * @param goalId Идентификатор цели для удаления.
     */
    data class DeleteGoal(val goalId: String) : ProfileEvent()
    
    /**
     * Событие добавления суммы к финансовой цели.
     * @param goalId Идентификатор цели.
     * @param amount Сумма для добавления.
     */
    data class AddAmountToGoal(val goalId: String, val amount: Double) : ProfileEvent()
    
    /**
     * Событие открытия редактирования профиля.
     */
    data object ShowEditProfileDialog : ProfileEvent()
    
    /**
     * Событие закрытия редактирования профиля.
     */
    data object HideEditProfileDialog : ProfileEvent()
    
    /**
     * Событие обновления информации о пользователе.
     * @param name Имя пользователя.
     * @param email Email пользователя.
     * @param phone Телефон пользователя.
     */
    data class UpdateUserInfo(val name: String, val email: String, val phone: String) : ProfileEvent()
    
    /**
     * Событие изменения темы приложения.
     * @param isDark Использовать темную тему.
     */
    data class ChangeTheme(val isDarkTheme: Boolean) : ProfileEvent()
    
    /**
     * Событие изменения языка приложения.
     * @param language Выбранный язык.
     */
    data class ChangeLanguage(val language: String) : ProfileEvent()
    
    /**
     * Событие изменения валюты по умолчанию.
     * @param currency Выбранная валюта.
     */
    data class ChangeCurrency(val currency: String) : ProfileEvent()
    
    /**
     * Событие изменения настроек уведомлений.
     * @param enabled Включены ли уведомления.
     */
    data class ChangeNotifications(val enabled: Boolean) : ProfileEvent()
    
    /**
     * Событие открытия диалога настройки уведомлений о транзакциях.
     */
    data object ShowNotificationSettingsDialog : ProfileEvent()
    
    /**
     * Событие закрытия диалога настройки уведомлений о транзакциях.
     */
    data object HideNotificationSettingsDialog : ProfileEvent()
    
    /**
     * Событие обновления настроек уведомлений о транзакциях.
     * @param isEnabled Включены ли уведомления.
     * @param reminderTime Время напоминания (часы и минуты) или null, если уведомления отключены.
     */
    data class UpdateTransactionReminder(val isEnabled: Boolean, val reminderTime: Pair<Int, Int>?) : ProfileEvent()
    
    /**
     * Событие изменения блокировки приложения.
     * @param enabled Включена ли блокировка.
     */
    data class ChangeAppLock(val enabled: Boolean) : ProfileEvent()
    
    /**
     * Событие изменения биометрической аутентификации.
     * @param enabled Включена ли биометрическая аутентификация.
     */
    data class ChangeBiometric(val enabled: Boolean) : ProfileEvent()
    
    /**
     * Событие загрузки финансовой аналитики.
     */
    data object LoadFinancialAnalytics : ProfileEvent()
    
    /**
     * Событие навигации к экрану библиотек.
     */
    data object NavigateToLibraries : ProfileEvent()
} 