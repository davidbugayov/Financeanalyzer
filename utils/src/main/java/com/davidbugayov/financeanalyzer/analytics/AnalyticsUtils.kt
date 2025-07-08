// Copied from app/src/main/java/com/davidbugayov/financeanalyzer/analytics/AnalyticsUtils.kt
package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import com.davidbugayov.financeanalyzer.core.model.Money
import java.util.Date
import java.util.UUID
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * Утилиты для работы с аналитикой
 */
object AnalyticsUtils : KoinComponent {
    private lateinit var analytics: IAnalytics
    private var sessionId: String = UUID.randomUUID().toString()
    private var appOpenTimestamp: Long = 0L

    fun init(analyticsImpl: IAnalytics) {
        analytics = analyticsImpl
        sessionId = UUID.randomUUID().toString()
        appOpenTimestamp = System.currentTimeMillis()
        Timber.d("AnalyticsUtils initialized with ${analyticsImpl.javaClass.simpleName}")
    }

    fun isInitialized(): Boolean = ::analytics.isInitialized

    fun getAnalytics(): IAnalytics {
        if (!isInitialized()) {
            analytics = NoOpAnalytics()
        }
        return analytics
    }

    fun logEvent(eventName: String) {
        if (isInitialized()) analytics.logEvent(eventName)
    }

    fun logEvent(
        eventName: String,
        params: Bundle,
    ) {
        if (isInitialized()) analytics.logEvent(eventName, params)
    }

    fun setUserProperty(
        name: String,
        value: String,
    ) {
        if (isInitialized()) analytics.setUserProperty(name, value)
    }

    fun setUserId(userId: String) {
        if (isInitialized()) analytics.setUserId(userId)
    }

    // Жизненный цикл приложения

    fun logAppOpen() {
        appOpenTimestamp = System.currentTimeMillis()
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.APP_OPEN, params)
        Timber.d("App open logged, session: $sessionId")
    }

    fun logAppClose() {
        val sessionDuration = System.currentTimeMillis() - appOpenTimestamp
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
                putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, sessionDuration)
            }
        logEvent(AnalyticsConstants.Events.APP_CLOSE, params)
        Timber.d("App close logged, session: $sessionId, duration: $sessionDuration ms")
    }

    fun logAppBackground() {
        val sessionDuration = System.currentTimeMillis() - appOpenTimestamp
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
                putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, sessionDuration)
            }
        logEvent(AnalyticsConstants.Events.APP_BACKGROUND, params)
        Timber.d("App background logged, session: $sessionId, duration: $sessionDuration ms")
    }

    fun logAppForeground() {
        appOpenTimestamp = System.currentTimeMillis()
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.APP_FOREGROUND, params)
        Timber.d("App foreground logged, session: $sessionId")
    }

    // Экраны и навигация

    fun logScreenView(
        screenName: String,
        screenClass: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
                putString(AnalyticsConstants.Params.SCREEN_CLASS, screenClass)
                putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            }
        logEvent(AnalyticsConstants.Events.SCREEN_VIEW, params)
        Timber.d("Screen view logged: $screenName ($screenClass)")
    }

    fun logScreenLoad(
        screenName: String,
        durationMs: Long,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
                putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
            }
        logEvent(AnalyticsConstants.Events.SCREEN_LOAD, params)
        Timber.d("Screen load logged: $screenName, duration: $durationMs ms")
    }

    fun logNavigation(
        source: String,
        destination: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SOURCE, source)
                putString(AnalyticsConstants.Params.DESTINATION, destination)
            }
        logEvent("navigation", params)
        Timber.d("Navigation logged: $source -> $destination")
    }

    // Транзакции

    fun logTransactionAdded(
        amount: Money,
        category: String,
        isExpense: Boolean,
        hasDescription: Boolean,
        source: String = AnalyticsConstants.Values.SOURCE_USER,
    ) {
        val params =
            Bundle().apply {
                putString(
                    AnalyticsConstants.Params.TRANSACTION_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
                    },
                )
                putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
                putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
                putBoolean(AnalyticsConstants.Params.HAS_DESCRIPTION, hasDescription)
                putString(AnalyticsConstants.Params.SOURCE, source)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_ADDED, params)
        Timber.d("Transaction added logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    fun logTransactionEdited(
        amount: Money,
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(
                    AnalyticsConstants.Params.TRANSACTION_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
                    },
                )
                putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
                putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EDITED, params)
        Timber.d("Transaction edited logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    fun logTransactionDeleted(
        amount: Money,
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(
                    AnalyticsConstants.Params.TRANSACTION_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
                    },
                )
                putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
                putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_DELETED, params)
        Timber.d("Transaction deleted logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    fun logTransactionFiltered(
        filters: Map<String, String>,
        resultCount: Int,
    ) {
        val params =
            Bundle().apply {
                putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, resultCount)
                filters.forEach { (key, value) ->
                    putString(key, value)
                }
                putBoolean(AnalyticsConstants.Params.FILTER_APPLIED, filters.isNotEmpty())
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_FILTERED, params)
        Timber.d("Transaction filtered logged: ${filters.size} filters, $resultCount results")
    }

    fun logTransactionSearched(
        query: String,
        resultCount: Int,
    ) {
        val params =
            Bundle().apply {
                putString("search_query", query)
                putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, resultCount)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_SEARCHED, params)
        Timber.d("Transaction searched logged: \"$query\", $resultCount results")
    }

    fun logTransactionImported(
        source: String,
        count: Int,
        success: Boolean,
        errorMessage: String? = null,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.IMPORT_SOURCE, source)
                putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, count)
                putString(
                    AnalyticsConstants.Params.FEATURE_RESULT,
                    if (success) AnalyticsConstants.Values.RESULT_SUCCESS else AnalyticsConstants.Values.RESULT_FAILURE,
                )
                errorMessage?.let { putString(AnalyticsConstants.Params.ERROR_MESSAGE, it) }
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_IMPORTED, params)
        Timber.d("Transaction imported logged: $source, $count transactions, success: $success")
    }

    fun logTransactionExportStarted(
        format: String,
        count: Int,
        periodType: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.EXPORT_FORMAT, format)
                putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, count)
                putString(AnalyticsConstants.Params.PERIOD_TYPE, periodType)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EXPORT_STARTED, params)
        Timber.d("Transaction export started: $format, $count transactions, period: $periodType")
    }

    fun logTransactionExportCompleted(
        format: String,
        count: Int,
        durationMs: Long,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.EXPORT_FORMAT, format)
                putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, count)
                putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
                putString(AnalyticsConstants.Params.FEATURE_RESULT, AnalyticsConstants.Values.RESULT_SUCCESS)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EXPORT_COMPLETED, params)
        Timber.d("Transaction export completed: $format, $count transactions, duration: $durationMs ms")
    }

    fun logTransactionExportFailed(
        format: String,
        errorMessage: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.EXPORT_FORMAT, format)
                putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
                putString(AnalyticsConstants.Params.FEATURE_RESULT, AnalyticsConstants.Values.RESULT_FAILURE)
            }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EXPORT_FAILED, params)
        Timber.d("Transaction export failed: $format, error: $errorMessage")
    }

    // Категории

    fun logCategoryAdded(
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
                putString(
                    AnalyticsConstants.Params.CATEGORY_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
                    },
                )
            }
        logEvent(AnalyticsConstants.Events.CATEGORY_ADDED, params)
        Timber.d("Category added: $category (${if (isExpense) "expense" else "income"})")
    }

    fun logCategoryEdited(
        oldCategory: String,
        newCategory: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.CATEGORY_NAME, newCategory)
                putString(AnalyticsConstants.Params.CATEGORY_OLD_NAME, oldCategory)
                putString(
                    AnalyticsConstants.Params.CATEGORY_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
                    },
                )
            }
        logEvent(AnalyticsConstants.Events.CATEGORY_EDITED, params)
        Timber.d("Category edited: $oldCategory -> $newCategory (${if (isExpense) "expense" else "income"})")
    }

    fun logCategoryDeleted(
        category: String,
        isExpense: Boolean,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
                putString(
                    AnalyticsConstants.Params.CATEGORY_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
                    },
                )
            }
        logEvent(AnalyticsConstants.Events.CATEGORY_DELETED, params)
        Timber.d("Category deleted: $category (${if (isExpense) "expense" else "income"})")
    }

    fun logCategorySelected(
        category: String,
        isExpense: Boolean,
        source: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
                putString(
                    AnalyticsConstants.Params.CATEGORY_TYPE,
                    if (isExpense) {
                        AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE
                    } else {
                        AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
                    },
                )
                putString(AnalyticsConstants.Params.SOURCE, source)
            }
        logEvent(AnalyticsConstants.Events.CATEGORY_SELECTED, params)
        Timber.d("Category selected: $category (${if (isExpense) "expense" else "income"}) from $source")
    }

    // Бюджет

    fun logBudgetCreated(
        category: String,
        amount: Money,
        periodType: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.BUDGET_CATEGORY, category)
                putString(AnalyticsConstants.Params.BUDGET_AMOUNT, amount.toString())
                putString(AnalyticsConstants.Params.BUDGET_PERIOD, periodType)
            }
        logEvent(AnalyticsConstants.Events.BUDGET_CREATED, params)
        Timber.d("Budget created: $category, $amount, period: $periodType")
    }

    fun logBudgetUpdated(
        category: String,
        oldAmount: Money,
        newAmount: Money,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.BUDGET_CATEGORY, category)
                putString(AnalyticsConstants.Params.SETTING_PREVIOUS_VALUE, oldAmount.toString())
                putString(AnalyticsConstants.Params.BUDGET_AMOUNT, newAmount.toString())
            }
        logEvent(AnalyticsConstants.Events.BUDGET_UPDATED, params)
        Timber.d("Budget updated: $category, $oldAmount -> $newAmount")
    }

    fun logBudgetLimitReached(
        category: String,
        amount: Money,
        percentage: Float,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.BUDGET_CATEGORY, category)
                putString(AnalyticsConstants.Params.BUDGET_AMOUNT, amount.toString())
                putFloat(AnalyticsConstants.Params.BUDGET_PROGRESS, percentage)
            }
        logEvent(AnalyticsConstants.Events.BUDGET_LIMIT_REACHED, params)
        Timber.d("Budget limit reached: $category, $amount, $percentage%")
    }

    // Отчеты

    fun logReportGenerated(
        periodType: String,
        startDate: Date,
        endDate: Date,
        format: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.PERIOD_TYPE, periodType)
                putLong(AnalyticsConstants.Params.PERIOD_START, startDate.time)
                putLong(AnalyticsConstants.Params.PERIOD_END, endDate.time)
                putString(AnalyticsConstants.Params.REPORT_FORMAT, format)
            }
        logEvent(AnalyticsConstants.Events.REPORT_GENERATED, params)
        Timber.d("Report generated: $periodType, $format")
    }

    fun logReportShared(
        format: String,
        shareMethod: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.REPORT_FORMAT, format)
                putString(AnalyticsConstants.Params.REPORT_SHARE_METHOD, shareMethod)
            }
        logEvent(AnalyticsConstants.Events.REPORT_SHARED, params)
        Timber.d("Report shared: $format via $shareMethod")
    }

    // Настройки

    fun logSettingsChanged(
        settingName: String,
        settingValue: String,
        previousValue: String? = null,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.SETTING_NAME, settingName)
                putString(AnalyticsConstants.Params.SETTING_VALUE, settingValue)
                previousValue?.let { putString(AnalyticsConstants.Params.SETTING_PREVIOUS_VALUE, it) }
            }
        logEvent(AnalyticsConstants.Events.SETTINGS_CHANGED, params)
        Timber.d("Settings changed: $settingName = $settingValue")
    }

    // Ошибки

    fun logError(
        errorType: String,
        errorMessage: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
                putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            }
        logEvent(AnalyticsConstants.Events.ERROR, params)
        Timber.d("Error logged: $errorType - $errorMessage")
    }

    fun logValidationError(
        field: String,
        errorMessage: String,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.VALIDATION_FIELD, field)
                putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
                putString(AnalyticsConstants.Params.ERROR_TYPE, AnalyticsConstants.Values.ERROR_TYPE_VALIDATION)
            }
        logEvent(AnalyticsConstants.Events.VALIDATION_ERROR, params)
        Timber.d("Validation error logged: $field - $errorMessage")
    }

    // Функции и фичи

    fun logFeatureUsed(
        featureName: String,
        result: String = AnalyticsConstants.Values.RESULT_SUCCESS,
    ) {
        val params =
            Bundle().apply {
                putString(AnalyticsConstants.Params.FEATURE_NAME, featureName)
                putString(AnalyticsConstants.Params.FEATURE_RESULT, result)
            }
        logEvent(AnalyticsConstants.Events.FEATURE_USED, params)
        Timber.d("Feature used logged: $featureName, result: $result")
    }
}
