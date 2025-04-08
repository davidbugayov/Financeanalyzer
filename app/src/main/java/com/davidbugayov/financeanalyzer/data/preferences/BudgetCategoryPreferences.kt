package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Класс для управления бюджетными категориями через SharedPreferences.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class BudgetCategoryPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFERENCES_NAME = "budget_category_prefs"
        private const val KEY_BUDGET_CATEGORIES = "budget_categories"

        @Volatile
        private var instance: BudgetCategoryPreferences? = null

        fun getInstance(context: Context): BudgetCategoryPreferences {
            return instance ?: synchronized(this) {
                instance ?: BudgetCategoryPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Сохраняет бюджетные категории
     */
    fun saveBudgetCategories(categories: List<BudgetCategory>) {
        try {
            val categoriesJson = gson.toJson(categories)
            prefs.edit {
                putString(KEY_BUDGET_CATEGORIES, categoriesJson)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving budget categories")
        }
    }

    /**
     * Загружает бюджетные категории
     */
    fun getBudgetCategories(): List<BudgetCategory> {
        return try {
            val categoriesJson = prefs.getString(KEY_BUDGET_CATEGORIES, null)
            if (categoriesJson == null) {
                emptyList()
            } else {
                val type = object : TypeToken<List<BudgetCategory>>() {}.type
                gson.fromJson(categoriesJson, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading budget categories")
            emptyList()
        }
    }

    /**
     * Добавляет новую бюджетную категорию
     */
    fun addBudgetCategory(category: BudgetCategory) {
        val currentCategories = getBudgetCategories().toMutableList()
        currentCategories.add(category)
        saveBudgetCategories(currentCategories)
    }

    /**
     * Удаляет бюджетную категорию
     */
    fun removeBudgetCategory(categoryId: String) {
        val currentCategories = getBudgetCategories().toMutableList()
        currentCategories.removeIf { it.id == categoryId }
        saveBudgetCategories(currentCategories)
    }

    /**
     * Обновляет существующую бюджетную категорию
     */
    fun updateBudgetCategory(category: BudgetCategory) {
        val currentCategories = getBudgetCategories().toMutableList()
        val index = currentCategories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            currentCategories[index] = category
            saveBudgetCategories(currentCategories)
        }
    }
} 