package com.davidbugayov.financeanalyzer.core.feature

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for handling feature flags throughout the application.
 * Supports multiple configuration sources and provides reactive updates.
 */
class FeatureFlagManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("feature_flags", Context.MODE_PRIVATE)
) {

    private val _featureFlags = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val featureFlags: Flow<Map<String, Boolean>> = _featureFlags.asStateFlow()

    init {
        // Initialize with default values and any stored overrides
        val defaults = FeatureFlag.asMap()
        val stored = loadStoredFlags()
        val combined = defaults + stored
        _featureFlags.value = combined
    }

    /**
     * Check if a feature flag is enabled
     */
    fun isEnabled(flag: FeatureFlag): Boolean {
        return _featureFlags.value[flag.key] ?: flag.defaultValue
    }

    /**
     * Check if a feature flag is enabled by key
     */
    fun isEnabled(key: String): Boolean {
        val flag = FeatureFlag.fromKey(key)
        return if (flag != null) {
            isEnabled(flag)
        } else {
            // For unknown flags, default to false for safety
            false
        }
    }

    /**
     * Enable a feature flag
     */
    fun enable(flag: FeatureFlag) {
        setFlag(flag, true)
    }

    /**
     * Disable a feature flag
     */
    fun disable(flag: FeatureFlag) {
        setFlag(flag, false)
    }

    /**
     * Set a feature flag to a specific value
     */
    fun setFlag(flag: FeatureFlag, enabled: Boolean) {
        val currentFlags = _featureFlags.value.toMutableMap()
        currentFlags[flag.key] = enabled
        _featureFlags.value = currentFlags

        // Store the override
        storeFlag(flag.key, enabled)
    }

    /**
     * Reset a feature flag to its default value
     */
    fun resetToDefault(flag: FeatureFlag) {
        val currentFlags = _featureFlags.value.toMutableMap()
        currentFlags.remove(flag.key)
        _featureFlags.value = currentFlags

        // Remove stored override
        removeStoredFlag(flag.key)
    }

    /**
     * Reset all feature flags to their default values
     */
    fun resetAllToDefaults() {
        _featureFlags.value = FeatureFlag.asMap()
        clearAllStoredFlags()
    }

    /**
     * Get all current feature flag states
     */
    fun getAllFlags(): Map<String, Boolean> {
        return _featureFlags.value.toMap()
    }

    /**
     * Get flags that have been overridden from defaults
     */
    fun getOverriddenFlags(): Map<String, Boolean> {
        val defaults = FeatureFlag.asMap()
        return _featureFlags.value.filter { (key, value) ->
            defaults[key] != value
        }
    }

    /**
     * Check if a flag has been overridden from its default
     */
    fun isOverridden(flag: FeatureFlag): Boolean {
        return _featureFlags.value[flag.key] != flag.defaultValue
    }

    // Private methods for persistence

    private fun loadStoredFlags(): Map<String, Boolean> {
        val stored = mutableMapOf<String, Boolean>()
        FeatureFlag.values().forEach { flag ->
            val storedValue = sharedPreferences.getBoolean(flag.key, flag.defaultValue)
            if (storedValue != flag.defaultValue) {
                stored[flag.key] = storedValue
            }
        }
        return stored
    }

    private fun storeFlag(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }

    private fun removeStoredFlag(key: String) {
        sharedPreferences.edit {
            remove(key)
        }
    }

    private fun clearAllStoredFlags() {
        sharedPreferences.edit {
            FeatureFlag.values().forEach { flag ->
                remove(flag.key)
            }
        }
    }
}

/**
 * DSL for conditional feature flag execution
 */
inline fun FeatureFlagManager.ifEnabled(flag: FeatureFlag, block: () -> Unit) {
    if (isEnabled(flag)) {
        block()
    }
}

inline fun FeatureFlagManager.ifDisabled(flag: FeatureFlag, block: () -> Unit) {
    if (!isEnabled(flag)) {
        block()
    }
}

inline fun <T> FeatureFlagManager.whenEnabled(flag: FeatureFlag, defaultValue: T, block: () -> T): T {
    return if (isEnabled(flag)) {
        block()
    } else {
        defaultValue
    }
}

/**
 * Annotation for marking features that require specific flags
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresFeatureFlag(val flag: String)

/**
 * Annotation for marking experimental features
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExperimentalFeature
