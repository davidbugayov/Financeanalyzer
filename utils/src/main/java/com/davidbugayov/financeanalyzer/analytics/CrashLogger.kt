package com.davidbugayov.financeanalyzer.analytics

interface CrashLogger {
    fun logException(throwable: Throwable)
    fun logDatabaseError(operation: String, errorMessage: String, throwable: Throwable? = null)
} 