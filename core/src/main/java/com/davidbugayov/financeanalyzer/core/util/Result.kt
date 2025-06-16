package com.davidbugayov.financeanalyzer.core.util

import com.davidbugayov.financeanalyzer.core.model.AppException

/**
 * Sealed класс для представления результата операции
 */
sealed class Result<out T> {

    /**
     * Успешный результат операции
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Ошибка операции
     */
    data class Error(val exception: AppException) : Result<Nothing>()

    companion object {

        /**
         * Создает успешный результат
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Создает результат с ошибкой
         */
        fun error(exception: AppException): Result<Nothing> = Error(exception)

        /**
         * Создает результат из блока кода
         */
        inline fun <T> of(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(AppException.mapException(e))
        }
    }
}
