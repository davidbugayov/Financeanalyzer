package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

/**
 * Adapter for Firebase Analytics implementing IAnalytics.
 * Used in RuStore flavor.
 */
class FirebaseAnalyticsAdapter(
    private val firebaseAnalytics: FirebaseAnalytics
) : IAnalytics {

    override fun logEvent(eventName: String) {
        Timber.d("Logging Firebase event: $eventName")
        firebaseAnalytics.logEvent(eventName, null)
    }

    override fun logEvent(eventName: String, params: Bundle) {
        Timber.d("Logging Firebase event: $eventName with params: $params")
        firebaseAnalytics.logEvent(eventName, params)
    }

    override fun setUserProperty(name: String, value: String) {
        Timber.d("Setting Firebase user property: $name = $value")
        firebaseAnalytics.setUserProperty(name, value)
    }

    override fun setUserId(userId: String) {
        Timber.d("Setting Firebase user ID: $userId")
        firebaseAnalytics.setUserId(userId)
    }
} 