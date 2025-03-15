package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.FinanceApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Утилитарный класс для работы с Firebase Crashlytics.
 * Содержит методы для отслеживания ошибок и исключений.
 */
object CrashlyticsUtils {

    /**
     * Безопасно получает экземпляр Crashlytics
     * @return Экземпляр FirebaseCrashlytics или null, если не удалось получить
     */
    private fun getCrashlyticsInstance(): FirebaseCrashlytics? {
        return try {
            // Всегда пытаемся получить экземпляр напрямую, независимо от флага
            FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении экземпляра Crashlytics")
            null
        }
    }

    /**
     * Логирует сообщение в Crashlytics
     * @param message Сообщение для логирования
     */
    fun log(message: String) {
        try {
            getCrashlyticsInstance()?.log(message) ?: Timber.d("Сообщение не отправлено в Crashlytics: $message")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании в Crashlytics")
        }
    }

    /**
     * Записывает исключение в Crashlytics
     * @param throwable Исключение для записи
     * @param message Дополнительное сообщение (опционально)
     */
    fun recordException(throwable: Throwable, message: String? = null) {
        try {
            val crashlytics = getCrashlyticsInstance()
            if (crashlytics != null) {
                message?.let { crashlytics.log(it) }
                crashlytics.recordException(throwable)
                Timber.e(throwable, message ?: "Exception recorded in Crashlytics")
            } else {
                Timber.e(throwable, "Исключение не отправлено в Crashlytics: ${message ?: ""}")
                throwable.printStackTrace()
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при записи исключения в Crashlytics")
            throwable.printStackTrace()
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: String) {
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value) ?: Timber.d("Ключ не установлен в Crashlytics: $key = $value")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке ключа в Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Long) {
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value) ?: Timber.d("Ключ не установлен в Crashlytics: $key = $value")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке ключа в Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            getCrashlyticsInstance()?.setCustomKey(key, value) ?: Timber.d("Ключ не установлен в Crashlytics: $key = $value")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при установке ключа в Crashlytics: $key = $value")
        }
    }
} 