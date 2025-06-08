package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import com.davidbugayov.financeanalyzer.domain.model.Money
import timber.log.Timber
import java.util.Date

/**
 * Утилиты для работы с аналитикой
 */
object AnalyticsUtils {
    // Глобальный экземпляр аналитики
    private lateinit var analytics: IAnalytics

    /**
     * Инициализирует аналитику с выбранной реализацией
     */
    fun init(analyticsImpl: IAnalytics) {
        analytics = analyticsImpl
        Timber.d("AnalyticsUtils initialized with ${analyticsImpl.javaClass.simpleName}")
    }

    /**
     * Проверяет, была ли инициализирована аналитика
     */
    fun isInitialized(): Boolean {
        return ::analytics.isInitialized
    }

    /**
     * Возвращает текущий экземпляр аналитики
     */
    fun getAnalytics(): IAnalytics {
        if (!isInitialized()) {
            // Если аналитика не инициализирована, используем NoOp
            analytics = NoOpAnalytics()
        }
        return analytics
    }

    /**
     * Логирует событие без параметров
     */
    fun logEvent(eventName: String) {
        if (isInitialized()) {
            analytics.logEvent(eventName)
        }
    }

    /**
     * Логирует событие с параметрами
     */
    fun logEvent(eventName: String, params: Bundle) {
        if (isInitialized()) {
            analytics.logEvent(eventName, params)
        }
    }

    /**
     * Логирует событие просмотра экрана
     */
    fun logScreenView(screenName: String, screenClass: String) {
        val params = Bundle().apply {
            putString(Params.SCREEN_NAME, screenName)
            putString("screen_class", screenClass)
        }
        logEvent(Events.SCREEN_VIEW, params)
        Timber.d("Screen view logged: $screenName ($screenClass)")
    }

    /**
     * Логирует событие добавления транзакции
     */
    fun logTransactionAdded(
        amount: Money,
        category: String,
        isExpense: Boolean,
        hasDescription: Boolean
    ) {
        val params = Bundle().apply {
            putString(Params.TRANSACTION_TYPE, if (isExpense) "expense" else "income")
            putString(Params.TRANSACTION_AMOUNT, amount.toString())
            putString(Params.TRANSACTION_CATEGORY, category)
            putBoolean(Params.HAS_DESCRIPTION, hasDescription)
        }
        logEvent(Events.TRANSACTION_ADDED, params)
        Timber.d(
            "Transaction added logged: $amount, $category, ${if (isExpense) "expense" else "income"}"
        )
    }

    /**
     * Логирует событие редактирования транзакции
     */
    fun logTransactionEdited(amount: Money, category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(Params.TRANSACTION_TYPE, if (isExpense) "expense" else "income")
            putString(Params.TRANSACTION_AMOUNT, amount.toString())
            putString(Params.TRANSACTION_CATEGORY, category)
        }
        logEvent(Events.TRANSACTION_EDITED, params)
        Timber.d(
            "Transaction edited logged: $amount, $category, ${if (isExpense) "expense" else "income"}"
        )
    }

    /**
     * Логирует событие удаления транзакции
     */
    fun logTransactionDeleted(amount: Money, category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(Params.TRANSACTION_TYPE, if (isExpense) "expense" else "income")
            putString(Params.TRANSACTION_AMOUNT, amount.toString())
            putString(Params.TRANSACTION_CATEGORY, category)
        }
        logEvent(Events.TRANSACTION_DELETED, params)
        Timber.d(
            "Transaction deleted logged: $amount, $category, ${if (isExpense) "expense" else "income"}"
        )
    }

    /**
     * Логирует событие добавления категории
     */
    fun logCategoryAdded(category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(Params.CATEGORY_NAME, category)
            putString(Params.CATEGORY_TYPE, if (isExpense) "expense" else "income")
        }
        logEvent(Events.CATEGORY_ADDED, params)
        Timber.d("Category added: $category (${if (isExpense) "expense" else "income"})")
    }

    /**
     * Логирует событие редактирования категории
     */
    fun logCategoryEdited(oldCategory: String, newCategory: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(Params.CATEGORY_NAME, newCategory)
            putString(Params.CATEGORY_OLD_NAME, oldCategory)
            putString(Params.CATEGORY_TYPE, if (isExpense) "expense" else "income")
        }
        logEvent(Events.CATEGORY_EDITED, params)
        Timber.d(
            "Category edited: $oldCategory -> $newCategory (${if (isExpense) "expense" else "income"})"
        )
    }

    /**
     * Логирует событие удаления категории
     */
    fun logCategoryDeleted(category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString(Params.CATEGORY_NAME, category)
            putString(Params.CATEGORY_TYPE, if (isExpense) "expense" else "income")
        }
        logEvent(Events.CATEGORY_DELETED, params)
        Timber.d("Category deleted: $category (${if (isExpense) "expense" else "income"})")
    }

    /**
     * Логирует событие создания отчета
     */
    fun logReportGenerated(periodType: String, startDate: Date, endDate: Date, format: String) {
        val params = Bundle().apply {
            putString(Params.PERIOD_TYPE, periodType)
            putLong(Params.PERIOD_START, startDate.time)
            putLong(Params.PERIOD_END, endDate.time)
            putString(Params.REPORT_FORMAT, format)
        }
        logEvent(Events.REPORT_GENERATED, params)
        Timber.d("Report generated: $periodType, $format")
    }

    /**
     * Логирует событие изменения настроек приложения
     */
    fun logSettingsChanged(settingName: String, settingValue: String) {
        val params = Bundle().apply {
            putString(Params.SETTING_NAME, settingName)
            putString(Params.SETTING_VALUE, settingValue)
        }
        logEvent(Events.SETTINGS_CHANGED, params)
        Timber.d("Settings changed: $settingName = $settingValue")
    }

    /**
     * Логирует событие ошибки
     */
    fun logError(errorType: String, errorMessage: String) {
        val params = Bundle().apply {
            putString(Params.ERROR_TYPE, errorType)
            putString(Params.ERROR_MESSAGE, errorMessage)
        }
        logEvent(Events.ERROR, params)
        Timber.d("Error logged: $errorType - $errorMessage")
    }

    /**
     * Логирует событие успешного входа в приложение
     */
    fun logAppOpen() {
        logEvent(Events.APP_OPEN)
        Timber.d("App open logged")
    }

    /**
     * Логирует событие создания/изменения бюджета
     */
    fun logBudgetSet(amount: Money, period: String) {
        val params = Bundle().apply {
            putString(Params.BUDGET_AMOUNT, amount.toString())
            putString(Params.BUDGET_PERIOD, period)
        }
        logEvent(Events.BUDGET_SET, params)
        Timber.d("Budget set: $amount for $period")
    }

    /**
     * Устанавливает пользовательское свойство
     */
    fun setUserProperty(name: String, value: String) {
        if (isInitialized()) {
            analytics.setUserProperty(name, value)
        }
    }

    /**
     * Устанавливает ID пользователя
     */
    fun setUserId(userId: String) {
        if (isInitialized()) {
            analytics.setUserId(userId)
        }
    }

    /**
     * Константы событий аналитики
     */
    object Events {
        const val APP_OPEN = "app_open"
        const val SCREEN_VIEW = "screen_view"
        const val TRANSACTION_ADDED = "transaction_added"
        const val TRANSACTION_EDITED = "transaction_edited"
        const val TRANSACTION_DELETED = "transaction_deleted"
        const val CATEGORY_ADDED = "category_added"
        const val CATEGORY_EDITED = "category_edited"
        const val CATEGORY_DELETED = "category_deleted"
        const val REPORT_GENERATED = "report_generated"
        const val SETTINGS_CHANGED = "settings_changed"
        const val BUDGET_SET = "budget_set"
        const val BUDGET_EXCEEDED = "budget_exceeded"
        const val ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        const val FEATURE_USED = "feature_used"
        const val WALLET_CREATED = "wallet_created"
        const val WALLET_DELETED = "wallet_deleted"
        const val ERROR = "app_error"
    }

    /**
     * Константы параметров для событий
     */
    object Params {
        const val SCREEN_NAME = "screen_name"
        const val TRANSACTION_TYPE = "transaction_type"
        const val TRANSACTION_AMOUNT = "transaction_amount"
        const val TRANSACTION_CATEGORY = "transaction_category"
        const val HAS_DESCRIPTION = "has_description"
        const val CATEGORY_NAME = "category_name"
        const val CATEGORY_OLD_NAME = "category_old_name"
        const val CATEGORY_TYPE = "category_type"
        const val PERIOD_TYPE = "period_type"
        const val PERIOD_START = "period_start"
        const val PERIOD_END = "period_end"
        const val REPORT_FORMAT = "report_format"
        const val SETTING_NAME = "setting_name"
        const val SETTING_VALUE = "setting_value"
        const val BUDGET_AMOUNT = "budget_amount"
        const val BUDGET_PERIOD = "budget_period"
        const val ACHIEVEMENT_ID = "achievement_id"
        const val FEATURE_NAME = "feature_name"
        const val WALLET_NAME = "wallet_name"
        const val ERROR_MESSAGE = "error_message"
        const val ERROR_TYPE = "error_type"
    }
} 
