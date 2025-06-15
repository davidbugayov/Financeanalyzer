package com.davidbugayov.financeanalyzer.domain.util

/**
 * Класс для представления результата операции
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success

    fun exceptionOrNull(): Exception? = if (this is Error) exception else null
} 