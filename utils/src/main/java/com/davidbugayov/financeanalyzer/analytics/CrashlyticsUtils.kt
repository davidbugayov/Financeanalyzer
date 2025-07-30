package com.davidbugayov.financeanalyzer.analytics

import timber.log.Timber

object CrashlyticsUtils {
    fun logException(throwable: Throwable) {
        Timber.e(throwable, "Исключение")
    }

    fun log(message: String) {
        Timber.d("Log: $message")
    }

    fun setUserId(userId: String) {
        Timber.d("UserId: $userId")
    }

    fun setCustomKey(
        key: String,
        value: String,
    ) {
        Timber.d("Key: $key = $value")
    }

    fun setCustomKey(
        key: String,
        value: Int,
    ) {
        Timber.d("Key: $key = $value")
    }

    fun setCustomKey(
        key: String,
        value: Boolean,
    ) {
        Timber.d("Key: $key = $value")
    }

    fun setCustomKey(
        key: String,
        value: Float,
    ) {
        Timber.d("Key: $key = $value")
    }

    fun setCustomKey(
        key: String,
        value: Double,
    ) {
        Timber.d("Key: $key = $value")
    }
} 
