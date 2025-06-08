package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import timber.log.Timber

/**
 * Заглушка адаптера Firebase Analytics для F-Droid flavor.
 * Имитирует API Firebase, но не выполняет реальных действий.
 */
class FirebaseAnalyticsAdapter : IAnalytics {
    
    override fun logEvent(eventName: String) {
        Timber.d("Stub Firebase event: $eventName")
    }

    override fun logEvent(eventName: String, params: Bundle) {
        Timber.d("Stub Firebase event: $eventName with params: $params")
    }

    override fun setUserProperty(name: String, value: String) {
        Timber.d("Stub Firebase user property: $name = $value")
    }

    override fun setUserId(userId: String) {
        Timber.d("Stub Firebase user ID: $userId")
    }
} 