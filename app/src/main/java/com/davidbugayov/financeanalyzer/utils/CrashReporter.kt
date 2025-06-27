package com.davidbugayov.financeanalyzer.utils

import android.app.Application
import android.os.Build
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Centralized crash reporting system that captures uncaught exceptions
 * and reports them through the analytics system.
 */
object CrashReporter {
    private const val MAX_STACK_TRACE_LENGTH = 4000 // Limit stack trace length for analytics

    /**
     * Initialize the crash reporter
     * @param application Application instance
     */
    fun init(application: Application) {
        // Preserve existing handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Log detailed crash information
                logCrash(throwable, application)
            } catch (e: Exception) {
                // If crash reporting fails, log the error but don't crash again
                Timber.e(e, "Error in crash reporting")
            } finally {
                // Always delegate to default handler
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
        
        Timber.d("CrashReporter initialized")
    }
    
    /**
     * Log a non-fatal error that doesn't crash the app
     * @param throwable The error to log
     * @param message Optional message providing context about the error
     */
    fun logError(throwable: Throwable, message: String = "") {
        Timber.e(throwable, message)
        
        val stackTrace = getStackTraceString(throwable)
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"
        
        // Report to analytics
        val params = android.os.Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, "$message: $errorMessage")
            putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
            putString(AnalyticsConstants.Params.IS_FATAL, "false")
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.APP_ERROR, params)
    }
    
    /**
     * Log a handled exception with custom parameters
     * @param throwable The exception to log
     * @param customParams Additional parameters to include with the error report
     */
    fun logException(throwable: Throwable, customParams: Map<String, String> = emptyMap()) {
        Timber.e(throwable)
        
        val stackTrace = getStackTraceString(throwable)
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"
        
        // Report to analytics
        val params = android.os.Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
            putString(AnalyticsConstants.Params.IS_FATAL, "false")
            
            // Add custom parameters
            customParams.forEach { (key, value) ->
                putString(key, value)
            }
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.APP_EXCEPTION, params)
    }
    
    /**
     * Log details about a crash
     */
    private fun logCrash(throwable: Throwable, application: Application) {
        val stackTrace = getStackTraceString(throwable)
        val errorType = throwable::class.java.simpleName
        val errorMessage = throwable.message ?: "No message"
        
        Timber.e(throwable, "FATAL CRASH: $errorType - $errorMessage")
        
        // Report to analytics
        val params = android.os.Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.STACK_TRACE, stackTrace)
            putString(AnalyticsConstants.Params.IS_FATAL, "true")
            putString(AnalyticsConstants.Params.APP_VERSION, BuildConfig.VERSION_NAME)
            putInt(AnalyticsConstants.Params.APP_VERSION_CODE, BuildConfig.VERSION_CODE)
            putString(AnalyticsConstants.Params.DEVICE_MODEL, "${Build.MANUFACTURER} ${Build.MODEL}")
            putString(AnalyticsConstants.Params.ANDROID_VERSION, "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.APP_CRASH, params)
    }
    
    /**
     * Convert a Throwable to a string representation of its stack trace
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        var stackTrace = sw.toString()
        
        // Limit stack trace length for analytics
        if (stackTrace.length > MAX_STACK_TRACE_LENGTH) {
            stackTrace = stackTrace.substring(0, MAX_STACK_TRACE_LENGTH) + "... (truncated)"
        }
        
        return stackTrace
    }
} 