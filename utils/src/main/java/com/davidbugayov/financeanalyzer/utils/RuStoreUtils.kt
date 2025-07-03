package com.davidbugayov.financeanalyzer.utils

import android.app.Activity
import android.content.Context

/**
 * Stub implementation of RuStoreUtils for non-RuStore flavors.
 * Does nothing in Google and F-Droid builds.
 */
object RuStoreUtils {
    /**
     * No-op update check.
     */
    fun checkForUpdates(context: Context) {
        // No-op
    }

    /**
     * No-op review request.
     */
    fun requestReview(activity: Activity) {
        // No-op
    }
}
