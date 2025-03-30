package com.davidbugayov.financeanalyzer.presentation.profile.event

import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode

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
     * Событие для поделиться экспортированным CSV файлом.
     */
    data object ShareExportedFile : ProfileEvent()
    
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
     * Событие изменения темы приложения.
     * @param theme Выбранный режим темы (светлая, темная или системная).
     */
    data class ChangeTheme(val theme: ThemeMode) : ProfileEvent()
    
    /**
     * Событие отображения диалога выбора темы.
     */
    object ShowThemeDialog : ProfileEvent()
    
    /**
     * Событие скрытия диалога выбора темы.
     */
    object HideThemeDialog : ProfileEvent()
    
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