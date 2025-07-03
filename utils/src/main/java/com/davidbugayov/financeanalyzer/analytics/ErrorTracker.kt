package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Класс для отслеживания и анализа ошибок приложения.
 * Собирает информацию об ошибках, исключениях и сбоях,
 * отправляет их в аналитику и логирует.
 */
object ErrorTracker {
    private const val MAX_STACK_TRACE_LENGTH = 4000 // Лимит длины стека вызовов для аналитики

    /**
     * Отслеживать ошибку
     * @param errorType Тип ошибки
     * @param errorMessage Сообщение об ошибке
     * @param additionalParams Дополнительные параметры
     */
    fun trackError(errorType: String, errorMessage: String, additionalParams: Map<String, Any> = emptyMap()) {
        Timber.e("Error: [$errorType] $errorMessage")

        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)

            // Добавляем дополнительные параметры
            additionalParams.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.ERROR, params)
    }

    /**
     * Отслеживать исключение
     * @param throwable Исключение
     * @param isFatal Является ли исключение фатальным (приводящим к сбою)
     * @param additionalParams Дополнительные параметры
     */
    fun trackException(
        throwable: Throwable,
        isFatal: Boolean = false,
        additionalParams: Map<String, Any> = emptyMap(),
    ) {
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"
        val stackTrace = getStackTraceString(throwable)

        if (isFatal) {
            Timber.e(throwable, "FATAL: $errorType - $errorMessage")
        } else {
            Timber.e(throwable, "Exception: $errorType - $errorMessage")
        }

        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
            putBoolean(AnalyticsConstants.Params.IS_FATAL, isFatal)

            // Добавляем дополнительные параметры
            additionalParams.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }

        val eventName = if (isFatal) AnalyticsConstants.Events.APP_CRASH else AnalyticsConstants.Events.APP_EXCEPTION
        AnalyticsUtils.logEvent(eventName, params)
    }

    /**
     * Отслеживать ошибку валидации
     * @param field Поле, в котором произошла ошибка
     * @param errorMessage Сообщение об ошибке
     */
    fun trackValidationError(field: String, errorMessage: String) {
        Timber.w("Validation error in field '$field': $errorMessage")

        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.VALIDATION_FIELD, field)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.ERROR_TYPE, AnalyticsConstants.Values.ERROR_TYPE_VALIDATION)
        }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.VALIDATION_ERROR, params)
    }

    /**
     * Отслеживать ошибку сети
     * @param url URL, на котором произошла ошибка
     * @param errorCode Код ошибки
     * @param errorMessage Сообщение об ошибке
     */
    fun trackNetworkError(url: String, errorCode: Int, errorMessage: String) {
        Timber.e("Network error: [$errorCode] $errorMessage, URL: $url")

        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, AnalyticsConstants.Values.ERROR_TYPE_NETWORK)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putInt(AnalyticsConstants.Params.ERROR_CODE, errorCode)
            putString("url", url)
        }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.NETWORK_ERROR, params)
    }

    /**
     * Отслеживать ошибку базы данных
     * @param operation Операция, при которой произошла ошибка
     * @param errorMessage Сообщение об ошибке
     * @param throwable Исключение (опционально)
     */
    fun trackDatabaseError(operation: String, errorMessage: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.e(throwable, "Database error during $operation: $errorMessage")
        } else {
            Timber.e("Database error during $operation: $errorMessage")
        }

        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, AnalyticsConstants.Values.ERROR_TYPE_DATABASE)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.OPERATION_NAME, operation)
            throwable?.let {
                putString(AnalyticsConstants.Params.STACK_TRACE, getStackTraceString(it))
            }
        }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.DATABASE_ERROR, params)
    }

    /**
     * Получить строковое представление стека вызовов
     * @param throwable Исключение
     * @return Строка со стеком вызовов
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        var stackTrace = sw.toString()

        // Ограничиваем длину стека вызовов для аналитики
        if (stackTrace.length > MAX_STACK_TRACE_LENGTH) {
            stackTrace = stackTrace.substring(0, MAX_STACK_TRACE_LENGTH) + "... (truncated)"
        }

        return stackTrace
    }

    /**
     * Получить корневую причину исключения
     * @param throwable Исключение
     * @return Корневая причина
     */
    fun getRootCause(throwable: Throwable): Throwable {
        var rootCause: Throwable = throwable
        while (rootCause.cause != null && rootCause.cause !== rootCause) {
            rootCause = rootCause.cause!!
        }
        return rootCause
    }

    /**
     * Получить краткое описание ошибки для пользователя
     * @param throwable Исключение
     * @return Краткое описание ошибки
     */
    fun getUserFriendlyErrorMessage(throwable: Throwable): String {
        val rootCause = getRootCause(throwable)
        return when (rootCause) {
            is java.net.UnknownHostException -> "Не удалось подключиться к серверу. Проверьте подключение к интернету."
            is java.net.SocketTimeoutException -> "Время ожидания ответа от сервера истекло. Попробуйте позже."
            is java.io.IOException -> "Ошибка ввода/вывода: ${rootCause.message ?: "неизвестная ошибка"}"
            is java.lang.IllegalArgumentException -> "Недопустимое значение: ${rootCause.message ?: "неизвестная ошибка"}"
            is java.lang.NullPointerException -> "Внутренняя ошибка приложения. Пожалуйста, сообщите разработчикам."
            is java.lang.OutOfMemoryError -> "Недостаточно памяти для выполнения операции. Попробуйте перезапустить приложение."
            is java.util.concurrent.TimeoutException -> "Время выполнения операции истекло. Попробуйте позже."
            else -> "Произошла ошибка: ${rootCause.message ?: "неизвестная ошибка"}"
        }
    }
}
