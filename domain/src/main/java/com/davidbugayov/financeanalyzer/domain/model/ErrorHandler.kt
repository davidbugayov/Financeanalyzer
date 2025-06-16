package com.davidbugayov.financeanalyzer.domain.model

import timber.log.Timber
import java.io.IOException
import com.davidbugayov.financeanalyzer.domain.util.Result

/**
 * Преобразует исключения в AppException
 */
fun mapException(e: Exception): AppException = when (e) {
    is IOException -> AppException.FileSystem.ReadError(cause = e)
    is IllegalArgumentException -> AppException.Data.ValidationError(e.message)
    is IllegalStateException -> AppException.Business.InvalidOperation(
        e.message ?: "Недопустимая операция",
    )
    is AppException -> e
    else -> {
        Timber.e(e, "Unmapped exception")
        AppException.Unknown(cause = e)
    }
}

/**
 * Преобразует Result в другой тип
 */
fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> try {
        Result.Success(transform(data))
    } catch (e: Exception) {
        Timber.e(e, "Error in map transformation")
        Result.Error(mapException(e))
    }
    is Result.Error -> Result.Error(exception)
}

/**
 * Преобразует результат с помощью функции fold
 */
inline fun <T, R> Result<T>.fold(onSuccess: (T) -> R, onFailure: (AppException) -> R): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onFailure(exception)
}
