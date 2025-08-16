package com.davidbugayov.financeanalyzer.shared.analytics

/**
 * Android-специфичная реализация PlatformAnalyticsTracker
 */
actual class PlatformAnalyticsTracker : AnalyticsTracker by NoOpAnalyticsTracker()
