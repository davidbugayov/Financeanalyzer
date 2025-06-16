package com.davidbugayov.financeanalyzer.core.util

import com.davidbugayov.financeanalyzer.core.model.AppException
import timber.log.Timber

/**
 * Преобразует Result в другой тип
 */
fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> try {
        Result.Success(transform(data))
    } catch (e: Exception) {
        Timber.e(e, "Error in map transformation")
        Result.Error(AppException.mapException(e))
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
