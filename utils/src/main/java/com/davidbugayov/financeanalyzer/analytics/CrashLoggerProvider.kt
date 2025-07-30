package com.davidbugayov.financeanalyzer.analytics

object CrashLoggerProvider {
    var crashLogger: CrashLogger =
        object : CrashLogger {
            override fun logException(throwable: Throwable) { /* no-op */ }

            override fun logDatabaseError(
                operation: String,
                errorMessage: String,
                throwable: Throwable?,
            ) { /* no-op */ }
        }
} 
