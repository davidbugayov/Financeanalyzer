package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * Интерфейс для Android-специфичной аналитики
 * Позволяет shared модулю работать с Android-специфичными реализациями
 */
interface AndroidAnalyticsProvider {
    fun logScreenView(screenName: String, screenClass: String)
    fun logEvent(eventName: String, parameters: Map<String, Any>)
    fun setUserProperty(key: String, value: String)
    fun logAppOpen()
    fun logAppForeground()
    fun logAppBackground()
}
