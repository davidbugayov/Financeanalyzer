package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import timber.log.Timber

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
     * Логирует событие удаления категории
     */
    fun logCategoryDeleted(category: String, isExpense: Boolean) {
        val params = Bundle().apply {
            putString("category_name", category)
            putString("category_type", if (isExpense) "expense" else "income")
        }
        logEvent(Events.CATEGORY_DELETED, params)
        Timber.d("Category deleted: $category (${if (isExpense) "expense" else "income"})")
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
        const val ERROR_MESSAGE = "error_message"
        const val ERROR_TYPE = "error_type"
    }
} 