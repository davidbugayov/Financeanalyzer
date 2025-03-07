package com.davidbugayov.financeanalyzer.domain.util

import com.davidbugayov.financeanalyzer.domain.model.AppException
import com.davidbugayov.financeanalyzer.domain.model.Result

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