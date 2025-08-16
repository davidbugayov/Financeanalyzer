package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * Фабрика для создания трекера аналитики
 */
object AnalyticsTrackerFactory {
    
    /**
     * Создает экземпляр трекера аналитики для текущей платформы
     */
    fun create(): AnalyticsTracker {
        return PlatformAnalyticsTracker()
    }
}

/**
 * Ожидаемая декларация для платформо-специфичной реализации
 */
expect class PlatformAnalyticsTracker() : AnalyticsTracker
