package com.davidbugayov.financeanalyzer.core.feature

/**
 * Enum representing all available feature flags in the application.
 * Each flag has a unique key and default value.
 */
enum class FeatureFlag(
    val key: String,
    val defaultValue: Boolean,
    val description: String
) {

    // Analytics features
    ANALYTICS_ENABLED(
        key = "analytics_enabled",
        defaultValue = true,
        description = "Enable/disable analytics tracking"
    ),

    // Widget features
    WIDGETS_ENABLED(
        key = "widgets_enabled",
        defaultValue = true,
        description = "Enable/disable home screen widgets"
    ),

    // Backup features
    BACKUP_ENABLED(
        key = "backup_enabled",
        defaultValue = true,
        description = "Enable/disable automatic backup functionality"
    ),

    // Achievement features
    ACHIEVEMENTS_ENABLED(
        key = "achievements_enabled",
        defaultValue = true,
        description = "Enable/disable achievement system"
    ),

    // Statistics features
    ADVANCED_STATISTICS_ENABLED(
        key = "advanced_statistics_enabled",
        defaultValue = false,
        description = "Enable/disable advanced statistics features"
    ),

    // Export features
    EXPORT_TO_CSV_ENABLED(
        key = "export_to_csv_enabled",
        defaultValue = true,
        description = "Enable/disable CSV export functionality"
    ),

    // UI features
    DARK_MODE_ENABLED(
        key = "dark_mode_enabled",
        defaultValue = true,
        description = "Enable/disable dark mode support"
    ),

    // Notification features
    PUSH_NOTIFICATIONS_ENABLED(
        key = "push_notifications_enabled",
        defaultValue = false,
        description = "Enable/disable push notifications"
    ),

    // Experimental features
    EXPERIMENTAL_UI_ENABLED(
        key = "experimental_ui_enabled",
        defaultValue = false,
        description = "Enable/disable experimental UI features"
    ),

    // Performance features
    PERFORMANCE_MONITORING_ENABLED(
        key = "performance_monitoring_enabled",
        defaultValue = false,
        description = "Enable/disable performance monitoring"
    ),

    // Development features
    DEBUG_LOGGING_ENABLED(
        key = "debug_logging_enabled",
        defaultValue = false,
        description = "Enable/disable debug logging (only in debug builds)"
    ),

    // Beta features
    BETA_FEATURES_ENABLED(
        key = "beta_features_enabled",
        defaultValue = false,
        description = "Enable/disable beta features"
    );

    companion object {
        /**
         * Get a feature flag by its key
         */
        fun fromKey(key: String): FeatureFlag? {
            return values().find { it.key == key }
        }

        /**
         * Get all feature flags as a map
         */
        fun asMap(): Map<String, Boolean> {
            return values().associate { it.key to it.defaultValue }
        }
    }
}
