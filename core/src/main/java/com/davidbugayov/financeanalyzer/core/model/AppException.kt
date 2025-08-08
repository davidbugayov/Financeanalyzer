package com.davidbugayov.financeanalyzer.core.model

import com.davidbugayov.financeanalyzer.core.R
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider
import org.koin.core.context.GlobalContext

// Строки берутся через ResourceProvider (Koin GlobalContext)

/**
 * Базовый класс для всех исключений в приложении
 */
sealed class AppException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    private object RStrings {
        private val resourceProvider: ResourceProvider
            get() = GlobalContext.get().get()
        val errorNetworkConnection: String get() = resourceProvider.getString(R.string.error_network_connection)
        val errorDataNotFound: String get() = resourceProvider.getString(R.string.error_data_not_found)
        val errorFileRead: String get() = resourceProvider.getString(R.string.error_file_read)
        val errorUnknown: String get() = resourceProvider.getString(R.string.error_unknown)
    }

    /**
     * Ошибки сети
     */
    sealed class Network(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {
        class Connection(cause: Throwable? = null) : Network(RStrings.errorNetworkConnection, cause)
    }

    /**
     * Ошибки данных
     */
    sealed class Data(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {
        class NotFound(message: String? = null) : Data(message ?: RStrings.errorDataNotFound)
    }

    /**
     * Ошибки файловой системы
     */
    sealed class FileSystem(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message, cause) {
        class ReadError(message: String? = null, cause: Throwable? = null) : FileSystem(
            message ?: RStrings.errorFileRead,
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
    ) : AppException(message ?: RStrings.errorUnknown, cause)

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
