package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Класс для управления пользовательскими категориями через SharedPreferences.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class CategoryPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {

        private const val PREFS_NAME = "category_preferences"
        private const val KEY_EXPENSE_CATEGORIES = "expense_categories"
        private const val KEY_INCOME_CATEGORIES = "income_categories"

        @Volatile
        private var instance: CategoryPreferences? = null

        fun getInstance(context: Context): CategoryPreferences {
            return instance ?: synchronized(this) {
                instance ?: CategoryPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Сохраняет пользовательские категории расходов
     */
    fun saveExpenseCategories(categories: List<String>) {
        val json = gson.toJson(categories)
        prefs.edit {
            putString(KEY_EXPENSE_CATEGORIES, json)
        }
    }

    /**
     * Загружает пользовательские категории расходов
     */
    fun loadExpenseCategories(): List<String> {
        val json = prefs.getString(KEY_EXPENSE_CATEGORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    /**
     * Сохраняет пользовательские категории доходов
     */
    fun saveIncomeCategories(categories: List<String>) {
        val json = gson.toJson(categories)
        prefs.edit {
            putString(KEY_INCOME_CATEGORIES, json)
        }
    }

    /**
     * Загружает пользовательские категории доходов
     */
    fun loadIncomeCategories(): List<String> {
        val json = prefs.getString(KEY_INCOME_CATEGORIES, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    /**
     * Добавляет новую категорию расходов
     */
    fun addExpenseCategory(category: String) {
        val categories = loadExpenseCategories().toMutableList()
        if (!categories.contains(category)) {
            categories.add(category)
            saveExpenseCategories(categories)
        }
    }

    /**
     * Добавляет новую категорию доходов
     */
    fun addIncomeCategory(category: String) {
        val categories = loadIncomeCategories().toMutableList()
        if (!categories.contains(category)) {
            categories.add(category)
            saveIncomeCategories(categories)
        }
    }

    /**
     * Удаляет категорию расходов
     */
    fun removeExpenseCategory(category: String) {
        val categories = loadExpenseCategories().toMutableList()
        if (categories.remove(category)) {
            saveExpenseCategories(categories)
        }
    }

    /**
     * Удаляет категорию доходов
     */
    fun removeIncomeCategory(category: String) {
        val categories = loadIncomeCategories().toMutableList()
        if (categories.remove(category)) {
            saveIncomeCategories(categories)
        }
    }
} 