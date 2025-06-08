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

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()

    companion object {
        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
        private const val KEY_CUSTOM_SOURCES = "custom_sources"

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
            Timber.d("saveCustomSources: saving sources = $sourcesJson")
            prefs.edit {
                putString(KEY_CUSTOM_SOURCES, sourcesJson)
            }
            Timber.d("saveCustomSources: saved sources count = ${sources.size}")
        } catch (e: Exception) {
            Timber.e(e, "Error saving sources")
        }
    }

    /**
     * Загружает пользовательские источники средств
     */
    fun getCustomSources(): List<Source> {
        val sourcesJson = prefs.getString(KEY_CUSTOM_SOURCES, null)
        Timber.d("getCustomSources: loaded json = $sourcesJson")
        if (sourcesJson == null) return emptyList()
        val type = object : TypeToken<List<Source>>() {}.type
        return try {
            val sources = gson.fromJson<List<Source>>(sourcesJson, type)
            Timber.d("getCustomSources: loaded sources count = ${sources.size}")
            sources
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

    /**
     * Проверяет, существует ли источник с указанным именем
     *
     * @param sourceName Имя источника для проверки
     * @return true, если источник существует
     */
    fun sourceExists(sourceName: String): Boolean {
        return getCustomSources().any { it.name == sourceName }
    }

    /**
     * Добавляет новый источник
     *
     * @param source Источник для добавления
     */
    fun addSource(source: Source) {
        addCustomSource(source)
    }
}
