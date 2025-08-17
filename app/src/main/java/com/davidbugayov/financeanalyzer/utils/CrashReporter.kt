package com.davidbugayov.financeanalyzer.utils

import android.app.Application
import android.os.Build
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.CrashLogger
import com.davidbugayov.financeanalyzer.analytics.CrashlyticsUtils
import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsConstants
import io.appmetrica.analytics.AppMetrica
import java.io.PrintWriter
import java.io.StringWriter
import timber.log.Timber

/**
 * Централизованная система отчетности о сбоях, которая перехватывает неперехваченные исключения
 * и отправляет их через систему аналитики.
 */
object CrashReporter : CrashLogger {
    private const val MAX_STACK_TRACE_LENGTH = 4000 // Ограничение длины стека для аналитики

    @Volatile
    var instance: CrashLogger = this

    @JvmStatic
    var isAppMetricaInitialized: Boolean = false

    /**
     * Инициализация системы отчетности о сбоях
     * @param application Экземпляр приложения
     */
    fun init(application: Application) {
        // Сохраняем существующий обработчик
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Логируем подробную информацию о сбое
                logCrash(throwable, application)
            } catch (e: Exception) {
                // Если отправка отчета о сбое не удалась, логируем ошибку, но не перезапускаем
                Timber.e(e, "Crash report error")
            } finally {
                // Всегда делегируем по умолчанию
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        Timber.d("CrashReporter initialized")
    }

    /**
     * Логирует нефатальную ошибку, которая не приводит к краху приложения
     * @param throwable Ошибка для логирования
     * @param message Опциональное сообщение, предоставляющее контекст ошибки
     */
    fun logError(
        throwable: Throwable,
        message: String = "",
    ) {
        Timber.e(throwable, message)
        // Отправка в Firebase Crashlytics
        if (BuildConfig.USE_FIREBASE) {
            CrashlyticsUtils.logException(throwable)
        }

        val stackTrace = getStackTraceString(throwable)
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"

        // Отправка в аналитику
        val params =
            android.os.Bundle().apply {
                putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
                putString(AnalyticsConstants.Params.ERROR_MESSAGE, "$message: $errorMessage")
                putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
                putString(AnalyticsConstants.Params.IS_FATAL, "false")
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.APP_ERROR, params)
        // Отправляем ошибку в AppMetrica
        AppMetrica.reportError("APP_ERROR: $errorType - $errorMessage", throwable)
    }

    /**
     * Логирует обработанное исключение с пользовательскими параметрами
     * @param throwable Исключение для логирования
     * @param customParams Дополнительные параметры для включения в отчет об ошибке
     */
    fun logException(
        throwable: Throwable,
        customParams: Map<String, String> = emptyMap(),
    ) {
        Timber.e(throwable)
        // Отправка в Firebase Crashlytics
        if (BuildConfig.USE_FIREBASE) {
            CrashlyticsUtils.logException(throwable)
        }

        val stackTrace = getStackTraceString(throwable)
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"

        // Отправка в аналитику
        val params =
            android.os.Bundle().apply {
                putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
                putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
                putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
                putString(AnalyticsConstants.Params.IS_FATAL, "false")

                // Добавляем пользовательские параметры
                customParams.forEach { (key, value) ->
                    putString(key, value)
                }
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.APP_EXCEPTION, params)
        // Отправляем ошибку в AppMetrica
        AppMetrica.reportError("APP_EXCEPTION: $errorType - $errorMessage", throwable)
    }

    /**
     * Логирует детали о сбое
     */
    private fun logCrash(
        throwable: Throwable,
        application: Application,
    ) {
        val stackTrace = getStackTraceString(throwable)
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"

        Timber.e(throwable, "FATAL CRASH: $errorType - $errorMessage")
        // Отправка в Firebase Crashlytics
        if (BuildConfig.USE_FIREBASE) {
            CrashlyticsUtils.logException(throwable)
        }

        // Отправка в аналитику
        val params =
            android.os.Bundle().apply {
                putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
                putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
                putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
                putString(AnalyticsConstants.Params.IS_FATAL, "true")
                putString(AnalyticsConstants.Params.APP_VERSION, BuildConfig.VERSION_NAME)
                putInt(AnalyticsConstants.Params.APP_VERSION_CODE, BuildConfig.VERSION_CODE)
                putString(AnalyticsConstants.Params.DEVICE_MODEL, "${Build.MANUFACTURER} ${Build.MODEL}")
                putString(
                    AnalyticsConstants.Params.ANDROID_VERSION,
                    "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
                )
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.APP_CRASH, params)
        // Отправляем фатальный крэш в AppMetrica
        AppMetrica.reportUnhandledException(throwable)
    }

    /**
     * Логирует пользовательскую ошибку (аналог trackError из ErrorTracker)
     */
    fun trackError(
        errorType: String,
        errorMessage: String,
        additionalParams: Map<String, Any> = emptyMap(),
    ) {
        Timber.e("Error: [$errorType] $errorMessage")
        // Отправка в Firebase Crashlytics
        if (BuildConfig.USE_FIREBASE) {
            CrashlyticsUtils.logException(Exception("$errorType: $errorMessage"))
        }
        // Отправка в AppMetrica
        AppMetrica.reportError("ERROR: $errorType - $errorMessage", null as Throwable?)
    }

    /**
     * Логирует ошибку валидации (аналог trackValidationError из ErrorTracker)
     */
    fun trackValidationError(
        field: String,
        errorMessage: String,
    ) {
        Timber.w("Validation error in field '$field': $errorMessage")
        // Отправка в Firebase Crashlytics
        if (BuildConfig.USE_FIREBASE) {
            CrashlyticsUtils.logException(Exception("Validation error in field '$field': $errorMessage"))
        }
        // Отправка в AppMetrica
        AppMetrica.reportError("VALIDATION_ERROR: $field - $errorMessage", null as Throwable?)
    }

    /**
     * Логирует ошибку базы данных (аналог trackDatabaseError из ErrorTracker)
     */
    fun trackDatabaseError(
        operation: String,
        errorMessage: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Timber.e(throwable, "Database error during $operation: $errorMessage")
            // Отправка в Firebase Crashlytics
            if (BuildConfig.USE_FIREBASE) {
                CrashlyticsUtils.logException(throwable)
            }
            // Отправка в AppMetrica
            AppMetrica.reportError("DATABASE_ERROR: $operation - $errorMessage", throwable)
        } else {
            Timber.e("Database error during $operation: $errorMessage")
            // Отправка в Firebase Crashlytics
            if (BuildConfig.USE_FIREBASE) {
                CrashlyticsUtils.logException(Exception("Database error during $operation: $errorMessage"))
            }
            // Отправка в AppMetrica
            AppMetrica.reportError("DATABASE_ERROR: $operation - $errorMessage", null as Throwable?)
        }
    }

    override fun logException(throwable: Throwable) {
        if (!isAppMetricaInitialized) {
            Timber.e(
                throwable,
                "[CrashReporter] AppMetrica не инициализирована, ошибка только в логах: %s",
                throwable.message,
            )
            return
        }
        try {
            io.appmetrica.analytics.AppMetrica
                .reportError(throwable.message ?: "", throwable)
        } catch (e: Exception) {
            Timber.e(e, "[CrashReporter] Ошибка при отправке в AppMetrica: %s", e.message)
        }
    }

    override fun logDatabaseError(
        operation: String,
        errorMessage: String,
        throwable: Throwable?,
    ) {
        trackDatabaseError(operation, errorMessage, throwable)
    }

    /**
     * Преобразует Throwable в строковое представление его стека вызовов
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        var stackTrace = sw.toString()

        // Ограничиваем длину стека для аналитики
        if (stackTrace.length > MAX_STACK_TRACE_LENGTH) {
            stackTrace = stackTrace.substring(0, MAX_STACK_TRACE_LENGTH) + "... (truncated)"
        }

        return stackTrace
    }
}
