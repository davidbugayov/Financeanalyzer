package com.davidbugayov.financeanalyzer.analytics

import com.davidbugayov.financeanalyzer.shared.analytics.AndroidAnalyticsProvider

/**
 * Реальная реализация AndroidAnalyticsProvider
 * Использует существующий AnalyticsUtils
 */
class AndroidAnalyticsProviderImpl : AndroidAnalyticsProvider {
    override fun logScreenView(screenName: String, screenClass: String) {
        tryReportEvent(
            "screen_view",
            mapOf(
                "screen_name" to screenName,
                "screen_class" to screenClass,
            ),
        )
    }

    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        tryReportEvent(eventName, parameters)
    }

    override fun setUserProperty(key: String, value: String) {
        tryReportEvent("user_property", mapOf("name" to key, "value" to value))
    }

    override fun logAppOpen() {
        tryReportEvent("app_open", emptyMap())
    }

    override fun logAppForeground() {
        tryReportEvent("app_foreground", emptyMap())
    }

    override fun logAppBackground() {
        tryReportEvent("app_background", emptyMap())
    }

    private fun tryReportEvent(eventName: String, params: Map<String, Any>) {
        // Пытаемся отправить событие в AppMetrica через reflection (без compile-time зависимостей)
        try {
            val appMetricaClass = Class.forName("io.appmetrica.analytics.AppMetrica")
            val method = appMetricaClass.getMethod("reportEvent", String::class.java, MutableMap::class.java)
            // Преобразуем в изменяемую Map, как ожидает AppMetrica API
            val attributes = params.toMutableMap()
            method.invoke(null, eventName, attributes)
        } catch (_: Throwable) {
            // Ничего: библиотека может отсутствовать для некоторых флейворов
        }
    }
}
