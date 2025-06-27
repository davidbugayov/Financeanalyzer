package com.davidbugayov.financeanalyzer.utils

import android.app.Activity
import android.content.Context

/**
 * Stub implementation of RuStoreUtils for library modules.
 * For non-rustore flavors, this does nothing.
 */
object RuStoreUtils {
    /**
     * Stub method for checking updates in RuStore.
     * Does nothing in non-rustore flavors.
     */
    fun checkForUpdates(context: Context) {
        // No-op stub for non-rustore flavors
    }
    
    /**
     * Stub method for requesting app review in RuStore.
     * Does nothing in non-rustore flavors.
     */
    fun requestReview(activity: Activity) {
        // No-op stub for non-rustore flavors
    }
} 
