package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import io.appmetrica.analytics.AppMetrica
import timber.log.Timber

/**
 * Адаптер для Яндекс.AppMetrica, реализующий интерфейс IAnalytics.
 * Используется во всех флейворах.
 */
class AppMetricaAnalyticsAdapter : IAnalytics {
    
    override fun logEvent(eventName: String) {
        Timber.d("Logging AppMetrica event: $eventName")
        AppMetrica.reportEvent(eventName)
    }

    override fun logEvent(eventName: String, params: Bundle) {
        Timber.d("Logging AppMetrica event: $eventName with params: $params")
        val attributes = mutableMapOf<String, Any>()
        
        // Преобразуем Bundle в Map для AppMetrica
        params.keySet().forEach { key ->
            params.get(key)?.let { value ->
                attributes[key] = value
            }
        }
        
        AppMetrica.reportEvent(eventName, attributes)
    }

    override fun setUserProperty(name: String, value: String) {
        Timber.d("Setting AppMetrica user profile attribute: $name = $value")
        // В AppMetrica нет прямого аналога пользовательских свойств,
        // поэтому логируем событие с этими данными
        val attributes = mapOf("name" to name, "value" to value)
        AppMetrica.reportEvent("user_property", attributes)
    }

    override fun setUserId(userId: String) {
        Timber.d("Setting AppMetrica user ID: $userId")
        AppMetrica.setUserProfileID(userId)
    }
} 