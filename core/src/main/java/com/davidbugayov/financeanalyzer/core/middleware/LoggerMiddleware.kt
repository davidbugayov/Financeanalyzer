package com.davidbugayov.financeanalyzer.core.middleware

import timber.log.Timber

/**
 * Middleware interface for logging operations.
 * Provides centralized logging with different levels and contexts.
 */
interface LoggerMiddleware {
    /**
     * Log verbose message
     */
    fun v(
        message: String,
        vararg args: Any?,
    )

    /**
     * Log verbose message with throwable
     */
    fun v(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    )

    /**
     * Log debug message
     */
    fun d(
        message: String,
        vararg args: Any?,
    )

    /**
     * Log debug message with throwable
     */
    fun d(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    )

    /**
     * Log info message
     */
    fun i(
        message: String,
        vararg args: Any?,
    )

    /**
     * Log info message with throwable
     */
    fun i(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    )

    /**
     * Log warning message
     */
    fun w(
        message: String,
        vararg args: Any?,
    )

    /**
     * Log warning message with throwable
     */
    fun w(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    )

    /**
     * Log error message
     */
    fun e(
        message: String,
        vararg args: Any?,
    )

    /**
     * Log error message with throwable
     */
    fun e(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    )

    /**
     * Log wtf (what a terrible failure) message
     */
    fun wtf(
        message: String,
        vararg args: Any?,
    )

    /**
     * Log wtf message with throwable
     */
    fun wtf(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    )

    /**
     * Log performance timing
     */
    fun logTiming(
        operation: String,
        durationMs: Long,
        context: String? = null,
    )

    /**
     * Log user action
     */
    fun logUserAction(
        action: String,
        context: String? = null,
        metadata: Map<String, Any>? = null,
    )

    /**
     * Log feature usage
     */
    fun logFeatureUsage(
        feature: String,
        context: String? = null,
    )

    /**
     * Log error with context
     */
    fun logError(
        error: Throwable,
        context: String? = null,
        metadata: Map<String, Any>? = null,
    )
}

/**
 * Default implementation using Timber
 */
class DefaultLoggerMiddleware : LoggerMiddleware {

    override fun v(
        message: String,
        vararg args: Any?,
    ) {
        Timber.v(message, *args)
    }

    override fun v(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        Timber.v(t, message, *args)
    }

    override fun d(
        message: String,
        vararg args: Any?,
    ) {
        Timber.d(message, *args)
    }

    override fun d(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        Timber.d(t, message, *args)
    }

    override fun i(
        message: String,
        vararg args: Any?,
    ) {
        Timber.i(message, *args)
    }

    override fun i(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        Timber.i(t, message, *args)
    }

    override fun w(
        message: String,
        vararg args: Any?,
    ) {
        Timber.w(message, *args)
    }

    override fun w(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        Timber.w(t, message, *args)
    }

    override fun e(
        message: String,
        vararg args: Any?,
    ) {
        Timber.e(message, *args)
    }

    override fun e(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        Timber.e(t, message, *args)
    }

    override fun wtf(
        message: String,
        vararg args: Any?,
    ) {
        Timber.wtf(message, *args)
    }

    override fun wtf(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        Timber.wtf(t, message, *args)
    }

    override fun logTiming(
        operation: String,
        durationMs: Long,
        context: String?,
    ) {
        val contextMsg = context?.let { " [$it]" } ?: ""
        Timber.i("TIMING: $operation took ${durationMs}ms$contextMsg")
    }

    override fun logUserAction(
        action: String,
        context: String?,
        metadata: Map<String, Any>?,
    ) {
        val contextMsg = context?.let { " in $it" } ?: ""
        val metadataMsg = metadata?.let { " with metadata: $it" } ?: ""
        Timber.i("USER_ACTION: $action$contextMsg$metadataMsg")
    }

    override fun logFeatureUsage(
        feature: String,
        context: String?,
    ) {
        val contextMsg = context?.let { " in $it" } ?: ""
        Timber.i("FEATURE_USAGE: $feature$contextMsg")
    }

    override fun logError(
        error: Throwable,
        context: String?,
        metadata: Map<String, Any>?,
    ) {
        val contextMsg = context?.let { " in $it" } ?: ""
        val metadataMsg = metadata?.let { " with metadata: $it" } ?: ""
        Timber.e(error, "ERROR: ${error.message}$contextMsg$metadataMsg")
    }
}

/**
 * No-op implementation for cases when logging is disabled
 */
class NoOpLoggerMiddleware : LoggerMiddleware {

    override fun v(
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun v(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun d(
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun d(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun i(
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun i(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun w(
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun w(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun e(
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun e(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun wtf(
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun wtf(
        t: Throwable?,
        message: String,
        vararg args: Any?,
    ) {
        // No operation
    }

    override fun logTiming(
        operation: String,
        durationMs: Long,
        context: String?,
    ) {
        // No operation
    }

    override fun logUserAction(
        action: String,
        context: String?,
        metadata: Map<String, Any>?,
    ) {
        // No operation
    }

    override fun logFeatureUsage(
        feature: String,
        context: String?,
    ) {
        // No operation
    }

    override fun logError(
        error: Throwable,
        context: String?,
        metadata: Map<String, Any>?,
    ) {
        // No operation
    }
}

/**
 * Performance monitoring middleware
 */
class PerformanceMiddleware(
    private val logger: LoggerMiddleware,
) {
    companion object {
        private const val PERFORMANCE_TAG = "PERFORMANCE"
    }

    /**
     * Measure execution time of a suspend function
     */
    suspend fun <T> measureTime(
        operation: String,
        context: String? = null,
        block: suspend () -> T,
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logger.logTiming(operation, duration, context)
        }
    }

    /**
     * Measure execution time of a regular function
     */
    fun <T> measureTimeSync(
        operation: String,
        context: String? = null,
        block: () -> T,
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logger.logTiming(operation, duration, context)
        }
    }

    /**
     * Create a performance tracer
     */
    fun createTracer(
        operation: String,
        context: String? = null,
    ): PerformanceTracer {
        return PerformanceTracer(operation, context, logger)
    }
}

/**
 * Performance tracer for manual timing control
 */
class PerformanceTracer(
    private val operation: String,
    private val context: String?,
    private val logger: LoggerMiddleware,
) {
    private var startTime: Long = 0

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun end() {
        if (startTime > 0) {
            val duration = System.currentTimeMillis() - startTime
            logger.logTiming(operation, duration, context)
            startTime = 0
        }
    }

    fun endWithResult(result: String) {
        if (startTime > 0) {
            val duration = System.currentTimeMillis() - startTime
            logger.i("PERFORMANCE: $operation completed with $result in ${duration}ms", context ?: "")
            startTime = 0
        }
    }
}
