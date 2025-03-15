package com.davidbugayov.financeanalyzer.utils

import android.util.Log
import com.davidbugayov.financeanalyzer.FinanceApp
import timber.log.Timber
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Инициализатор для настройки логирования
 */
object TimberInitializer {
    fun init(isDebug: Boolean) {
        if (isDebug) {
            // Для отладочной сборки используем расширенное логирование через Timber
            Timber.plant(Timber.DebugTree())
            Timber.d("Initialized Timber with DebugTree")
        } else {
            // Для релизной сборки всегда используем CrashReportingTree
            try {
                Timber.plant(CrashReportingTree())
                Timber.d("Initialized Timber with CrashReportingTree")
            } catch (e: Exception) {
                // В случае ошибки используем DebugTree
                Timber.plant(Timber.DebugTree())
                Log.w("TimberInitializer", "Failed to initialize CrashReportingTree, using DebugTree instead", e)
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

            // Получаем экземпляр Crashlytics напрямую
            val crashlytics = try {
                FirebaseCrashlytics.getInstance()
            } catch (e: Exception) {
                Log.e("CrashReportingTree", "Failed to get Crashlytics instance", e)
                return
            }

            // Для информационных логов только записываем сообщение
            val logMessage = if (tag != null) "[$tag] $message" else message
            crashlytics.log(logMessage)

            // Добавляем дополнительные данные для отслеживания
            crashlytics.setCustomKey("log_priority", priority)
            crashlytics.setCustomKey("log_tag", tag ?: "NO_TAG")

            // Для ошибок записываем исключение
            if (t != null) {
                crashlytics.recordException(t)
            } else if (priority == Log.ERROR || priority == Log.ASSERT) {
                // Если исключения нет, но это ошибка, создаем исключение из сообщения
                crashlytics.recordException(Exception(logMessage))
            }
        } catch (e: Exception) {
            // В случае ошибки при логировании, выводим в стандартный лог
            // Используем стандартный Log, так как Timber может вызвать рекурсию
            Log.e("CrashReportingTree", "Ошибка при логировании: ${e.message}")
            Log.println(priority, tag ?: "NO_TAG", message)
        }
    }
} 