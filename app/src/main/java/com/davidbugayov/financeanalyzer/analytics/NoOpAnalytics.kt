package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import timber.log.Timber

/**
 * Реализация интерфейса IAnalytics, которая ничего не делает.
 * Используется для F-Droid версии приложения или когда аналитика отключена.
 */
class NoOpAnalytics : IAnalytics {
    override fun logEvent(eventName: String) {
        Timber.d("Analytics event: $eventName (no-op)")
    }

    override fun logEvent(eventName: String, params: Bundle) {
        Timber.d("Analytics event: $eventName with params: $params (no-op)")
    }

    override fun setUserProperty(name: String, value: String) {
        Timber.d("Analytics user property: $name = $value (no-op)")
    }

    override fun setUserId(userId: String) {
        Timber.d("Analytics user ID: $userId (no-op)")
    }
} 
