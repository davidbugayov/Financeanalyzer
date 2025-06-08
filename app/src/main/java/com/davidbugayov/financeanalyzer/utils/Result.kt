package com.davidbugayov.financeanalyzer.utils

/**
 * Класс для обработки результатов операций с данными
 * Может быть в состоянии успеха с данными или ошибки с исключением
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Exception): Result<Nothing> = Error(exception)
        fun error(message: String): Result<Nothing> = Error(Exception(message))
    }

    /**
     * Выполняет действие в зависимости от результата
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Выполняет действие в случае ошибки
     */
    inline fun onError(action: (Exception) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Преобразует данные в случае успеха
     */
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
        }
    }

    /**
     * Проверяет, является ли результат успешным
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Проверяет, является ли результат ошибкой
     */
    fun isError(): Boolean = this is Error

    /**
     * Получает данные или выбрасывает исключение в случае ошибки
     */
    fun getOrThrow(): T {
        when (this) {
            is Success -> return data
            is Error -> throw exception
        }
    }

    /**
     * Получает данные или null в случае ошибки
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            is Error -> null
        }
    }

    /**
     * Получает данные или значение по умолчанию в случае ошибки
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T {
        return when (this) {
            is Success -> data
            is Error -> defaultValue
        }
    }
}
