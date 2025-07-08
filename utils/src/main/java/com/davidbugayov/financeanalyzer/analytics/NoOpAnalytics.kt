package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import timber.log.Timber

/**
 * Реализация IAnalytics, которая ничего не делает.
 * Используется как запасной вариант, если не настроена другая аналитика.
 */
class NoOpAnalytics : IAnalytics {
    override fun logEvent(eventName: String) {
        Timber.d("NoOpAnalytics: Event logged: $eventName (no-op)")
    }

    override fun logEvent(
        eventName: String,
        params: Bundle,
    ) {
        Timber.d("NoOpAnalytics: Event logged: $eventName with params (no-op)")
    }

    override fun setUserProperty(
        name: String,
        value: String,
    ) {
        Timber.d("NoOpAnalytics: User property set: $name = $value (no-op)")
    }

    override fun setUserId(userId: String) {
        Timber.d("NoOpAnalytics: User ID set: $userId (no-op)")
    }
}
