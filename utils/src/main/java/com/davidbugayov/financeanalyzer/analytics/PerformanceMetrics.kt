package com.davidbugayov.financeanalyzer.analytics

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Utility class for tracking performance metrics of key user scenarios.
 * Uses the analytics system to report metrics.
 */
object PerformanceMetrics {
    private const val TAG = "PerformanceMetrics"
    private val operationTimers = ConcurrentHashMap<String, Long>()
    private val operationCounters = ConcurrentHashMap<String, Int>()
    private val screenLoadTimers = ConcurrentHashMap<String, Long>()
    private val dbOperationTimers = ConcurrentHashMap<String, Long>()
    private val networkOperationTimers = ConcurrentHashMap<String, Long>()
    private val renderTimers = ConcurrentHashMap<String, Long>()
    
    // Пороговые значения для предупреждений (в миллисекундах)
    private const val SCREEN_LOAD_WARNING_THRESHOLD = 500L
    private const val DB_OPERATION_WARNING_THRESHOLD = 100L
    private const val NETWORK_OPERATION_WARNING_THRESHOLD = 1000L
    private const val RENDER_WARNING_THRESHOLD = 16L // ~60 FPS
    private const val OPERATION_WARNING_THRESHOLD = 500L

    /**
     * Start timing an operation
     * @param operationName Name of the operation to time
     */
    fun startOperation(operationName: String) {
        operationTimers[operationName] = SystemClock.elapsedRealtime()
        Timber.d("Started timing operation: $operationName")
    }

    /**
     * End timing an operation and report the duration
     * @param operationName Name of the operation that was timed
     * @param additionalParams Additional parameters to include in the analytics event
     */
    fun endOperation(operationName: String, additionalParams: Map<String, Any> = emptyMap()) {
        val startTime = operationTimers.remove(operationName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for operation that wasn't started: $operationName")
            return
        }

        val duration = SystemClock.elapsedRealtime() - startTime
        
        // Увеличиваем счетчик операций
        operationCounters[operationName] = (operationCounters[operationName] ?: 0) + 1
        
        // Логируем предупреждение, если операция заняла слишком много времени
        if (duration > OPERATION_WARNING_THRESHOLD) {
            Timber.w("Operation $operationName took $duration ms, which exceeds the warning threshold of $OPERATION_WARNING_THRESHOLD ms")
        }
        
        Timber.d("Operation $operationName completed in $duration ms")

        // Report to analytics
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.DURATION_MS, duration)
            putString(AnalyticsConstants.Params.OPERATION_NAME, operationName)
            putInt(AnalyticsConstants.Params.FEATURE_USAGE_COUNT, operationCounters[operationName] ?: 1)
            
            // Add any additional parameters
            additionalParams.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.PERFORMANCE_METRIC, params)
    }

    /**
     * Track a specific user action with its duration
     * @param actionName Name of the user action
     * @param durationMs Duration of the action in milliseconds
     * @param additionalParams Additional parameters to include in the analytics event
     */
    fun trackAction(actionName: String, durationMs: Long, additionalParams: Map<String, Any> = emptyMap()) {
        Timber.d("Action $actionName took $durationMs ms")
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
            putString(AnalyticsConstants.Params.ACTION_NAME, actionName)
            
            // Add any additional parameters
            additionalParams.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.USER_ACTION, params)
    }

    /**
     * Track a screen load time
     * @param screenName Name of the screen
     * @param durationMs Time taken to load the screen in milliseconds
     */
    fun trackScreenLoad(screenName: String, durationMs: Long) {
        // Логируем предупреждение, если загрузка экрана заняла слишком много времени
        if (durationMs > SCREEN_LOAD_WARNING_THRESHOLD) {
            Timber.w("Screen $screenName loaded in $durationMs ms, which exceeds the warning threshold of $SCREEN_LOAD_WARNING_THRESHOLD ms")
        }
        
        Timber.d("Screen $screenName loaded in $durationMs ms")
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
            putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.SCREEN_LOAD, params)
    }
    
    /**
     * Start timing a screen load
     * @param screenName Name of the screen to time
     */
    fun startScreenLoadTiming(screenName: String) {
        screenLoadTimers[screenName] = SystemClock.elapsedRealtime()
        Timber.d("Started timing screen load: $screenName")
    }
    
    /**
     * End timing a screen load and report the duration
     * @param screenName Name of the screen that was timed
     */
    fun endScreenLoadTiming(screenName: String) {
        val startTime = screenLoadTimers.remove(screenName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for screen that wasn't started: $screenName")
            return
        }
        
        val duration = SystemClock.elapsedRealtime() - startTime
        trackScreenLoad(screenName, duration)
    }
    
    /**
     * Start timing a database operation
     * @param operationName Name of the database operation to time
     */
    fun startDbOperation(operationName: String) {
        dbOperationTimers[operationName] = SystemClock.elapsedRealtime()
        Timber.d("Started timing DB operation: $operationName")
    }
    
    /**
     * End timing a database operation and report the duration
     * @param operationName Name of the database operation that was timed
     * @param queryCount Number of queries executed (optional)
     */
    fun endDbOperation(operationName: String, queryCount: Int = 1) {
        val startTime = dbOperationTimers.remove(operationName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for DB operation that wasn't started: $operationName")
            return
        }
        
        val duration = SystemClock.elapsedRealtime() - startTime
        
        // Логируем предупреждение, если операция с БД заняла слишком много времени
        if (duration > DB_OPERATION_WARNING_THRESHOLD) {
            Timber.w("DB operation $operationName took $duration ms, which exceeds the warning threshold of $DB_OPERATION_WARNING_THRESHOLD ms")
        }
        
        Timber.d("DB operation $operationName completed in $duration ms with $queryCount queries")
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.DURATION_MS, duration)
            putString(AnalyticsConstants.Params.OPERATION_NAME, operationName)
            putInt(AnalyticsConstants.Params.DB_QUERY_COUNT, queryCount)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.DATABASE_OPERATION, params)
    }
    
    /**
     * Start timing a network operation
     * @param operationName Name of the network operation to time
     */
    fun startNetworkOperation(operationName: String) {
        networkOperationTimers[operationName] = SystemClock.elapsedRealtime()
        Timber.d("Started timing network operation: $operationName")
    }
    
    /**
     * End timing a network operation and report the duration
     * @param operationName Name of the network operation that was timed
     * @param bytesSent Number of bytes sent (optional)
     * @param bytesReceived Number of bytes received (optional)
     */
    fun endNetworkOperation(operationName: String, bytesSent: Long = 0, bytesReceived: Long = 0) {
        val startTime = networkOperationTimers.remove(operationName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for network operation that wasn't started: $operationName")
            return
        }
        
        val duration = SystemClock.elapsedRealtime() - startTime
        
        // Логируем предупреждение, если сетевая операция заняла слишком много времени
        if (duration > NETWORK_OPERATION_WARNING_THRESHOLD) {
            Timber.w("Network operation $operationName took $duration ms, which exceeds the warning threshold of $NETWORK_OPERATION_WARNING_THRESHOLD ms")
        }
        
        Timber.d("Network operation $operationName completed in $duration ms, sent: $bytesSent bytes, received: $bytesReceived bytes")
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.DURATION_MS, duration)
            putString(AnalyticsConstants.Params.OPERATION_NAME, operationName)
            putLong("bytes_sent", bytesSent)
            putLong("bytes_received", bytesReceived)
        }
        
        AnalyticsUtils.logEvent("network_operation", params)
    }
    
    /**
     * Start timing a render operation
     * @param viewName Name of the view to time
     */
    fun startRenderTiming(viewName: String) {
        renderTimers[viewName] = SystemClock.elapsedRealtime()
        Timber.d("Started timing render: $viewName")
    }
    
    /**
     * End timing a render operation and report the duration
     * @param viewName Name of the view that was timed
     */
    fun endRenderTiming(viewName: String) {
        val startTime = renderTimers.remove(viewName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for render that wasn't started: $viewName")
            return
        }
        
        val duration = SystemClock.elapsedRealtime() - startTime
        
        // Логируем предупреждение, если рендеринг занял слишком много времени
        if (duration > RENDER_WARNING_THRESHOLD) {
            Timber.w("Render $viewName took $duration ms, which exceeds the warning threshold of $RENDER_WARNING_THRESHOLD ms")
        }
        
        Timber.d("Render $viewName completed in $duration ms")
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.DURATION_MS, duration)
            putString("view_name", viewName)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.RENDER_TIME, params)
    }
    
    /**
     * Track memory usage of the application
     * Примечание: требуется Context, поэтому этот метод нужно вызывать из Activity или Service
     */
    fun trackMemoryUsage(usedMemoryMB: Long, totalMemoryMB: Long, availableMemoryMB: Long) {
        val percentUsed = (usedMemoryMB.toFloat() / totalMemoryMB.toFloat()) * 100
        
        Timber.d("Memory usage: $usedMemoryMB MB / $totalMemoryMB MB ($percentUsed%)")
        
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.MEMORY_USAGE_MB, usedMemoryMB)
            putLong(AnalyticsConstants.Params.MEMORY_TOTAL, totalMemoryMB)
            putLong(AnalyticsConstants.Params.MEMORY_AVAILABLE, availableMemoryMB)
            putFloat("memory_percent_used", percentUsed)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.MEMORY_USAGE, params)
    }
    
    /**
     * Track a background task
     * @param taskName Name of the background task
     * @param durationMs Duration of the task in milliseconds
     * @param result Result of the task (success, failure, etc.)
     */
    fun trackBackgroundTask(taskName: String, durationMs: Long, result: String) {
        Timber.d("Background task $taskName completed in $durationMs ms with result: $result")
        
        val params = Bundle().apply {
            putString("task_name", taskName)
            putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
            putString(AnalyticsConstants.Params.FEATURE_RESULT, result)
        }
        
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.BACKGROUND_TASK, params)
    }
    
    /**
     * Track frame rate metrics
     * @param fps Frames per second
     * @param droppedFrames Number of dropped frames
     * @param screenName Name of the screen being rendered
     */
    fun trackFrameMetrics(fps: Float, droppedFrames: Int, screenName: String) {
        Timber.d("Frame metrics: $fps FPS, $droppedFrames dropped frames on screen $screenName")
        
        val params = Bundle().apply {
            putFloat(AnalyticsConstants.Params.FRAME_RATE, fps)
            putInt(AnalyticsConstants.Params.FRAME_DROP_COUNT, droppedFrames)
            putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
        }
        
        AnalyticsUtils.logEvent("frame_metrics", params)
    }

    // Common operation names
    object Operations {
        const val APP_STARTUP = "app_startup"
        const val DATABASE_LOAD = "database_load"
        const val TRANSACTION_ADD = "transaction_add"
        const val TRANSACTION_EDIT = "transaction_edit"
        const val TRANSACTION_LOAD = "transaction_load"
        const val STATISTICS_CALCULATION = "statistics_calculation"
        const val REPORT_GENERATION = "report_generation"
        const val EXPORT_DATA = "export_data"
        const val IMPORT_DATA = "import_data"
        const val CATEGORY_LOAD = "category_load"
        const val USER_DATA_LOAD = "user_data_load"
        const val SETTINGS_LOAD = "settings_load"
        const val BUDGET_CALCULATION = "budget_calculation"
        const val CHART_RENDERING = "chart_rendering"
        const val WIDGET_UPDATE = "widget_update"
        const val SEARCH_OPERATION = "search_operation"
        const val FILTER_OPERATION = "filter_operation"
        const val SYNC_OPERATION = "sync_operation"
        const val BACKUP_OPERATION = "backup_operation"
        const val RESTORE_OPERATION = "restore_operation"
    }

    // Common action names
    object Actions {
        const val BUTTON_CLICK = "button_click"
        const val FORM_SUBMIT = "form_submit"
        const val LIST_SCROLL = "list_scroll"
        const val FILTER_APPLY = "filter_apply"
        const val SEARCH = "search"
        const val SWIPE = "swipe"
        const val LONG_PRESS = "long_press"
        const val DOUBLE_TAP = "double_tap"
        const val DRAG_DROP = "drag_drop"
        const val PINCH_ZOOM = "pinch_zoom"
        const val DIALOG_OPEN = "dialog_open"
        const val DIALOG_CLOSE = "dialog_close"
        const val MENU_OPEN = "menu_open"
        const val MENU_ITEM_SELECT = "menu_item_select"
        const val TAB_SWITCH = "tab_switch"
        const val NAVIGATION = "navigation"
        const val REFRESH_PULL = "refresh_pull"
        const val KEYBOARD_OPEN = "keyboard_open"
        const val TEXT_INPUT = "text_input"
        const val DATE_SELECT = "date_select"
    }
    
    // Common screen names
    object Screens {
        const val HOME = "home_screen"
        const val TRANSACTION_ADD = "transaction_add_screen"
        const val TRANSACTION_EDIT = "transaction_edit_screen"
        const val TRANSACTION_DETAILS = "transaction_details_screen"
        const val CATEGORY_LIST = "category_list_screen"
        const val CATEGORY_EDIT = "category_edit_screen"
        const val STATISTICS = "statistics_screen"
        const val REPORTS = "reports_screen"
        const val SETTINGS = "settings_screen"
        const val PROFILE = "profile_screen"
        const val BUDGET = "budget_screen"
        const val SEARCH_RESULTS = "search_results_screen"
        const val EXPORT_IMPORT = "export_import_screen"
        const val ONBOARDING = "onboarding_screen"
    }
} 