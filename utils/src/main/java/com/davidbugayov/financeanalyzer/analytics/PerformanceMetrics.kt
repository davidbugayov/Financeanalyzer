package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import android.os.SystemClock
import java.util.concurrent.ConcurrentHashMap
import timber.log.Timber

/**
 * Класс для отслеживания и анализа производительности приложения.
 * Собирает метрики о времени выполнения операций, загрузке экранов,
 * использовании памяти и т.д.
 */
object PerformanceMetrics {
    private val operationTimers = ConcurrentHashMap<String, Long>()
    private val screenLoadTimers = ConcurrentHashMap<String, Long>()
    private val dbOperationTimers = ConcurrentHashMap<String, Long>()
    private val networkCallTimers = ConcurrentHashMap<String, Long>()

    private const val SCREEN_LOAD_WARNING_THRESHOLD = 500L // миллисекунды
    private const val DB_OPERATION_WARNING_THRESHOLD = 100L // миллисекунды
    private const val NETWORK_CALL_WARNING_THRESHOLD = 1000L // миллисекунды

    // Имена экранов для отслеживания
    object Screens {
        const val HOME = "home_screen"
        const val PROFILE = "profile_screen"
        const val ADD_TRANSACTION = "add_transaction_screen"
        const val EDIT_TRANSACTION = "edit_transaction_screen"
        const val TRANSACTION_HISTORY = "transaction_history_screen"
        const val TRANSACTION_DETAILS = "transaction_details_screen"
        const val STATISTICS = "statistics_screen"
        const val BUDGET = "budget_screen"
        const val SETTINGS = "settings_screen"
        const val CATEGORIES = "categories_screen"
        const val EXPORT_IMPORT = "export_import_screen"
        const val ACHIEVEMENTS = "achievements_screen"
    }

    /**
     * Начать отслеживание времени выполнения операции
     * @param operationName Название операции
     */
    fun startOperation(operationName: String) {
        operationTimers[operationName] = SystemClock.elapsedRealtime()
        Timber.d("Started timing operation: $operationName")
    }

    /**
     * Завершить отслеживание времени выполнения операции
     * @param operationName Название операции
     * @return Время выполнения в миллисекундах или -1, если операция не была начата
     */
    fun endOperation(operationName: String): Long {
        val startTime = operationTimers.remove(operationName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for operation that wasn't started: $operationName")
            return -1
        }

        val duration = SystemClock.elapsedRealtime() - startTime
        Timber.d("Operation $operationName took $duration ms")

        val params =
            Bundle().apply {
                putLong(AnalyticsConstants.Params.DURATION_MS, duration)
                putString(AnalyticsConstants.Params.OPERATION_NAME, operationName)
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.OPERATION_COMPLETED, params)

        return duration
    }

    /**
     * Отслеживать время выполнения блока кода
     * @param operationName Название операции
     * @param block Блок кода для выполнения
     * @return Результат выполнения блока
     */
    inline fun <T> trackOperation(
        operationName: String,
        block: () -> T,
    ): T {
        startOperation(operationName)
        try {
            return block()
        } finally {
            endOperation(operationName)
        }
    }

    /**
     * Отслеживать действие пользователя с его длительностью
     * @param actionName Название действия пользователя
     * @param durationMs Длительность действия в миллисекундах
     * @param additionalParams Дополнительные параметры
     */
    fun trackAction(
        actionName: String,
        durationMs: Long,
        additionalParams: Map<String, Any> = emptyMap(),
    ) {
        Timber.d("Action $actionName took $durationMs ms")

        val params =
            Bundle().apply {
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
    fun trackScreenLoad(
        screenName: String,
        durationMs: Long,
    ) {
        // Логируем предупреждение, если загрузка экрана заняла слишком много времени
        if (durationMs > SCREEN_LOAD_WARNING_THRESHOLD) {
            Timber.w(
                "Screen $screenName loaded in $durationMs ms, which exceeds the warning threshold of $SCREEN_LOAD_WARNING_THRESHOLD ms",
            )
        }

        Timber.d("Screen $screenName loaded in $durationMs ms")

        val params =
            Bundle().apply {
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
     */
    fun endDbOperation(operationName: String) {
        val startTime = dbOperationTimers.remove(operationName)
        if (startTime == null) {
            Timber.w("Attempted to end timing for DB operation that wasn't started: $operationName")
            return
        }

        val duration = SystemClock.elapsedRealtime() - startTime

        // Логируем предупреждение, если операция с БД заняла слишком много времени
        if (duration > DB_OPERATION_WARNING_THRESHOLD) {
            Timber.w(
                "DB operation $operationName took $duration ms, which exceeds the warning threshold of $DB_OPERATION_WARNING_THRESHOLD ms",
            )
        }

        Timber.d("DB operation $operationName took $duration ms")

        val params =
            Bundle().apply {
                putLong(AnalyticsConstants.Params.DURATION_MS, duration)
                putString(AnalyticsConstants.Params.OPERATION_NAME, operationName)
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.DATABASE_OPERATION, params)
    }

    /**
     * Track a database operation with its duration
     * @param operationName Name of the database operation
     * @param durationMs Duration of the operation in milliseconds
     */
    fun trackDbOperation(
        operationName: String,
        durationMs: Long,
    ) {
        // Логируем предупреждение, если операция с БД заняла слишком много времени
        if (durationMs > DB_OPERATION_WARNING_THRESHOLD) {
            Timber.w(
                "DB operation $operationName took $durationMs ms, which exceeds the warning threshold of $DB_OPERATION_WARNING_THRESHOLD ms",
            )
        }

        Timber.d("DB operation $operationName took $durationMs ms")

        val params =
            Bundle().apply {
                putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
                putString(AnalyticsConstants.Params.OPERATION_NAME, operationName)
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.DATABASE_OPERATION, params)
    }

    /**
     * Time a database operation block
     * @param operationName Name of the database operation
     * @param block Block of code to time
     * @return Result of the block
     */
    inline fun <T> trackDbOperation(
        operationName: String,
        block: () -> T,
    ): T {
        startDbOperation(operationName)
        try {
            return block()
        } finally {
            endDbOperation(operationName)
        }
    }

    /**
     * Start timing a network call
     * @param url URL of the network call to time
     */
    fun startNetworkCall(url: String) {
        networkCallTimers[url] = SystemClock.elapsedRealtime()
        Timber.d("Started timing network call to: $url")
    }

    /**
     * End timing a network call and report the duration
     * @param url URL of the network call that was timed
     * @param statusCode HTTP status code of the response
     */
    fun endNetworkCall(
        url: String,
        statusCode: Int,
    ) {
        val startTime = networkCallTimers.remove(url)
        if (startTime == null) {
            Timber.w("Attempted to end timing for network call that wasn't started: $url")
            return
        }

        val duration = SystemClock.elapsedRealtime() - startTime

        // Логируем предупреждение, если сетевой запрос занял слишком много времени
        if (duration > NETWORK_CALL_WARNING_THRESHOLD) {
            Timber.w(
                "Network call to $url took $duration ms, which exceeds the warning threshold of $NETWORK_CALL_WARNING_THRESHOLD ms",
            )
        }

        Timber.d("Network call to $url took $duration ms with status code $statusCode")

        val params =
            Bundle().apply {
                putLong(AnalyticsConstants.Params.DURATION_MS, duration)
                putString("url", url)
                putInt("status_code", statusCode)
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.NETWORK_CALL, params)
    }

    /**
     * Track a network call with its duration
     * @param url URL of the network call
     * @param durationMs Duration of the call in milliseconds
     * @param statusCode HTTP status code of the response
     */
    fun trackNetworkCall(
        url: String,
        durationMs: Long,
        statusCode: Int,
    ) {
        // Логируем предупреждение, если сетевой запрос занял слишком много времени
        if (durationMs > NETWORK_CALL_WARNING_THRESHOLD) {
            Timber.w(
                "Network call to $url took $durationMs ms, which exceeds the warning threshold of $NETWORK_CALL_WARNING_THRESHOLD ms",
            )
        }

        Timber.d("Network call to $url took $durationMs ms with status code $statusCode")

        val params =
            Bundle().apply {
                putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
                putString("url", url)
                putInt("status_code", statusCode)
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.NETWORK_CALL, params)
    }

    /**
     * Track memory usage
     * @param usedMemoryMB Used memory in MB
     * @param totalMemoryMB Total memory in MB
     * @param availableMemoryMB Available memory in MB
     */
    fun trackMemoryUsage(
        usedMemoryMB: Long,
        totalMemoryMB: Long,
        availableMemoryMB: Long,
    ) {
        val percentUsed = (usedMemoryMB.toFloat() / totalMemoryMB.toFloat()) * 100

        Timber.d("Memory usage: $usedMemoryMB MB / $totalMemoryMB MB ($percentUsed%)")

        val params =
            Bundle().apply {
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
    fun trackBackgroundTask(
        taskName: String,
        durationMs: Long,
        result: String,
    ) {
        Timber.d("Background task $taskName completed in $durationMs ms with result: $result")

        val params =
            Bundle().apply {
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
    fun trackFrameMetrics(
        fps: Float,
        droppedFrames: Int,
        screenName: String,
    ) {
        Timber.d("Frame metrics: $fps FPS, $droppedFrames dropped frames on screen $screenName")

        val params =
            Bundle().apply {
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
        const val SWIPE = "swipe"
        const val NAVIGATION = "navigation"
        const val SELECTION = "selection"
        const val SEARCH = "search"
        const val FILTER = "filter"
        const val SORT = "sort"
        const val EDIT = "edit"
        const val DELETE = "delete"
        const val SHARE = "share"
        const val SAVE = "save"
        const val CANCEL = "cancel"
        const val CONFIRM = "confirm"
        const val TOGGLE = "toggle"
        const val REFRESH = "refresh"
        const val SCROLL = "scroll"
        const val ZOOM = "zoom"
        const val LONG_PRESS = "long_press"
        const val DRAG = "drag"
        const val DROP = "drop"
    }
}
