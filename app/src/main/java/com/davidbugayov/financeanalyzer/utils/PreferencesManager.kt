package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
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
        sharedPreferences.edit { putString(KEY_CUSTOM_SOURCES, sourcesJson) }
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

    /**
     * Сохраняет тему приложения в SharedPreferences
     * @param themeMode Режим темы для сохранения
     */
    fun saveThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit { putString(KEY_THEME_MODE, themeMode.name) }
    }

    /**
     * Загружает тему приложения из SharedPreferences
     * @return Режим темы или SYSTEM, если ничего не сохранено
     */
    fun getThemeMode(): ThemeMode {
        val themeName = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeName ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    companion object {

        private const val PREFERENCES_NAME = "finance_analyzer_prefs"
        private const val KEY_CUSTOM_SOURCES = "custom_sources"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TOTAL_INCOME = "total_income"
        private const val KEY_TOTAL_EXPENSE = "total_expense"
        private const val KEY_BALANCE = "balance"
        private const val KEY_STATS_LAST_UPDATE = "stats_last_update"
    }

    /**
     * Сохраняет финансовую статистику в SharedPreferences
     * @param totalIncome Общий доход
     * @param totalExpense Общий расход
     * @param balance Текущий баланс
     */
    fun saveFinancialStats(totalIncome: Double, totalExpense: Double, balance: Double) {
        sharedPreferences.edit {
            putString(KEY_TOTAL_INCOME, totalIncome.toString())
            putString(KEY_TOTAL_EXPENSE, totalExpense.toString())
            putString(KEY_BALANCE, balance.toString())
            putLong(KEY_STATS_LAST_UPDATE, System.currentTimeMillis())
        }
    }

    /**
     * Загружает финансовую статистику из SharedPreferences
     * @return Triple с общим доходом, общим расходом и балансом
     */
    fun getFinancialStats(): Triple<Double, Double, Double> {
        val totalIncome = sharedPreferences.getString(KEY_TOTAL_INCOME, "0.0")?.toDoubleOrNull() ?: 0.0
        val totalExpense = sharedPreferences.getString(KEY_TOTAL_EXPENSE, "0.0")?.toDoubleOrNull() ?: 0.0
        val balance = sharedPreferences.getString(KEY_BALANCE, "0.0")?.toDoubleOrNull() ?: 0.0
        
        return Triple(totalIncome, totalExpense, balance)
    }

    /**
     * Проверяет актуальность сохраненной статистики
     * @return true, если статистика актуальна (обновлена недавно)
     */
    fun isStatsUpToDate(): Boolean {
        val lastUpdate = sharedPreferences.getLong(KEY_STATS_LAST_UPDATE, 0)
        // Считаем статистику актуальной, если она обновлена не более 24 часов назад
        return System.currentTimeMillis() - lastUpdate < 24 * 60 * 60 * 1000
    }
} 