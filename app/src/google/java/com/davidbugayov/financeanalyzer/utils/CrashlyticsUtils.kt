package com.davidbugayov.financeanalyzer.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Вспомогательные утилиты для работы с Firebase Crashlytics.
 * Реальная имплементация для Google flavor.
 */
object CrashlyticsUtils {

    /**
     * Возвращает экземпляр Crashlytics
     */
    private fun getCrashlyticsInstance(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }

    /**
     * Логирует нефатальное исключение в Crashlytics и Timber
     */
    fun logException(throwable: Throwable) {
        Timber.e(throwable, "Исключение")
        try {
            getCrashlyticsInstance().recordException(throwable)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании исключения в Crashlytics")
        }
    }

    /**
     * Логирует произвольное сообщение в Crashlytics и Timber
     */
    fun log(message: String) {
        Timber.d("Crashlytics log: $message")
        try {
            getCrashlyticsInstance().log(message)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании сообщения в Crashlytics")
        }
    }

    /**
     * Устанавливает пользовательский идентификатор для Crashlytics
     */
    fun setUserId(userId: String) {
        Timber.d("Установка Crashlytics userId: $userId")
        try {
            getCrashlyticsInstance().setUserId(userId)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке userId в Crashlytics")
        }
    }

    /**
     * Добавляет строковый ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: String) {
        Timber.d("Установка Crashlytics key: $key = $value")
        try {
            getCrashlyticsInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке строкового ключа в Crashlytics")
        }
    }

    /**
     * Добавляет целочисленный ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Int) {
        Timber.d("Установка Crashlytics key: $key = $value")
        try {
            getCrashlyticsInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке целочисленного ключа в Crashlytics")
        }
    }

    /**
     * Добавляет булевский ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Boolean) {
        Timber.d("Установка Crashlytics key: $key = $value")
        try {
            getCrashlyticsInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке булевского ключа в Crashlytics")
        }
    }

    /**
     * Добавляет число с плавающей точкой ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Float) {
        Timber.d("Установка Crashlytics key: $key = $value")
        try {
            getCrashlyticsInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке ключа с плавающей точкой в Crashlytics")
        }
    }

    /**
     * Добавляет число с плавающей точкой ключ-значение в отчет о сбое
     */
    fun setCustomKey(key: String, value: Double) {
        Timber.d("Установка Crashlytics key: $key = $value")
        try {
            getCrashlyticsInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке ключа с плавающей точкой в Crashlytics")
        }
    }
}
