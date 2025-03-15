package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Утилитарный класс для работы с Firebase Crashlytics.
 * Содержит методы для логирования ошибок и установки пользовательских ключей.
 */
object CrashlyticsUtils {

    /**
     * Логирует сообщение в Crashlytics
     * @param message Сообщение для логирования
     */
    fun log(message: String) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics log: $message")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().log(message)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to log to Crashlytics: $message")
        }
    }

    /**
     * Записывает исключение в Crashlytics
     * @param throwable Исключение для записи
     * @param message Дополнительное сообщение (опционально)
     */
    fun recordException(throwable: Throwable, message: String? = null) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.e(throwable, "Crashlytics exception: ${message ?: throwable.message}")
            } else {
                // В release-сборке отправляем в Crashlytics
                message?.let { FirebaseCrashlytics.getInstance().log(it) }
                FirebaseCrashlytics.getInstance().recordException(throwable)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to record exception in Crashlytics")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: String) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics custom key: $key = $value")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set custom key in Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Boolean) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics custom key: $key = $value")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set custom key in Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics custom key: $key = $value")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set custom key in Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Long) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics custom key: $key = $value")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set custom key in Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Float) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics custom key: $key = $value")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set custom key in Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает пользовательский ключ для Crashlytics
     * @param key Ключ
     * @param value Значение
     */
    fun setCustomKey(key: String, value: Double) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics custom key: $key = $value")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set custom key in Crashlytics: $key = $value")
        }
    }

    /**
     * Устанавливает идентификатор пользователя для Crashlytics
     * @param userId Идентификатор пользователя
     */
    fun setUserId(userId: String) {
        try {
            if (BuildConfig.DEBUG) {
                // В debug-сборке только логируем в Timber
                Timber.d("Crashlytics user ID: $userId")
            } else {
                // В release-сборке отправляем в Crashlytics
                FirebaseCrashlytics.getInstance().setUserId(userId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set user ID in Crashlytics: $userId")
        }
    }

    /**
     * Логирует информацию о текущем состоянии приложения
     * @param state Состояние приложения
     */
    fun logAppState(state: Map<String, Any>) {
        state.forEach { (key, value) ->
            when (value) {
                is String -> setCustomKey(key, value)
                is Boolean -> setCustomKey(key, value)
                is Int -> setCustomKey(key, value)
                is Long -> setCustomKey(key, value)
                is Float -> setCustomKey(key, value)
                is Double -> setCustomKey(key, value)
                else -> setCustomKey(key, value.toString())
            }
        }
    }
} 