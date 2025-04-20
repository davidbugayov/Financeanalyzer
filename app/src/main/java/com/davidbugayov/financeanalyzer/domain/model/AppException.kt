package com.davidbugayov.financeanalyzer.domain.model

/**
 * Базовый класс для всех исключений в приложении
 */
sealed class AppException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Ошибки сети
     */
    sealed class Network(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause) {

        class Connection(cause: Throwable? = null) : Network("Ошибка подключения к сети", cause)
        class Server(message: String? = null) : Network(message ?: "Ошибка сервера")
        class Unknown(cause: Throwable? = null) : Network("Неизвестная сетевая ошибка", cause)
    }

    /**
     * Ошибки данных
     */
    sealed class Data(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause) {

        class NotFound(message: String? = null) : Data(message ?: "Данные не найдены")
        class InvalidFormat(message: String? = null) : Data(message ?: "Неверный формат данных")
        class ValidationError(message: String? = null) : Data(message ?: "Ошибка валидации")
    }

    /**
     * Ошибки файловой системы
     */
    sealed class FileSystem(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause) {

        class ReadError(message: String? = null, cause: Throwable? = null) : FileSystem(message ?: "Ошибка чтения файла", cause)
        class WriteError(message: String? = null, cause: Throwable? = null) : FileSystem(message ?: "Ошибка записи файла", cause)
        class NotFound(message: String? = null) : FileSystem(message ?: "Файл не найден")
    }

    /**
     * Ошибки бизнес-логики
     */
    sealed class Business(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause) {

        class InvalidOperation(message: String) : Business(message)
        class InsufficientFunds(message: String? = null) : Business(message ?: "Недостаточно средств")
    }

    /**
     * Неизвестные ошибки
     */
    class Unknown(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message ?: "Неизвестная ошибка", cause)
    
    companion object {
        /**
         * Преобразует стандартное исключение в AppException
         */
        fun mapException(exception: Throwable): AppException {
            return when (exception) {
                is AppException -> exception
                is java.net.UnknownHostException, 
                is java.net.ConnectException,
                is java.net.SocketTimeoutException -> Network.Connection(exception)
                is java.io.IOException -> FileSystem.ReadError(cause = exception)
                else -> Unknown(exception.message, exception)
            }
        }
    }
} 