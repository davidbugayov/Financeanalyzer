package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Класс для управления источниками средств через SharedPreferences.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class SourcePreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
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

    /**
     * Сохраняет пользовательские источники средств
     */
    fun saveCustomSources(sources: List<Source>) {
        try {
            val sourcesJson = gson.toJson(sources)
            prefs.edit {
                putString(KEY_CUSTOM_SOURCES, sourcesJson)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving sources")
        }
    }

    /**
     * Загружает пользовательские источники средств
     */
    fun getCustomSources(): List<Source> {
        val sourcesJson = prefs.getString(KEY_CUSTOM_SOURCES, null) ?: return emptyList()
        val type = object : TypeToken<List<Source>>() {}.type
        return try {
            gson.fromJson(sourcesJson, type)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing sources")
            emptyList()
        }
    }

    /**
     * Добавляет новый источник средств
     */
    fun addCustomSource(source: Source) {
        val sources = getCustomSources().toMutableList()
        if (!sources.contains(source)) {
            sources.add(source)
            saveCustomSources(sources)
        }
    }

    /**
     * Удаляет источник средств
     */
    fun removeCustomSource(source: Source) {
        val sources = getCustomSources().toMutableList()
        if (sources.remove(source)) {
            saveCustomSources(sources)
        }
    }

    /**
     * Сохраняет список удаленных дефолтных источников
     */
    fun saveDeletedDefaultSources(sources: List<String>) {
        try {
            val json = gson.toJson(sources)
            prefs.edit {
                putString(KEY_DELETED_DEFAULT_SOURCES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving deleted default sources")
        }
    }

    /**
     * Загружает список удаленных дефолтных источников
     */
    fun loadDeletedDefaultSources(): List<String> {
        val json = prefs.getString(KEY_DELETED_DEFAULT_SOURCES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing deleted default sources")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Добавляет источник в список удаленных дефолтных источников
     */
    fun addDeletedDefaultSource(source: String) {
        val sources = loadDeletedDefaultSources().toMutableList()
        if (!sources.contains(source)) {
            sources.add(source)
            saveDeletedDefaultSources(sources)
        }
    }

    /**
     * Удаляет источник средств по имени
     */
    fun deleteSource(sourceName: String) {
        val sources = getCustomSources().toMutableList()
        val found = sources.find { it.name == sourceName }
        if (found != null && sources.remove(found)) {
            saveCustomSources(sources)
            Timber.d("Source deleted: $sourceName")
        } else {
            Timber.d("Source not found for deletion: $sourceName")
        }
    }
} 