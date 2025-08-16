package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * iOS-специфичная реализация трекера аналитики
 * Пока что заглушка, так как iOS версия не реализована
 */
class IosAnalyticsTracker : AnalyticsTracker {
    
    override fun logScreenView(screenName: String, screenClass: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        // TODO: Реализовать для iOS
    }
    
    override fun setUserProperty(key: String, value: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logAppError(errorType: String, errorMessage: String, isFatal: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logException(exception: Throwable, isFatal: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logAppCrash(exception: Throwable) {
        // TODO: Реализовать для iOS
    }
    
    override fun logDatabaseOperation(operation: String, duration: Long, success: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logMemoryUsage(usageMB: Long, totalMB: Long, availableMB: Long) {
        // TODO: Реализовать для iOS
    }
    
    override fun logScreenLoad(screenName: String, durationMs: Long) {
        // TODO: Реализовать для iOS
    }
    
    override fun logAchievementUnlocked(
        achievementId: String,
        achievementTitle: String,
        achievementCategory: String,
        achievementRarity: String,
        rewardCoins: Int
    ) {
        // TODO: Реализовать для iOS
    }
    
    override fun logTransactionAdded(transactionType: String, amount: String, category: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logTransactionEdited(transactionType: String, amount: String, category: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logTransactionDeleted(transactionType: String, amount: String, category: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logCategoryDeleted(categoryName: String, isExpense: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logAchievementsScreenViewed() {
        // TODO: Реализовать для iOS
    }
    
    override fun logAchievementFilterChanged(filterType: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logSecurityAuthScreenViewed() {
        // TODO: Реализовать для iOS
    }
    
    override fun logSecurityAuthSuccess(authMethod: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logSecurityAuthFailed(authMethod: String, reason: String) {
        // TODO: Реализовать для iOS
    }
    
    override fun logSecurityAppLockChanged(enabled: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logSecurityBiometricChanged(enabled: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logSecurityPinSetup(isFirstSetup: Boolean) {
        // TODO: Реализовать для iOS
    }
    
    override fun logAppOpen() {
        // TODO: Реализовать для iOS
    }
    
    override fun logAppForeground() {
        // TODO: Реализовать для iOS
    }
    
    override fun logAppBackground() {
        // TODO: Реализовать для iOS
    }
}
