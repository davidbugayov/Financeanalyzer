package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * iOS-специфичная реализация PlatformAnalyticsTracker
 */
actual class PlatformAnalyticsTracker : AnalyticsTracker by IosAnalyticsTracker()
