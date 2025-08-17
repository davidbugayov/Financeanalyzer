package com.davidbugayov.financeanalyzer.analytics

import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsTrackerFactory

/**
 * Утилиты для аналитики, использующие shared модуль
 */
object AnalyticsUtils {
    private val tracker = AnalyticsTrackerFactory.create()

    /**
     * Логирует просмотр экрана
     */
    fun logScreenView(
        screenName: String,
        screenClass: String,
    ) {
        tracker.logScreenView(screenName, screenClass)
    }

    /**
     * Логирует событие
     */
    fun logEvent(
        eventName: String,
        parameters: android.os.Bundle,
    ) {
        val params =
            parameters.keySet().associateWith { key ->
                parameters.get(key)
            }.filterValues { it != null }.mapValues { it.value!! }
        tracker.logEvent(eventName, params)
    }

    /**
     * Устанавливает пользовательское свойство
     */
    fun setUserProperty(
        key: String,
        value: String,
    ) {
        tracker.setUserProperty(key, value)
    }

    /**
     * Логирует ошибку приложения
     */
    fun logAppError(
        errorType: String,
        errorMessage: String,
        isFatal: Boolean = false,
    ) {
        tracker.logAppError(errorType, errorMessage, isFatal)
    }

    /**
     * Логирует исключение
     */
    fun logException(
        exception: Throwable,
        isFatal: Boolean = false,
    ) {
        tracker.logException(exception, isFatal)
    }

    /**
     * Логирует краш приложения
     */
    fun logAppCrash(exception: Throwable) {
        tracker.logAppCrash(exception)
    }

    /**
     * Логирует операцию с базой данных
     */
    fun logDatabaseOperation(
        operation: String,
        duration: Long,
        success: Boolean,
    ) {
        tracker.logDatabaseOperation(operation, duration, success)
    }

    /**
     * Логирует использование памяти
     */
    fun logMemoryUsage(
        usageMB: Long,
        totalMB: Long,
        availableMB: Long,
    ) {
        tracker.logMemoryUsage(usageMB, totalMB, availableMB)
    }

    /**
     * Логирует время загрузки экрана
     */
    fun logScreenLoad(
        screenName: String,
        durationMs: Long,
    ) {
        tracker.logScreenLoad(screenName, durationMs)
    }

    /**
     * Логирует разблокировку достижения
     */
    fun logAchievementUnlocked(
        achievementId: String,
        achievementTitle: String,
        achievementCategory: String,
        achievementRarity: String,
        rewardCoins: Int,
    ) {
        tracker.logAchievementUnlocked(
            achievementId,
            achievementTitle,
            achievementCategory,
            achievementRarity,
            rewardCoins,
        )
    }

    /**
     * Логирует добавление транзакции
     */
    fun logTransactionAdded(
        transactionType: String,
        amount: String,
        category: String,
    ) {
        tracker.logTransactionAdded(transactionType, amount, category)
    }

    /**
     * Логирует редактирование транзакции
     */
    fun logTransactionEdited(
        transactionType: String,
        amount: String,
        category: String,
    ) {
        tracker.logTransactionEdited(transactionType, amount, category)
    }

    /**
     * Логирует удаление транзакции
     */
    fun logTransactionDeleted(
        transactionType: String,
        amount: String,
        category: String,
    ) {
        tracker.logTransactionDeleted(transactionType, amount, category)
    }

    /**
     * Логирует удаление категории
     */
    fun logCategoryDeleted(
        categoryName: String,
        isExpense: Boolean,
    ) {
        tracker.logCategoryDeleted(categoryName, isExpense)
    }

    /**
     * Логирует просмотр экрана достижений
     */
    fun logAchievementsScreenViewed() {
        tracker.logAchievementsScreenViewed()
    }

    /**
     * Логирует изменение фильтра достижений
     */
    fun logAchievementFilterChanged(filterType: String) {
        tracker.logAchievementFilterChanged(filterType)
    }

    /**
     * Логирует просмотр экрана безопасности
     */
    fun logSecurityAuthScreenViewed() {
        tracker.logSecurityAuthScreenViewed()
    }

    /**
     * Логирует успешную аутентификацию
     */
    fun logSecurityAuthSuccess(authMethod: String) {
        tracker.logSecurityAuthSuccess(authMethod)
    }

    /**
     * Логирует неудачную аутентификацию
     */
    fun logSecurityAuthFailed(
        authMethod: String,
        reason: String,
    ) {
        tracker.logSecurityAuthFailed(authMethod, reason)
    }

    /**
     * Логирует изменение блокировки приложения
     */
    fun logSecurityAppLockChanged(enabled: Boolean) {
        tracker.logSecurityAppLockChanged(enabled)
    }

    /**
     * Логирует изменение биометрической аутентификации
     */
    fun logSecurityBiometricChanged(enabled: Boolean) {
        tracker.logSecurityBiometricChanged(enabled)
    }

    /**
     * Логирует настройку PIN-кода
     */
    fun logSecurityPinSetup(isFirstSetup: Boolean) {
        tracker.logSecurityPinSetup(isFirstSetup)
    }

    /**
     * Логирует открытие приложения
     */
    fun logAppOpen() {
        tracker.logAppOpen()
    }

    /**
     * Логирует переход приложения на передний план
     */
    fun logAppForeground() {
        tracker.logAppForeground()
    }

    /**
     * Логирует переход приложения в фон
     */
    fun logAppBackground() {
        tracker.logAppBackground()
    }

    // Устаревшие методы для обратной совместимости
    @Deprecated("Use logEvent with Map<String, Any> instead", ReplaceWith("logEvent(eventName, parameters.toMap())"))
    fun logEvent(eventName: String) {
        tracker.logEvent(eventName, emptyMap())
    }

    @Deprecated("Use logScreenView instead", ReplaceWith("logScreenView(screenName, screenClass)"))
    fun logScreenView(
        screenName: String,
        screenClass: String,
        additionalParams: Map<String, Any> = emptyMap(),
    ) {
        tracker.logScreenView(screenName, screenClass)
        if (additionalParams.isNotEmpty()) {
            tracker.logEvent("screen_view_additional", additionalParams)
        }
    }
}
