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
            when {
                params.containsKey(key) -> {
                    val value = when {
                        params.getString(key) != null -> params.getString(key)
                        params.getInt(key, Int.MIN_VALUE) != Int.MIN_VALUE -> params.getInt(key)
                        params.getBoolean(key, false) || params.getBoolean(key, true) -> params.getBoolean(key)
                        params.getFloat(key, Float.MIN_VALUE) != Float.MIN_VALUE -> params.getFloat(key)
                        params.getDouble(key, Double.MIN_VALUE) != Double.MIN_VALUE -> params.getDouble(key)
                        params.getLong(key, Long.MIN_VALUE) != Long.MIN_VALUE -> params.getLong(key)
                        else -> null
                    }
                    value?.let {
                        attributes[key] = it
                    }
                }
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
