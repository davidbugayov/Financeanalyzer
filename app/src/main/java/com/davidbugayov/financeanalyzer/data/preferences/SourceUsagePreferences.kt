package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Класс для отслеживания частоты использования источников через SharedPreferences.
 * Позволяет сортировать источники по частоте использования.
 */
class SourceUsagePreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()

    companion object {

        private const val PREFERENCES_NAME = "source_usage_prefs"
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
     * @return Map с именами источников и количеством их использований
     */
    fun loadSourcesUsage(): Map<String, Int> {
        val usageJson = prefs.getString(KEY_SOURCES_USAGE, null)
        if (usageJson == null) return emptyMap()

        val type = object : TypeToken<Map<String, Int>>() {}.type
        return try {
            val usage = gson.fromJson<Map<String, Int>>(usageJson, type)
            Timber.d("[SOURCE_USAGE] Загружена статистика использования: %s", usage)
            usage
        } catch (e: Exception) {
            Timber.e(e, "[SOURCE_USAGE] Ошибка при загрузке статистики использования источников")
            emptyMap()
        }
    }

    /**
     * Сохраняет статистику использования источников
     * @param usage Map с именами источников и количеством их использований
     */
    private fun saveSourcesUsage(usage: Map<String, Int>) {
        try {
            val usageJson = gson.toJson(usage)
            prefs.edit {
                putString(KEY_SOURCES_USAGE, usageJson)
            }
            Timber.d("[SOURCE_USAGE] Сохранена статистика использования: %s", usage)
        } catch (e: Exception) {
            Timber.e(e, "[SOURCE_USAGE] Ошибка при сохранении статистики использования источников")
        }
    }

    /**
     * Увеличивает счетчик использования источника
     * @param sourceName Имя источника
     */
    fun incrementSourceUsage(sourceName: String) {
        if (sourceName.isBlank()) return

        val usage = loadSourcesUsage().toMutableMap()
        val currentCount = usage[sourceName] ?: 0
        usage[sourceName] = currentCount + 1

        saveSourcesUsage(usage)
        Timber.d(
            "[SOURCE_USAGE] Увеличен счетчик использования источника '%s': %d",
            sourceName,
            currentCount + 1,
        )
    }

    /**
     * Возвращает количество использований источника
     * @param sourceName Имя источника
     * @return Количество использований источника
     */
    fun getSourceUsage(sourceName: String): Int {
        val usage = loadSourcesUsage()[sourceName] ?: 0
        Timber.d("[SOURCE_USAGE] Использований источника '%s': %d", sourceName, usage)
        return usage
    }
}
