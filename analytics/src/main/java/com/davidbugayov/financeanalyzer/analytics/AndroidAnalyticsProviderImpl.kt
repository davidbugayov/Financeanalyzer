package com.davidbugayov.financeanalyzer.analytics

import com.davidbugayov.financeanalyzer.shared.analytics.AndroidAnalyticsProvider

/**
 * Реальная реализация AndroidAnalyticsProvider
 * Использует существующий AnalyticsUtils
 */
class AndroidAnalyticsProviderImpl : AndroidAnalyticsProvider {
    
    override fun logScreenView(screenName: String, screenClass: String) {
        AnalyticsUtils.logScreenView(screenName, screenClass)
    }
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        val bundle = android.os.Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                }
            }
        }
        AnalyticsUtils.logEvent(eventName, bundle)
    }
    
    override fun setUserProperty(key: String, value: String) {
        AnalyticsUtils.setUserProperty(key, value)
    }
    
    override fun logAppOpen() {
        AnalyticsUtils.logAppOpen()
    }
    
    override fun logAppForeground() {
        AnalyticsUtils.logAppForeground()
    }
    
    override fun logAppBackground() {
        AnalyticsUtils.logAppBackground()
    }
}
