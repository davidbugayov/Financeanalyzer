package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * Мост для регистрации Android-реализации аналитики из Android слоя в общий KMP код.
 */
object AnalyticsProviderBridge {
    private var androidProvider: AndroidAnalyticsProvider? = null

    /**
     * Регистрирует Android-провайдера аналитики.
     *
     * @param provider Реализация интерфейса AndroidAnalyticsProvider
     */
    fun setProvider(provider: AndroidAnalyticsProvider) {
        androidProvider = provider
    }

    /**
     * Возвращает зарегистрированного провайдера аналитики или null, если он не установлен.
     */
    fun getProvider(): AndroidAnalyticsProvider? = androidProvider
}

/**
 * Реализация AnalyticsTracker, использующая зарегистрированный AndroidAnalyticsProvider.
 */
internal class ProviderBackedAnalyticsTracker(
    private val provider: AndroidAnalyticsProvider,
) : AnalyticsTracker {
    override fun logScreenView(screenName: String, screenClass: String) {
        provider.logScreenView(screenName, screenClass)
    }

    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        provider.logEvent(eventName, parameters)
    }

    override fun setUserProperty(key: String, value: String) {
        provider.setUserProperty(key, value)
    }

    override fun logAppError(errorType: String, errorMessage: String, isFatal: Boolean) {
        provider.logEvent(
            eventName = "app_error",
            parameters = mapOf(
                "type" to errorType,
                "message" to errorMessage,
                "fatal" to isFatal,
            ),
        )
    }

    override fun logException(exception: Throwable, isFatal: Boolean) {
        provider.logEvent(
            eventName = "exception",
            parameters = mapOf(
                "type" to exception::class.simpleName.orEmpty(),
                "message" to (exception.message ?: ""),
                "fatal" to isFatal,
            ),
        )
    }

    override fun logAppCrash(exception: Throwable) {
        provider.logEvent(
            eventName = "app_crash",
            parameters = mapOf(
                "type" to exception::class.simpleName.orEmpty(),
                "message" to (exception.message ?: ""),
            ),
        )
    }

    override fun logDatabaseOperation(operation: String, duration: Long, success: Boolean) {
        provider.logEvent(
            eventName = "db_op",
            parameters = mapOf(
                "operation" to operation,
                "duration_ms" to duration,
                "success" to success,
            ),
        )
    }

    override fun logMemoryUsage(usageMB: Long, totalMB: Long, availableMB: Long) {
        provider.logEvent(
            eventName = "mem_usage",
            parameters = mapOf(
                "usage_mb" to usageMB,
                "total_mb" to totalMB,
                "avail_mb" to availableMB,
            ),
        )
    }

    override fun logScreenLoad(screenName: String, durationMs: Long) {
        provider.logEvent(
            eventName = "screen_load",
            parameters = mapOf(
                "screen" to screenName,
                "duration_ms" to durationMs,
            ),
        )
    }

    override fun logAchievementUnlocked(
        achievementId: String,
        achievementTitle: String,
        achievementCategory: String,
        achievementRarity: String,
        rewardCoins: Int,
    ) {
        provider.logEvent(
            eventName = "achievement_unlocked",
            parameters = mapOf(
                "id" to achievementId,
                "title" to achievementTitle,
                "category" to achievementCategory,
                "rarity" to achievementRarity,
                "coins" to rewardCoins,
            ),
        )
    }

    override fun logTransactionAdded(transactionType: String, amount: String, category: String) {
        provider.logEvent("tx_added", mapOf("type" to transactionType, "amount" to amount, "category" to category))
    }

    override fun logTransactionEdited(transactionType: String, amount: String, category: String) {
        provider.logEvent("tx_edited", mapOf("type" to transactionType, "amount" to amount, "category" to category))
    }

    override fun logTransactionDeleted(transactionType: String, amount: String, category: String) {
        provider.logEvent("tx_deleted", mapOf("type" to transactionType, "amount" to amount, "category" to category))
    }

    override fun logCategoryDeleted(categoryName: String, isExpense: Boolean) {
        provider.logEvent("category_deleted", mapOf("name" to categoryName, "is_expense" to isExpense))
    }

    override fun logAchievementsScreenViewed() {
        provider.logEvent("achievements_screen", emptyMap())
    }

    override fun logAchievementFilterChanged(filterType: String) {
        provider.logEvent("achievements_filter", mapOf("filter" to filterType))
    }

    override fun logSecurityAuthScreenViewed() {
        provider.logEvent("security_auth_screen", emptyMap())
    }

    override fun logSecurityAuthSuccess(authMethod: String) {
        provider.logEvent("security_auth_success", mapOf("method" to authMethod))
    }

    override fun logSecurityAuthFailed(authMethod: String, reason: String) {
        provider.logEvent("security_auth_failed", mapOf("method" to authMethod, "reason" to reason))
    }

    override fun logSecurityAppLockChanged(enabled: Boolean) {
        provider.logEvent("security_app_lock_changed", mapOf("enabled" to enabled))
    }

    override fun logSecurityBiometricChanged(enabled: Boolean) {
        provider.logEvent("security_biometric_changed", mapOf("enabled" to enabled))
    }

    override fun logSecurityPinSetup(isFirstSetup: Boolean) {
        provider.logEvent("security_pin_setup", mapOf("first" to isFirstSetup))
    }

    override fun logAppOpen() {
        provider.logAppOpen()
    }

    override fun logAppForeground() {
        provider.logAppForeground()
    }

    override fun logAppBackground() {
        provider.logAppBackground()
    }
}


