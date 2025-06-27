package com.davidbugayov.financeanalyzer.utils.logging

import kotlin.Any
import kotlin.String
import timber.log.Timber

class TimberLogger : Logger {
    override fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }

    override fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }

    override fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }

    override fun e(message: String, vararg args: Any?) {
        Timber.e(message, *args)
    }
} 