package com.davidbugayov.financeanalyzer.domain.model

import timber.log.Timber
import java.io.IOException

/**
 * Безопасно выполняет блок кода и возвращает Result
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Timber.e(e, "Error in safeCall")
    Result.Error(mapException(e))
}

/**
 * Преобразует исключения в AppException
 */
fun mapException(e: Exception): AppException = when (e) {
    is IOException -> AppException.FileSystem.ReadError(cause = e)
    is IllegalArgumentException -> AppException.Data.ValidationError(e.message)
    is IllegalStateException -> AppException.Business.InvalidOperation(e.message ?: "Недопустимая операция")
    is AppException -> e
    else -> {
        Timber.e(e, "Unmapped exception")
        AppException.Unknown(cause = e)
    }
}

/**
 * Обрабатывает Result и возвращает значение или выбрасывает исключение
 */
fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw exception
}

/**
 * Обрабатывает Result и возвращает значение или null
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> {
        Timber.e(exception, "Error in getOrNull")
        null
    }
}

/**
 * Обрабатывает Result и возвращает значение или значение по умолчанию
 */
fun <T> Result<T>.getOrDefault(defaultValue: T): T = when (this) {
    is Result.Success -> data
    is Result.Error -> {
        Timber.e(exception, "Error in getOrDefault")
        defaultValue
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
 * Выполняет действие в случае успеха
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Выполняет действие в случае ошибки
 */
inline fun <T> Result<T>.onFailure(action: (AppException) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

/**
 * Выполняет действие в любом случае
 */
inline fun <T> Result<T>.onComplete(action: () -> Unit): Result<T> {
    action()
    return this
}

/**
 * Преобразует результат с помощью функции fold
 */
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (AppException) -> R
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onFailure(exception)
} 