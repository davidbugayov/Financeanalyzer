package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * Фабрика для создания трекера аналитики
 */
object AnalyticsTrackerFactory {
    
    /**
     * Создает экземпляр трекера аналитики для текущей платформы
     */
    fun create(): AnalyticsTracker {
        val provider = AnalyticsProviderBridge.getProvider()
        return if (provider != null) ProviderBackedAnalyticsTracker(provider) else NoOpAnalyticsTracker()
    }
}

/**
 * Заглушка для аналитики
 */
class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logScreenView(screenName: String, screenClass: String) {}
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {}
    override fun setUserProperty(key: String, value: String) {}
    override fun logAppError(errorType: String, errorMessage: String, isFatal: Boolean) {}
    override fun logException(exception: Throwable, isFatal: Boolean) {}
    override fun logAppCrash(exception: Throwable) {}
    override fun logDatabaseOperation(operation: String, duration: Long, success: Boolean) {}
    override fun logMemoryUsage(usageMB: Long, totalMB: Long, availableMB: Long) {}
    override fun logScreenLoad(screenName: String, durationMs: Long) {}
    override fun logAchievementUnlocked(achievementId: String, achievementTitle: String, achievementCategory: String, achievementRarity: String, rewardCoins: Int) {}
    override fun logTransactionAdded(transactionType: String, amount: String, category: String) {}
    override fun logTransactionEdited(transactionType: String, amount: String, category: String) {}
    override fun logTransactionDeleted(transactionType: String, amount: String, category: String) {}
    override fun logCategoryDeleted(categoryName: String, isExpense: Boolean) {}
    override fun logAchievementsScreenViewed() {}
    override fun logAchievementFilterChanged(filterType: String) {}
    override fun logSecurityAuthScreenViewed() {}
    override fun logSecurityAuthSuccess(authMethod: String) {}
    override fun logSecurityAuthFailed(authMethod: String, reason: String) {}
    override fun logSecurityAppLockChanged(enabled: Boolean) {}
    override fun logSecurityBiometricChanged(enabled: Boolean) {}
    override fun logSecurityPinSetup(isFirstSetup: Boolean) {}
    override fun logAppOpen() {}
    override fun logAppForeground() {}
    override fun logAppBackground() {}
}
