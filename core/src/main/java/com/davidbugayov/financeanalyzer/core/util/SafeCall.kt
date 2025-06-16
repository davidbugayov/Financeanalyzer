package com.davidbugayov.financeanalyzer.core.util

import com.davidbugayov.financeanalyzer.core.model.AppException

suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(AppException.mapException(e))
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
        Result.Error(AppException.mapException(e))
    }
}
