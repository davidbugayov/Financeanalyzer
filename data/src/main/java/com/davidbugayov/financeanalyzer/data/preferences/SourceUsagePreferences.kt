package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

/**
 * Класс для управления статистикой использования источников через SharedPreferences.
 * Отслеживает частоту использования источников для их умной сортировки.
 */
class SourceUsagePreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "source_usage_preferences"
        private const val KEY_SOURCES_USAGE = "sources_usage"

        @Volatile
        private var instance: SourceUsagePreferences? = null

        fun getInstance(context: Context): SourceUsagePreferences {
            return instance ?: synchronized(this) {
                instance ?: SourceUsagePreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Загружает статистику использования источников
     * @return Map с названиями источников и количеством их использований
     */
    fun getSourceUsage(): Map<String, Int> {
        val json = prefs.getString(KEY_SOURCES_USAGE, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                gson.fromJson(json, type) ?: emptyMap()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing sources usage")
                CrashLoggerProvider.crashLogger.logException(e)
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    /**
     * Сохраняет статистику использования источников
     */
    private fun saveSourceUsage(usageMap: Map<String, Int>) {
        try {
            val json = gson.toJson(usageMap)
            prefs.edit {
                putString(KEY_SOURCES_USAGE, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving sources usage")
            CrashLoggerProvider.crashLogger.logException(e)
        }
    }

    /**
     * Увеличивает счетчик использования источника
     * @param source Название источника
     */
    fun incrementSourceUsage(source: String) {
        val usageMap = getSourceUsage().toMutableMap()
        val currentCount = usageMap[source] ?: 0
        usageMap[source] = currentCount + 1
        saveSourceUsage(usageMap)
    }
} 