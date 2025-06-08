package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import timber.log.Timber

/**
 * Заглушка для RuStore SDK в F-Droid flavor
 */
object RuStoreUtils {
    /**
     * Заглушка для запуска диалога отзыва в RuStore
     */
    fun requestReview(context: Context) {
        Timber.d("RuStore review not available in F-Droid flavor")
    }

    /**
     * Заглушка для проверки обновлений в RuStore
     */
    fun checkForUpdates(context: Context) {
        Timber.d("RuStore updates not available in F-Droid flavor")
    }
} 