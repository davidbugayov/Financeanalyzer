/* Copied from app/src/main/java/com/davidbugayov/financeanalyzer/analytics/AnalyticsUtils.kt */
package com.davidbugayov.financeanalyzer.analytics

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.os.Bundle
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.davidbugayov.financeanalyzer.core.model.Money
import timber.log.Timber
import java.util.Date
import java.util.UUID

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

    fun logEvent(eventName: String, params: Bundle) {
        if (isInitialized()) analytics.logEvent(eventName, params)
    }

    fun setUserProperty(name: String, value: String) {
        if (isInitialized()) analytics.setUserProperty(name, value)
    }

    fun setUserId(userId: String) {
        if (isInitialized()) analytics.setUserId(userId)
    }

    // Жизненный цикл приложения
    
    fun logAppOpen() {
        appOpenTimestamp = System.currentTimeMillis()
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
        }
        logEvent(AnalyticsConstants.Events.APP_OPEN, params)
        Timber.d("App open logged, session: $sessionId")
    }
    
    fun logAppClose() {
        val sessionDuration = System.currentTimeMillis() - appOpenTimestamp
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, sessionDuration)
        }
        logEvent(AnalyticsConstants.Events.APP_CLOSE, params)
        Timber.d("App close logged, session: $sessionId, duration: $sessionDuration ms")
    }
    
    fun logAppBackground() {
        val sessionDuration = System.currentTimeMillis() - appOpenTimestamp
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
            putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, sessionDuration)
        }
        logEvent(AnalyticsConstants.Events.APP_BACKGROUND, params)
        Timber.d("App background logged, session: $sessionId, duration: $sessionDuration ms")
    }
    
    fun logAppForeground() {
        appOpenTimestamp = System.currentTimeMillis()
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
        }
        logEvent(AnalyticsConstants.Events.APP_FOREGROUND, params)
        Timber.d("App foreground logged, session: $sessionId")
    }

    // Экраны и навигация
    
    fun logScreenView(screenName: String, screenClass: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
            putString(AnalyticsConstants.Params.SCREEN_CLASS, screenClass)
            putString(AnalyticsConstants.Params.SESSION_ID, sessionId)
        }
        logEvent(AnalyticsConstants.Events.SCREEN_VIEW, params)
        Timber.d("Screen view logged: $screenName ($screenClass)")
    }
    
    fun logScreenLoad(screenName: String, durationMs: Long) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
            putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
        }
        logEvent(AnalyticsConstants.Events.SCREEN_LOAD, params)
        Timber.d("Screen load logged: $screenName, duration: $durationMs ms")
    }
    
    fun logNavigation(source: String, destination: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SOURCE, source)
            putString(AnalyticsConstants.Params.DESTINATION, destination)
        }
        logEvent("navigation", params)
        Timber.d("Navigation logged: $source -> $destination")
    }

    // Транзакции
    
    fun logTransactionAdded(amount: Money, category: String, isExpense: Boolean, hasDescription: Boolean, source: String = AnalyticsConstants.Values.SOURCE_USER) {
        val params = Bundle().apply {
            putString(
                AnalyticsConstants.Params.TRANSACTION_TYPE, 
                if (isExpense) AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE 
                else AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
            )
            putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
            putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
            putBoolean(AnalyticsConstants.Params.HAS_DESCRIPTION, hasDescription)
            putString(AnalyticsConstants.Params.SOURCE, source)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_ADDED, params)
        Timber.d("Transaction added logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    fun logTransactionEdited(amount: Money, category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(
                AnalyticsConstants.Params.TRANSACTION_TYPE, 
                if (isExpense) AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE 
                else AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
            )
            putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
            putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EDITED, params)
        Timber.d("Transaction edited logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }

    fun logTransactionDeleted(amount: Money, category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(
                AnalyticsConstants.Params.TRANSACTION_TYPE, 
                if (isExpense) AnalyticsConstants.Values.TRANSACTION_TYPE_EXPENSE 
                else AnalyticsConstants.Values.TRANSACTION_TYPE_INCOME
            )
            putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, amount.toString())
            putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, category)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_DELETED, params)
        Timber.d("Transaction deleted logged: $amount, $category, ${if (isExpense) "expense" else "income"}")
    }
    
    fun logTransactionFiltered(filters: Map<String, String>, resultCount: Int) {
        val params = Bundle().apply {
            putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, resultCount)
            filters.forEach { (key, value) ->
                putString(key, value)
            }
            putBoolean(AnalyticsConstants.Params.FILTER_APPLIED, filters.isNotEmpty())
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_FILTERED, params)
        Timber.d("Transaction filtered logged: ${filters.size} filters, $resultCount results")
    }
    
    fun logTransactionSearched(query: String, resultCount: Int) {
        val params = Bundle().apply {
            putString("search_query", query)
            putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, resultCount)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_SEARCHED, params)
        Timber.d("Transaction searched logged: \"$query\", $resultCount results")
    }
    
    fun logTransactionImported(source: String, count: Int, success: Boolean, errorMessage: String? = null) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.IMPORT_SOURCE, source)
            putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, count)
            putString(
                AnalyticsConstants.Params.FEATURE_RESULT,
                if (success) AnalyticsConstants.Values.RESULT_SUCCESS else AnalyticsConstants.Values.RESULT_FAILURE
            )
            errorMessage?.let { putString(AnalyticsConstants.Params.ERROR_MESSAGE, it) }
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_IMPORTED, params)
        Timber.d("Transaction imported logged: $source, $count transactions, success: $success")
    }
    
    fun logTransactionExportStarted(format: String, count: Int, periodType: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.EXPORT_FORMAT, format)
            putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, count)
            putString(AnalyticsConstants.Params.PERIOD_TYPE, periodType)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EXPORT_STARTED, params)
        Timber.d("Transaction export started: $format, $count transactions, period: $periodType")
    }
    
    fun logTransactionExportCompleted(format: String, count: Int, durationMs: Long) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.EXPORT_FORMAT, format)
            putInt(AnalyticsConstants.Params.TRANSACTION_COUNT, count)
            putLong(AnalyticsConstants.Params.DURATION_MS, durationMs)
            putString(AnalyticsConstants.Params.FEATURE_RESULT, AnalyticsConstants.Values.RESULT_SUCCESS)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EXPORT_COMPLETED, params)
        Timber.d("Transaction export completed: $format, $count transactions, duration: $durationMs ms")
    }
    
    fun logTransactionExportFailed(format: String, errorMessage: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.EXPORT_FORMAT, format)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.FEATURE_RESULT, AnalyticsConstants.Values.RESULT_FAILURE)
        }
        logEvent(AnalyticsConstants.Events.TRANSACTION_EXPORT_FAILED, params)
        Timber.d("Transaction export failed: $format, error: $errorMessage")
    }

    // Категории
    
    fun logCategoryAdded(category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
            putString(
                AnalyticsConstants.Params.CATEGORY_TYPE, 
                if (isExpense) AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE 
                else AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
            )
        }
        logEvent(AnalyticsConstants.Events.CATEGORY_ADDED, params)
        Timber.d("Category added: $category (${if (isExpense) "expense" else "income"})")
    }

    fun logCategoryEdited(oldCategory: String, newCategory: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.CATEGORY_NAME, newCategory)
            putString(AnalyticsConstants.Params.CATEGORY_OLD_NAME, oldCategory)
            putString(
                AnalyticsConstants.Params.CATEGORY_TYPE, 
                if (isExpense) AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE 
                else AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
            )
        }
        logEvent(AnalyticsConstants.Events.CATEGORY_EDITED, params)
        Timber.d("Category edited: $oldCategory -> $newCategory (${if (isExpense) "expense" else "income"})")
    }

    fun logCategoryDeleted(category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
            putString(
                AnalyticsConstants.Params.CATEGORY_TYPE, 
                if (isExpense) AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE 
                else AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
            )
        }
        logEvent(AnalyticsConstants.Events.CATEGORY_DELETED, params)
        Timber.d("Category deleted: $category (${if (isExpense) "expense" else "income"})")
    }
    
    fun logCategorySelected(category: String, isExpense: Boolean, source: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.CATEGORY_NAME, category)
            putString(
                AnalyticsConstants.Params.CATEGORY_TYPE, 
                if (isExpense) AnalyticsConstants.Values.CATEGORY_TYPE_EXPENSE 
                else AnalyticsConstants.Values.CATEGORY_TYPE_INCOME
            )
            putString(AnalyticsConstants.Params.SOURCE, source)
        }
        logEvent(AnalyticsConstants.Events.CATEGORY_SELECTED, params)
        Timber.d("Category selected: $category (${if (isExpense) "expense" else "income"}) from $source")
    }

    // Бюджет
    
    fun logBudgetCreated(category: String, amount: Money, periodType: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.BUDGET_CATEGORY, category)
            putString(AnalyticsConstants.Params.BUDGET_AMOUNT, amount.toString())
            putString(AnalyticsConstants.Params.BUDGET_PERIOD, periodType)
        }
        logEvent(AnalyticsConstants.Events.BUDGET_CREATED, params)
        Timber.d("Budget created: $category, $amount, period: $periodType")
    }
    
    fun logBudgetUpdated(category: String, oldAmount: Money, newAmount: Money) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.BUDGET_CATEGORY, category)
            putString(AnalyticsConstants.Params.SETTING_PREVIOUS_VALUE, oldAmount.toString())
            putString(AnalyticsConstants.Params.BUDGET_AMOUNT, newAmount.toString())
        }
        logEvent(AnalyticsConstants.Events.BUDGET_UPDATED, params)
        Timber.d("Budget updated: $category, $oldAmount -> $newAmount")
    }
    
    fun logBudgetLimitReached(category: String, amount: Money, percentage: Float) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.BUDGET_CATEGORY, category)
            putString(AnalyticsConstants.Params.BUDGET_AMOUNT, amount.toString())
            putFloat(AnalyticsConstants.Params.BUDGET_PROGRESS, percentage)
        }
        logEvent(AnalyticsConstants.Events.BUDGET_LIMIT_REACHED, params)
        Timber.d("Budget limit reached: $category, $amount, $percentage%")
    }

    // Отчеты
    
    fun logReportGenerated(periodType: String, startDate: Date, endDate: Date, format: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.PERIOD_TYPE, periodType)
            putLong(AnalyticsConstants.Params.PERIOD_START, startDate.time)
            putLong(AnalyticsConstants.Params.PERIOD_END, endDate.time)
            putString(AnalyticsConstants.Params.REPORT_FORMAT, format)
        }
        logEvent(AnalyticsConstants.Events.REPORT_GENERATED, params)
        Timber.d("Report generated: $periodType, $format")
    }
    
    fun logReportShared(format: String, shareMethod: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.REPORT_FORMAT, format)
            putString(AnalyticsConstants.Params.REPORT_SHARE_METHOD, shareMethod)
        }
        logEvent(AnalyticsConstants.Events.REPORT_SHARED, params)
        Timber.d("Report shared: $format via $shareMethod")
    }

    // Настройки
    
    fun logSettingsChanged(settingName: String, settingValue: String, previousValue: String? = null) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.SETTING_NAME, settingName)
            putString(AnalyticsConstants.Params.SETTING_VALUE, settingValue)
            previousValue?.let { putString(AnalyticsConstants.Params.SETTING_PREVIOUS_VALUE, it) }
        }
        logEvent(AnalyticsConstants.Events.SETTINGS_CHANGED, params)
        Timber.d("Settings changed: $settingName = $settingValue")
    }

    // Ошибки
    
    fun logError(errorType: String, errorMessage: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.ERROR_TYPE, errorType)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
        }
        logEvent(AnalyticsConstants.Events.ERROR, params)
        Timber.d("Error logged: $errorType - $errorMessage")
    }
    
    fun logValidationError(field: String, errorMessage: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.VALIDATION_FIELD, field)
            putString(AnalyticsConstants.Params.ERROR_MESSAGE, errorMessage)
            putString(AnalyticsConstants.Params.ERROR_TYPE, AnalyticsConstants.Values.ERROR_TYPE_VALIDATION)
        }
        logEvent(AnalyticsConstants.Events.VALIDATION_ERROR, params)
        Timber.d("Validation error logged: $field - $errorMessage")
    }

    // Функции и фичи
    
    fun logFeatureUsed(featureName: String, result: String = AnalyticsConstants.Values.RESULT_SUCCESS) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.FEATURE_NAME, featureName)
            putString(AnalyticsConstants.Params.FEATURE_RESULT, result)
        }
        logEvent(AnalyticsConstants.Events.FEATURE_USED, params)
        Timber.d("Feature used logged: $featureName, result: $result")
    }
    
    fun logFeatureEnabled(featureName: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.FEATURE_NAME, featureName)
        }
        logEvent(AnalyticsConstants.Events.FEATURE_ENABLED, params)
        Timber.d("Feature enabled: $featureName")
    }
    
    fun logFeatureDisabled(featureName: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.FEATURE_NAME, featureName)
        }
        logEvent(AnalyticsConstants.Events.FEATURE_DISABLED, params)
        Timber.d("Feature disabled: $featureName")
    }

    // Виджеты
    
    fun logWidgetAdded(widgetType: String, widgetSize: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.WIDGET_TYPE, widgetType)
            putString(AnalyticsConstants.Params.WIDGET_SIZE, widgetSize)
        }
        logEvent(AnalyticsConstants.Events.WIDGET_ADDED, params)
        Timber.d("Widget added: $widgetType, size: $widgetSize")
    }
    
    fun logWidgetInteraction(widgetType: String, action: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.WIDGET_TYPE, widgetType)
            putString(AnalyticsConstants.Params.WIDGET_ACTION, action)
        }
        logEvent(AnalyticsConstants.Events.WIDGET_INTERACTION, params)
        Timber.d("Widget interaction: $widgetType, action: $action")
    }

    // Уведомления
    
    fun logNotificationReceived(notificationType: String, notificationId: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.NOTIFICATION_TYPE, notificationType)
            putString(AnalyticsConstants.Params.NOTIFICATION_ID, notificationId)
        }
        logEvent(AnalyticsConstants.Events.NOTIFICATION_RECEIVED, params)
        Timber.d("Notification received: $notificationType, id: $notificationId")
    }
    
    fun logNotificationOpened(notificationType: String, notificationId: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.NOTIFICATION_TYPE, notificationType)
            putString(AnalyticsConstants.Params.NOTIFICATION_ID, notificationId)
        }
        logEvent(AnalyticsConstants.Events.NOTIFICATION_OPENED, params)
        Timber.d("Notification opened: $notificationType, id: $notificationId")
    }

    // Обновления
    
    fun logUpdateAvailable(version: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.UPDATE_VERSION, version)
        }
        logEvent(AnalyticsConstants.Events.UPDATE_AVAILABLE, params)
        Timber.d("Update available: $version")
    }
    
    fun logUpdateInstalled(version: String, source: String) {
        val params = Bundle().apply {
            putString(AnalyticsConstants.Params.UPDATE_VERSION, version)
            putString(AnalyticsConstants.Params.UPDATE_SOURCE, source)
        }
        logEvent(AnalyticsConstants.Events.UPDATE_INSTALLED, params)
        Timber.d("Update installed: $version from $source")
    }

    // Пользовательская активность
    
    fun logUserEngagement(durationMs: Long, screenName: String) {
        val params = Bundle().apply {
            putLong(AnalyticsConstants.Params.USER_ENGAGEMENT_TIME, durationMs)
            putString(AnalyticsConstants.Params.SCREEN_NAME, screenName)
        }
        logEvent(AnalyticsConstants.Events.USER_ENGAGEMENT, params)
        Timber.d("User engagement: $durationMs ms on $screenName")
    }
    
    fun logUserFeedback(score: Int, text: String?) {
        val params = Bundle().apply {
            putInt(AnalyticsConstants.Params.USER_FEEDBACK_SCORE, score)
            text?.let { putString(AnalyticsConstants.Params.USER_FEEDBACK_TEXT, it) }
        }
        logEvent(AnalyticsConstants.Events.USER_FEEDBACK, params)
        Timber.d("User feedback: score $score")
    }

    // Утилиты для получения информации о системе
    
    fun getDeviceInfo(context: Context): Bundle {
        return Bundle().apply {
            putString(AnalyticsConstants.Params.DEVICE_MODEL, "${Build.MANUFACTURER} ${Build.MODEL}")
            putString(AnalyticsConstants.Params.DEVICE_BRAND, Build.BRAND)
            putString(AnalyticsConstants.Params.ANDROID_VERSION, "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            putString(AnalyticsConstants.Params.NETWORK_TYPE, getNetworkType(context))
        }
    }
    
    private fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return AnalyticsConstants.Values.NETWORK_TYPE_NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return AnalyticsConstants.Values.NETWORK_TYPE_NONE
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> AnalyticsConstants.Values.NETWORK_TYPE_WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> AnalyticsConstants.Values.NETWORK_TYPE_MOBILE
                else -> AnalyticsConstants.Values.NETWORK_TYPE_NONE
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> AnalyticsConstants.Values.NETWORK_TYPE_WIFI
                ConnectivityManager.TYPE_MOBILE -> AnalyticsConstants.Values.NETWORK_TYPE_MOBILE
                else -> AnalyticsConstants.Values.NETWORK_TYPE_NONE
            }
        }
    }
}
