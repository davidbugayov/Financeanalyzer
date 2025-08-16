package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * Общий интерфейс для трекинга аналитики в KMP
 */
interface AnalyticsTracker {
    /**
     * Логирует просмотр экрана
     */
    fun logScreenView(screenName: String, screenClass: String)
    
    /**
     * Логирует событие
     */
    fun logEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
    
    /**
     * Устанавливает пользовательское свойство
     */
    fun setUserProperty(key: String, value: String)
    
    /**
     * Логирует ошибку приложения
     */
    fun logAppError(errorType: String, errorMessage: String, isFatal: Boolean = false)
    
    /**
     * Логирует исключение
     */
    fun logException(exception: Throwable, isFatal: Boolean = false)
    
    /**
     * Логирует краш приложения
     */
    fun logAppCrash(exception: Throwable)
    
    /**
     * Логирует операцию с базой данных
     */
    fun logDatabaseOperation(operation: String, duration: Long, success: Boolean)
    
    /**
     * Логирует использование памяти
     */
    fun logMemoryUsage(usageMB: Long, totalMB: Long, availableMB: Long)
    
    /**
     * Логирует время загрузки экрана
     */
    fun logScreenLoad(screenName: String, durationMs: Long)
    
    /**
     * Логирует разблокировку достижения
     */
    fun logAchievementUnlocked(
        achievementId: String,
        achievementTitle: String,
        achievementCategory: String,
        achievementRarity: String,
        rewardCoins: Int
    )
    
    /**
     * Логирует добавление транзакции
     */
    fun logTransactionAdded(
        transactionType: String,
        amount: String,
        category: String
    )
    
    /**
     * Логирует редактирование транзакции
     */
    fun logTransactionEdited(
        transactionType: String,
        amount: String,
        category: String
    )
    
    /**
     * Логирует удаление транзакции
     */
    fun logTransactionDeleted(
        transactionType: String,
        amount: String,
        category: String
    )
    
    /**
     * Логирует удаление категории
     */
    fun logCategoryDeleted(categoryName: String, isExpense: Boolean)
    
    /**
     * Логирует просмотр экрана достижений
     */
    fun logAchievementsScreenViewed()
    
    /**
     * Логирует изменение фильтра достижений
     */
    fun logAchievementFilterChanged(filterType: String)
    
    /**
     * Логирует просмотр экрана безопасности
     */
    fun logSecurityAuthScreenViewed()
    
    /**
     * Логирует успешную аутентификацию
     */
    fun logSecurityAuthSuccess(authMethod: String)
    
    /**
     * Логирует неудачную аутентификацию
     */
    fun logSecurityAuthFailed(authMethod: String, reason: String)
    
    /**
     * Логирует изменение блокировки приложения
     */
    fun logSecurityAppLockChanged(enabled: Boolean)
    
    /**
     * Логирует изменение биометрической аутентификации
     */
    fun logSecurityBiometricChanged(enabled: Boolean)
    
    /**
     * Логирует настройку PIN-кода
     */
    fun logSecurityPinSetup(isFirstSetup: Boolean)
    
    /**
     * Логирует открытие приложения
     */
    fun logAppOpen()
    
    /**
     * Логирует переход приложения на передний план
     */
    fun logAppForeground()
    
    /**
     * Логирует переход приложения в фон
     */
    fun logAppBackground()
}
