package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * NoOp реализация AnalyticsTracker
 * Используется как заглушка, когда реальная аналитика не инициализирована
 */
class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logScreenView(screenName: String, screenClass: String) {
        // No-op
    }
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        // No-op
    }
    
    override fun setUserProperty(key: String, value: String) {
        // No-op
    }
    
    override fun logAppError(errorType: String, errorMessage: String, isFatal: Boolean) {
        // No-op
    }
    
    override fun logException(exception: Throwable, isFatal: Boolean) {
        // No-op
    }
    
    override fun logAppCrash(exception: Throwable) {
        // No-op
    }
    
    override fun logDatabaseOperation(operation: String, duration: Long, success: Boolean) {
        // No-op
    }
    
    override fun logMemoryUsage(usageMB: Long, totalMB: Long, availableMB: Long) {
        // No-op
    }
    
    override fun logScreenLoad(screenName: String, durationMs: Long) {
        // No-op
    }
    
    override fun logAchievementUnlocked(
        achievementId: String,
        achievementTitle: String,
        achievementCategory: String,
        achievementRarity: String,
        rewardCoins: Int
    ) {
        // No-op
    }
    
    override fun logTransactionAdded(transactionType: String, amount: String, category: String) {
        // No-op
    }
    
    override fun logTransactionEdited(transactionType: String, amount: String, category: String) {
        // No-op
    }
    
    override fun logTransactionDeleted(transactionType: String, amount: String, category: String) {
        // No-op
    }
    
    override fun logCategoryDeleted(categoryName: String, isExpense: Boolean) {
        // No-op
    }
    
    override fun logAchievementsScreenViewed() {
        // No-op
    }
    
    override fun logAchievementFilterChanged(filterType: String) {
        // No-op
    }
    
    override fun logSecurityAuthScreenViewed() {
        // No-op
    }
    
    override fun logSecurityAuthSuccess(authMethod: String) {
        // No-op
    }
    
    override fun logSecurityAuthFailed(authMethod: String, reason: String) {
        // No-op
    }
    
    override fun logSecurityAppLockChanged(enabled: Boolean) {
        // No-op
    }
    
    override fun logSecurityBiometricChanged(enabled: Boolean) {
        // No-op
    }
    
    override fun logSecurityPinSetup(isFirstSetup: Boolean) {
        // No-op
    }
    
    override fun logAppOpen() {
        // No-op
    }
    
    override fun logAppForeground() {
        // No-op
    }
    
    override fun logAppBackground() {
        // No-op
    }
}
