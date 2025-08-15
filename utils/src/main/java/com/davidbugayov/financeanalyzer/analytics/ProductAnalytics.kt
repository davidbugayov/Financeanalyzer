package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants.Events
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants.Params
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants.Values
import timber.log.Timber

/**
 * Продуктовая аналитика для отслеживания основных пользовательских действий.
 * Фокусируется на ключевых метриках: просмотры экранов, навигация, действия с транзакциями.
 */
object ProductAnalytics {

    /**
     * Отслеживает просмотр основного экрана
     */
    fun trackScreenView(screenName: String, screenClass: String, additionalParams: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            putString(Params.SCREEN_NAME, screenName)
            putString(Params.SCREEN_CLASS, screenClass)
            putString(Params.ACTION_TYPE, Values.ACTION_VIEW)
            additionalParams.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Double -> putDouble(key, value)
                }
            }
        }
        
        AnalyticsUtils.logEvent(Events.SCREEN_VIEW, bundle)
        Timber.d("[ProductAnalytics] Screen viewed: $screenName ($screenClass)")
    }

    /**
     * Отслеживает навигацию между экранами
     */
    fun trackNavigation(fromScreen: String, toScreen: String, method: String = "navigation") {
        val bundle = Bundle().apply {
            putString(Params.NAVIGATION_SOURCE, fromScreen)
            putString(Params.NAVIGATION_DESTINATION, toScreen)
            putString(Params.ACTION_TYPE, Values.ACTION_NAVIGATE)
            putString(Params.ELEMENT_TYPE, Values.ELEMENT_NAVIGATION)
            putString("navigation_method", method)
        }
        
        AnalyticsUtils.logEvent(Events.BUTTON_CLICKED, bundle)
        Timber.d("[ProductAnalytics] Navigation: $fromScreen -> $toScreen")
    }

    /**
     * Отслеживает начало добавления транзакции
     */
    fun trackTransactionAddStarted(source: String = Values.SOURCE_MANUAL) {
        val bundle = Bundle().apply {
            putString(Params.TRANSACTION_SOURCE, source)
            putString(Params.ACTION_TYPE, Values.ACTION_ADD)
        }
        
        AnalyticsUtils.logEvent(Events.TRANSACTION_ADD_STARTED, bundle)
        Timber.d("[ProductAnalytics] Transaction add started from: $source")
    }

    /**
     * Отслеживает завершение добавления транзакции
     */
    fun trackTransactionAddCompleted(
        amount: Double,
        category: String,
        type: String,
        hasNote: Boolean = false
    ) {
        val bundle = Bundle().apply {
            putDouble(Params.TRANSACTION_AMOUNT, amount)
            putString(Params.TRANSACTION_CATEGORY, category)
            putString(Params.TRANSACTION_TYPE, type)
            putBoolean(Params.TRANSACTION_HAS_NOTE, hasNote)
            putString(Params.TRANSACTION_AMOUNT_RANGE, getAmountRange(amount))
            putString(Params.ACTION_TYPE, Values.ACTION_ADD)
        }
        
        AnalyticsUtils.logEvent(Events.TRANSACTION_ADD_COMPLETED, bundle)
        Timber.d("[ProductAnalytics] Transaction add completed: $amount $type in $category")
    }

    /**
     * Отслеживает отмену добавления транзакции
     */
    fun trackTransactionAddCancelled() {
        val bundle = Bundle().apply {
            putString(Params.ACTION_TYPE, Values.ACTION_CANCEL)
        }
        
        AnalyticsUtils.logEvent(Events.TRANSACTION_ADD_CANCELLED, bundle)
        Timber.d("[ProductAnalytics] Transaction add cancelled")
    }

    /**
     * Отслеживает начало редактирования транзакции
     */
    fun trackTransactionEditStarted(reason: String = "user_initiated") {
        val bundle = Bundle().apply {
            putString(Params.TRANSACTION_EDIT_REASON, reason)
            putString(Params.ACTION_TYPE, Values.ACTION_EDIT)
        }
        
        AnalyticsUtils.logEvent(Events.TRANSACTION_EDIT_STARTED, bundle)
        Timber.d("[ProductAnalytics] Transaction edit started, reason: $reason")
    }

    /**
     * Отслеживает завершение редактирования транзакции
     */
    fun trackTransactionEditCompleted(
        amount: Double,
        category: String,
        type: String,
        hasNote: Boolean = false
    ) {
        val bundle = Bundle().apply {
            putDouble(Params.TRANSACTION_AMOUNT, amount)
            putString(Params.TRANSACTION_CATEGORY, category)
            putString(Params.TRANSACTION_TYPE, type)
            putBoolean(Params.TRANSACTION_HAS_NOTE, hasNote)
            putString(Params.TRANSACTION_AMOUNT_RANGE, getAmountRange(amount))
            putString(Params.ACTION_TYPE, Values.ACTION_EDIT)
        }
        
        AnalyticsUtils.logEvent(Events.TRANSACTION_EDIT_COMPLETED, bundle)
        Timber.d("[ProductAnalytics] Transaction edit completed: $amount $type in $category")
    }

    /**
     * Отслеживает отмену редактирования транзакции
     */
    fun trackTransactionEditCancelled() {
        val bundle = Bundle().apply {
            putString(Params.ACTION_TYPE, Values.ACTION_CANCEL)
        }
        
        AnalyticsUtils.logEvent(Events.TRANSACTION_EDIT_CANCELLED, bundle)
        Timber.d("[ProductAnalytics] Transaction edit cancelled")
    }

    /**
     * Отслеживает клик по кнопке
     */
    fun trackButtonClick(buttonName: String, screenName: String) {
        val bundle = Bundle().apply {
            putString(Params.ELEMENT_NAME, buttonName)
            putString(Params.ELEMENT_TYPE, Values.ELEMENT_BUTTON)
            putString(Params.SCREEN_NAME, screenName)
            putString(Params.ACTION_TYPE, Values.ACTION_CLICK)
        }
        
        AnalyticsUtils.logEvent(Events.BUTTON_CLICKED, bundle)
        Timber.d("[ProductAnalytics] Button clicked: $buttonName on $screenName")
    }

    /**
     * Отслеживает клик по карточке
     */
    fun trackCardClick(cardName: String, screenName: String) {
        val bundle = Bundle().apply {
            putString(Params.ELEMENT_NAME, cardName)
            putString(Params.ELEMENT_TYPE, Values.ELEMENT_CARD)
            putString(Params.SCREEN_NAME, screenName)
            putString(Params.ACTION_TYPE, Values.ACTION_CLICK)
        }
        
        AnalyticsUtils.logEvent(Events.CARD_CLICKED, bundle)
        Timber.d("[ProductAnalytics] Card clicked: $cardName on $screenName")
    }

    /**
     * Отслеживает применение фильтра
     */
    fun trackFilterApplied(filterType: String, filterValue: String, screenName: String) {
        val bundle = Bundle().apply {
            putString(Params.ELEMENT_NAME, filterType)
            putString(Params.ELEMENT_TYPE, Values.ELEMENT_FILTER)
            putString(Params.SCREEN_NAME, screenName)
            putString(Params.FILTER_APPLIED, filterValue)
            putString(Params.ACTION_TYPE, Values.ACTION_CLICK)
        }
        
        AnalyticsUtils.logEvent(Events.FILTER_APPLIED, bundle)
        Timber.d("[ProductAnalytics] Filter applied: $filterType = $filterValue on $screenName")
    }

    /**
     * Отслеживает поиск
     */
    fun trackSearchPerformed(query: String, resultsCount: Int, screenName: String) {
        val bundle = Bundle().apply {
            putString(Params.ELEMENT_TYPE, Values.ELEMENT_SEARCH)
            putString(Params.SCREEN_NAME, screenName)
            putString("search_query", query)
            putInt("search_results_count", resultsCount)
            putString(Params.ACTION_TYPE, Values.ACTION_CLICK)
        }
        
        AnalyticsUtils.logEvent(Events.SEARCH_PERFORMED, bundle)
        Timber.d("[ProductAnalytics] Search performed: '$query' found $resultsCount results on $screenName")
    }

    /**
     * Отслеживает время, проведенное на экране
     */
    fun trackTimeSpentOnScreen(screenName: String, durationMs: Long) {
        val bundle = Bundle().apply {
            putString(Params.SCREEN_NAME, screenName)
            putLong(Params.TIME_SPENT_ON_SCREEN, durationMs)
            putString(Params.ACTION_TYPE, Values.ACTION_VIEW)
        }
        
        AnalyticsUtils.logEvent(Events.SCREEN_LOAD, bundle)
        Timber.d("[ProductAnalytics] Time spent on $screenName: ${durationMs}ms")
    }

    /**
     * Определяет диапазон суммы транзакции
     */
    private fun getAmountRange(amount: Double): String {
        return when {
            amount <= 1000 -> Values.AMOUNT_RANGE_SMALL
            amount <= 10000 -> Values.AMOUNT_RANGE_MEDIUM
            else -> Values.AMOUNT_RANGE_LARGE
        }
    }
}
