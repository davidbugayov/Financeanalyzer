package com.davidbugayov.financeanalyzer.analytics

import timber.log.Timber

object CrashlyticsUtils {
    fun logException(throwable: Throwable) {
        Timber.e(throwable, "Исключение")
    }

    fun log(message: String) {
        Timber.d("Log: $message")
    }
}
