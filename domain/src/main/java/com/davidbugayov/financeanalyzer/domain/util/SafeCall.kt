package com.davidbugayov.financeanalyzer.domain.util

import com.davidbugayov.financeanalyzer.domain.model.AppException

suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(mapException(e))
    }
}

private fun mapException(e: Exception): AppException {
    return when (e) {
        is AppException -> e
        else -> AppException.Unknown(cause = e)
    }
}

/**
 * Синхронная версия safeCall для использования в не-suspend контексте.
 * Оборачивает выполнение блока кода в try-catch, возвращая Result.Success или Result.Error.
 *
 * @param block Блок кода для выполнения
 * @return Result.Success с результатом выполнения блока или Result.Error с AppException
 */
fun <T> safeCallSync(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(mapException(e))
    }
}
