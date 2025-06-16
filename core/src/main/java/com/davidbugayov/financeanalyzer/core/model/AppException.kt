package com.davidbugayov.financeanalyzer.core.model

/**
 * Базовый класс для всех исключений в приложении
 */
sealed class AppException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /**
     * Ошибки сети
     */
    sealed class Network(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {

        class Connection(cause: Throwable? = null) : Network("Ошибка подключения к сети", cause)
    }

    /**
     * Ошибки данных
     */
    sealed class Data(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {

        class ValidationError(message: String? = null) : Data(message ?: "Ошибка валидации")

        class NotFound(message: String? = null) : Data(message ?: "Данные не найдены")
    }

    /**
     * Ошибки файловой системы
     */
    sealed class FileSystem(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {

        class ReadError(message: String? = null, cause: Throwable? = null) : FileSystem(
            message ?: "Ошибка чтения файла",
            cause,
        )
    }

    /**
     * Ошибки бизнес-логики
     */
    sealed class Business(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {

        class InvalidOperation(message: String) : Business(message)
    }

    /**
     * Неизвестные ошибки
     */
    class Unknown(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message ?: "Неизвестная ошибка", cause)

    /**
     * Общая ошибка приложения
     */
    class GenericAppException(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message ?: "Ошибка приложения", cause)

    companion object {

        /**
         * Преобразует стандартное исключение в AppException
         */
        fun mapException(exception: Throwable): AppException {
            return when (exception) {
                is AppException -> exception
                is java.net.UnknownHostException,
                is java.net.ConnectException,
                is java.net.SocketTimeoutException,
                -> Network.Connection(exception)

                is java.io.IOException -> FileSystem.ReadError(cause = exception)
                else -> Unknown(exception.message, exception)
            }
        }
    }
} 