package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Класс для управления статистикой использования категорий через SharedPreferences.
 * Отслеживает частоту использования категорий для их умной сортировки.
 */
class CategoryUsagePreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "category_usage_preferences"
        private const val KEY_EXPENSE_CATEGORIES_USAGE = "expense_categories_usage"
        private const val KEY_INCOME_CATEGORIES_USAGE = "income_categories_usage"

        @Volatile
        private var instance: CategoryUsagePreferences? = null

        fun getInstance(context: Context): CategoryUsagePreferences {
            return instance ?: synchronized(this) {
                instance ?: CategoryUsagePreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Загружает статистику использования категорий расходов
     * @return Map с названиями категорий и количеством их использований
     */
    fun loadExpenseCategoriesUsage(): Map<String, Int> {
        val json = prefs.getString(KEY_EXPENSE_CATEGORIES_USAGE, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                gson.fromJson(json, type) ?: emptyMap()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing expense categories usage")
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    /**
     * Загружает статистику использования категорий доходов
     * @return Map с названиями категорий и количеством их использований
     */
    fun loadIncomeCategoriesUsage(): Map<String, Int> {
        val json = prefs.getString(KEY_INCOME_CATEGORIES_USAGE, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                gson.fromJson(json, type) ?: emptyMap()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing income categories usage")
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    /**
     * Сохраняет статистику использования категорий расходов
     */
    private fun saveExpenseCategoriesUsage(usageMap: Map<String, Int>) {
        try {
            val json = gson.toJson(usageMap)
            prefs.edit {
                putString(KEY_EXPENSE_CATEGORIES_USAGE, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving expense categories usage")
        }
    }

    /**
     * Сохраняет статистику использования категорий доходов
     */
    private fun saveIncomeCategoriesUsage(usageMap: Map<String, Int>) {
        try {
            val json = gson.toJson(usageMap)
            prefs.edit {
                putString(KEY_INCOME_CATEGORIES_USAGE, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving income categories usage")
        }
    }

    /**
     * Увеличивает счетчик использования категории расходов
     * @param category Название категории
     */
    fun incrementExpenseCategoryUsage(category: String) {
        val usageMap = loadExpenseCategoriesUsage().toMutableMap()
        val currentCount = usageMap[category] ?: 0
        usageMap[category] = currentCount + 1
        saveExpenseCategoriesUsage(usageMap)
    }

    /**
     * Увеличивает счетчик использования категории доходов
     * @param category Название категории
     */
    fun incrementIncomeCategoryUsage(category: String) {
        val usageMap = loadIncomeCategoriesUsage().toMutableMap()
        val currentCount = usageMap[category] ?: 0
        usageMap[category] = currentCount + 1
        saveIncomeCategoriesUsage(usageMap)
    }
} 
