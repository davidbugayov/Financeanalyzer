package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Менеджер для работы с SharedPreferences.
 * Отвечает за сохранение и загрузку пользовательских настроек и данных.
 */
class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    /**
     * Сохраняет список источников средств в SharedPreferences
     * @param sources Список источников для сохранения
     */
    fun saveCustomSources(sources: List<Source>) {
        val sourcesJson = gson.toJson(sources)
        sharedPreferences.edit().putString(KEY_CUSTOM_SOURCES, sourcesJson).apply()
    }

    /**
     * Загружает список источников средств из SharedPreferences
     * @return Список источников или пустой список, если ничего не сохранено
     */
    fun getCustomSources(): List<Source> {
        val sourcesJson = sharedPreferences.getString(KEY_CUSTOM_SOURCES, null) ?: return emptyList()
        val type = object : TypeToken<List<Source>>() {}.type
        return try {
            gson.fromJson(sourcesJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {

        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
        private const val KEY_CUSTOM_SOURCES = "custom_sources"
    }
} 