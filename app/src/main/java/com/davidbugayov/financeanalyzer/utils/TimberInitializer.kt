package com.davidbugayov.financeanalyzer.utils

import android.util.Log
import com.davidbugayov.financeanalyzer.FinanceApp
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
            // Для релизной сборки используем Firebase Crashlytics, только если Firebase инициализирован
            if (FinanceApp.isFirebaseInitialized) {
                Timber.plant(CrashReportingTree())
            } else {
                // Если Firebase не инициализирован, используем DebugTree
                Timber.plant(Timber.DebugTree())
                Timber.w("Firebase не инициализирован, используем DebugTree вместо CrashReportingTree")
            }
        }
    }
}

/**
 * Дерево логирования для релизной сборки
 * Отправляет ошибки в Firebase Crashlytics
 */
private class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            // Игнорируем низкоприоритетные логи
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            // Проверяем, что Firebase инициализирован
            if (!FinanceApp.isFirebaseInitialized) {
                // Если Firebase не инициализирован, просто выводим в лог через стандартный механизм
                // Используем стандартный Log, так как Timber может вызвать рекурсию
                Log.println(priority, tag ?: "NO_TAG", message)
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
        } catch (e: Exception) {
            // В случае ошибки при логировании, выводим в стандартный лог
            // Используем стандартный Log, так как Timber может вызвать рекурсию
            Log.e("CrashReportingTree", "Ошибка при логировании: ${e.message}")
            Log.println(priority, tag ?: "NO_TAG", message)
        }
    }
} 