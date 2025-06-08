package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import timber.log.Timber

/**
 * Пустая реализация RuStoreUtils для F-Droid flavor
 */
object RuStoreUtils {
    /**
     * Пустая реализация метода requestReview
     */
    fun requestReview(context: Context) {
        Timber.d("RuStore not available in F-Droid flavor")
    }

    /**
     * Пустая реализация метода checkForUpdates
     */
    fun checkForUpdates(context: Context) {
        Timber.d("RuStore not available in F-Droid flavor")
    }
} 