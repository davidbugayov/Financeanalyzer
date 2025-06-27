package com.davidbugayov.financeanalyzer.utils.logging

import kotlin.String
import kotlin.Any

interface Logger {
    fun d(message: String, vararg args: Any?)
    fun i(message: String, vararg args: Any?)
    fun w(message: String, vararg args: Any?)
    fun e(message: String, vararg args: Any?)
} 