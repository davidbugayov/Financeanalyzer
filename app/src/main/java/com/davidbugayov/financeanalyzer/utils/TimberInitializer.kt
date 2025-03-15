package com.davidbugayov.financeanalyzer.utils

import android.util.Log
import com.davidbugayov.financeanalyzer.BuildConfig
import timber.log.Timber

/**
 * Инициализатор для настройки логирования
 */
object TimberInitializer {

    fun init() {
        if (BuildConfig.DEBUG) {
            // Для отладочной сборки используем расширенное логирование через Timber
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized with DebugTree")
        } else {
            // Для релизной сборки используем Firebase Crashlytics
            Timber.plant(CrashReportingTree())
            Timber.i("Timber initialized with CrashReportingTree")
        }
    }
}

/**
 * Дерево логирования для релизной сборки
 * Отправляет ошибки в Firebase Crashlytics
 */
private class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Игнорируем низкоприоритетные логи
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        // Для информационных логов только записываем сообщение
        if (priority == Log.INFO) {
            CrashlyticsUtils.log(message)
            return
        }

        // Для ошибок и предупреждений добавляем больше контекста
        val logMessage = if (tag != null) "[$tag] $message" else message
        CrashlyticsUtils.log(logMessage)

        // Добавляем дополнительные данные для отслеживания
        CrashlyticsUtils.setCustomKey("log_priority", priority)
        CrashlyticsUtils.setCustomKey("log_tag", tag ?: "NO_TAG")

        // Для ошибок записываем исключение
        if (t != null) {
            CrashlyticsUtils.recordException(t, logMessage)
        } else if (priority == Log.ERROR || priority == Log.ASSERT) {
            // Если исключения нет, но это ошибка, создаем исключение из сообщения
            CrashlyticsUtils.recordException(Exception(logMessage))
        }
    }
} 