package com.davidbugayov.financeanalyzer.analytics

import android.os.Bundle
import timber.log.Timber

/**
 * Адаптер для одновременной работы с несколькими системами аналитики.
 * Использует паттерн компоновщик для передачи событий во все зарегистрированные системы.
 */
class CompositeAnalytics : IAnalytics {
    private val analytics = mutableListOf<IAnalytics>()

    /**
     * Добавляет систему аналитики
     */
    fun addAnalytics(analytics: IAnalytics) {
        this.analytics.add(analytics)
        Timber.d("Analytics system added: ${analytics.javaClass.simpleName}")
    }

    /**
     * Удаляет систему аналитики
     */
    fun removeAnalytics(analytics: IAnalytics) {
        this.analytics.remove(analytics)
        Timber.d("Analytics system removed: ${analytics.javaClass.simpleName}")
    }

    override fun logEvent(eventName: String) {
        analytics.forEach { it.logEvent(eventName) }
    }

    override fun logEvent(
        eventName: String,
        params: Bundle,
    ) {
        analytics.forEach { it.logEvent(eventName, params) }
    }

    override fun setUserProperty(
        name: String,
        value: String,
    ) {
        analytics.forEach { it.setUserProperty(name, value) }
    }

    override fun setUserId(userId: String) {
        analytics.forEach { it.setUserId(userId) }
    }
}
