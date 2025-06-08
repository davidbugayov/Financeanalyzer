package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import timber.log.Timber

/**
 * Класс для инициализации Timber
 */
object TimberInitializer {

    /**
     * Инициализирует Timber в зависимости от режима сборки
     */
    fun init(context: Context, isDebug: Boolean) {
        if (isDebug) {
            // Для отладочных сборок используем DebugTree
            Timber.plant(Timber.DebugTree())
        } else {
            // Для релизных сборок используем CrashReportingTree
            Timber.plant(CrashReportingTree())
        }
    }

    /**
     * Timber Tree для релизных сборок с отправкой ошибок
     */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Логируем исключение, если оно есть
            if (t != null) {
                CrashlyticsUtils.logException(t)
            }
        }
    }
}
