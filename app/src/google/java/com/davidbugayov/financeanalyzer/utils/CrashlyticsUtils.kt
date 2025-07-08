package com.davidbugayov.financeanalyzer.utils

import timber.log.Timber

/**
 * Вспомогательные утилиты для работы с Firebase Crashlytics.
 * Заглушка для F-Droid совместимости.
 */
object CrashlyticsUtils {
    /**
     * Логирует нефатальное исключение в Timber
     */
    fun logException(throwable: Throwable) {
        Timber.e(throwable, "Исключение")
    }

    /**
     * Логирует произвольное сообщение в Timber
     */
    fun log(message: String) {
        Timber.d("Log: $message")
    }

    /**
     * Заглушка для установки пользовательского идентификатора
     */
    fun setUserId(userId: String) {
        Timber.d("UserId: $userId")
    }

    /**
     * Заглушка для строкового ключ-значения
     */
    fun setCustomKey(
        key: String,
        value: String,
    ) {
        Timber.d("Key: $key = $value")
    }

    /**
     * Заглушка для целочисленного ключ-значения
     */
    fun setCustomKey(
        key: String,
        value: Int,
    ) {
        Timber.d("Key: $key = $value")
    }

    /**
     * Заглушка для булевского ключ-значения
     */
    fun setCustomKey(
        key: String,
        value: Boolean,
    ) {
        Timber.d("Key: $key = $value")
    }

    /**
     * Заглушка для числа с плавающей точкой ключ-значения
     */
    fun setCustomKey(
        key: String,
        value: Float,
    ) {
        Timber.d("Key: $key = $value")
    }

    /**
     * Заглушка для числа с плавающей точкой ключ-значения
     */
    fun setCustomKey(
        key: String,
        value: Double,
    ) {
        Timber.d("Key: $key = $value")
    }
}
