package com.davidbugayov.financeanalyzer.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Инициализатор для настройки логирования
 */
object TimberInitializer {
    fun init(isDebug: Boolean) {
        if (isDebug) {
            // Для отладочной сборки используем расширенное логирование через Timber
            Timber.plant(Timber.DebugTree())
        } else {
            // Для релизной сборки используем Firebase Crashlytics
            Timber.plant(CrashReportingTree())
        }
    }
}

/**
 * Дерево логирования для релизной сборки
 * Отправляет ошибки в Firebase Crashlytics
 */
private class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        if (t != null) {
            if (priority == Log.ERROR) {
                FirebaseCrashlytics.getInstance().recordException(t)
            }
        }

        // Добавляем дополнительные данные для отслеживания
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("priority", priority)
            setCustomKey("tag", tag ?: "NO_TAG")
            setCustomKey("message", message)
            log(message)
        }
    }
} 