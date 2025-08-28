package com.davidbugayov.financeanalyzer.core.middleware

// TODO: Import AnalyticsEvent and AnalyticsTracker when shared module is refactored
// import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsEvent
// import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsTracker

/**
 * Analytics event representation for middleware layer
 */
sealed class AnalyticsEvent {
    data class ScreenView(val screenName: String, val screenClass: String? = null) : AnalyticsEvent()
    data class Custom(val name: String, val parameters: Map<String, Any> = emptyMap()) : AnalyticsEvent()
    data class Exception(val exception: kotlin.Exception, val isFatal: Boolean = false, val additionalData: Map<String, String> = emptyMap()) : AnalyticsEvent()
    data class Purchase(val productId: String, val productName: String, val currency: String, val value: Double, val quantity: Int = 1) : AnalyticsEvent()
    data class Timing(val category: String, val name: String, val value: Long, val label: String? = null) : AnalyticsEvent()
}

/**
 * Analytics tracker interface for middleware layer
 */
interface AnalyticsTracker {
    fun trackEvent(event: AnalyticsEvent)
    fun setUserProperty(name: String, value: String)
}

/**
 * Middleware interface for analytics operations.
 * Provides a centralized way to track user interactions and app events.
 */
interface AnalyticsMiddleware {

    /**
     * Track a user interaction event
     */
    fun trackEvent(event: AnalyticsEvent)

    /**
     * Track screen view
     */
    fun trackScreenView(screenName: String, screenClass: String? = null)

    /**
     * Track user property
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Track timing event
     */
    fun trackTiming(
        category: String,
        name: String,
        value: Long,
        label: String? = null
    )

    /**
     * Track exception
     */
    fun trackException(
        exception: Exception,
        isFatal: Boolean = false,
        additionalData: Map<String, String> = emptyMap()
    )

    /**
     * Track purchase event
     */
    fun trackPurchase(
        productId: String,
        productName: String,
        currency: String,
        value: Double,
        quantity: Int = 1
    )

    /**
     * Track custom event with parameters
     */
    fun trackCustomEvent(
        eventName: String,
        parameters: Map<String, Any> = emptyMap()
    )
}

/**
 * Default implementation of AnalyticsMiddleware
 */
class DefaultAnalyticsMiddleware(
    private val analyticsTracker: AnalyticsTracker
) : AnalyticsMiddleware {

    override fun trackEvent(event: AnalyticsEvent) {
        analyticsTracker.trackEvent(event)
    }

    override fun trackScreenView(screenName: String, screenClass: String?) {
        val event = AnalyticsEvent.ScreenView(
            screenName = screenName,
            screenClass = screenClass
        )
        analyticsTracker.trackEvent(event)
    }

    override fun setUserProperty(name: String, value: String) {
        analyticsTracker.setUserProperty(name, value)
    }

    override fun trackTiming(
        category: String,
        name: String,
        value: Long,
        label: String?
    ) {
        val event = AnalyticsEvent.Timing(
            category = category,
            name = name,
            value = value,
            label = label
        )
        analyticsTracker.trackEvent(event)
    }

    override fun trackException(
        exception: Exception,
        isFatal: Boolean,
        additionalData: Map<String, String>
    ) {
        val event = AnalyticsEvent.Exception(
            exception = exception,
            isFatal = isFatal,
            additionalData = additionalData
        )
        analyticsTracker.trackEvent(event)
    }

    override fun trackPurchase(
        productId: String,
        productName: String,
        currency: String,
        value: Double,
        quantity: Int
    ) {
        val event = AnalyticsEvent.Purchase(
            productId = productId,
            productName = productName,
            currency = currency,
            value = value,
            quantity = quantity
        )
        analyticsTracker.trackEvent(event)
    }

    override fun trackCustomEvent(
        eventName: String,
        parameters: Map<String, Any>
    ) {
        val event = AnalyticsEvent.Custom(
            name = eventName,
            parameters = parameters
        )
        analyticsTracker.trackEvent(event)
    }
}

/**
 * No-op implementation for cases when analytics is disabled
 */
class NoOpAnalyticsMiddleware : AnalyticsMiddleware {

    override fun trackEvent(event: AnalyticsEvent) {
        // No operation
    }

    override fun trackScreenView(screenName: String, screenClass: String?) {
        // No operation
    }

    override fun setUserProperty(name: String, value: String) {
        // No operation
    }

    override fun trackTiming(
        category: String,
        name: String,
        value: Long,
        label: String?
    ) {
        // No operation
    }

    override fun trackException(
        exception: Exception,
        isFatal: Boolean,
        additionalData: Map<String, String>
    ) {
        // No operation
    }

    override fun trackPurchase(
        productId: String,
        productName: String,
        currency: String,
        value: Double,
        quantity: Int
    ) {
        // No operation
    }

    override fun trackCustomEvent(
        eventName: String,
        parameters: Map<String, Any>
    ) {
        // No operation
    }
}
