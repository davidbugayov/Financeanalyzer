package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.data.utils.GsonUtils
import com.google.gson.Gson
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

/**
 * Класс для управления источниками транзакций через SharedPreferences.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class SourcePreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()
    private val typeCustomSourceList = object : com.google.gson.reflect.TypeToken<List<CustomSourceData>>() {}.type

    companion object {
        private const val PREFS_NAME = "source_preferences"
        private const val KEY_CUSTOM_SOURCES = "custom_sources"
        private const val KEY_DELETED_DEFAULT_SOURCES = "deleted_default_sources"

        @Volatile
        private var instance: SourcePreferences? = null

        fun getInstance(context: Context): SourcePreferences {
            return instance ?: synchronized(this) {
                instance ?: SourcePreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    data class CustomSourceData(
        val name: String,
        val colorHex: String,
    )

    /**
     * Сохраняет пользовательские источники
     */
    fun saveCustomSources(sources: List<CustomSourceData>) {
        try {
            val json = gson.toJson(sources)
            prefs.edit {
                putString(KEY_CUSTOM_SOURCES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving custom sources")
            CrashLoggerProvider.crashLogger.logException(e)
        }
    }

    /**
     * Загружает пользовательские источники
     */
    fun getCustomSources(): List<CustomSourceData> {
        val json = prefs.getString(KEY_CUSTOM_SOURCES, null)
        return if (json != null) {
            try {
                gson.fromJson<List<CustomSourceData>>(json, typeCustomSourceList) ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing custom sources")
                CrashLoggerProvider.crashLogger.logException(e)
                return emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Добавляет новый пользовательский источник
     */
    fun addCustomSource(source: CustomSourceData) {
        val sources = getCustomSources().toMutableList()
        if (sources.none { it.name == source.name }) {
            sources.add(source)
            saveCustomSources(sources)
        }
    }

    /**
     * Удаляет пользовательский источник
     */
    fun deleteSource(sourceName: String) {
        val sources = getCustomSources().toMutableList()
        if (sources.removeIf { it.name == sourceName }) {
            saveCustomSources(sources)
        } else {
            // Если не кастомный — добавляем в удалённые дефолтные
            addDeletedDefaultSource(sourceName)
        }
    }

    /**
     * Сохраняет список удаленных дефолтных источников
     */
    fun saveDeletedDefaultSources(sources: List<String>) {
        try {
            val json = GsonUtils.toJsonFromStringList(gson, sources)
            prefs.edit {
                putString(KEY_DELETED_DEFAULT_SOURCES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving deleted default sources")
            CrashLoggerProvider.crashLogger.logException(e)
        }
    }

    /**
     * Загружает список удаленных дефолтных источников
     */
    fun getDeletedDefaultSources(): List<String> {
        val json = prefs.getString(KEY_DELETED_DEFAULT_SOURCES, null)
        return if (json != null) {
            try {
                GsonUtils.fromJsonToStringList(gson, json)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing deleted default sources")
                CrashLoggerProvider.crashLogger.logException(e)
                return emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Добавляет источник в список удаленных дефолтных источников
     */
    fun addDeletedDefaultSource(source: String) {
        val sources = getDeletedDefaultSources().toMutableList()
        if (!sources.contains(source)) {
            sources.add(source)
            saveDeletedDefaultSources(sources)
        }
    }
} 