package com.davidbugayov.financeanalyzer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.utils.GsonUtils
import com.google.gson.Gson
import timber.log.Timber

/**
 * Класс для управления пользовательскими категориями через SharedPreferences.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
class CategoryPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val typeCustomCategoryList = object : com.google.gson.reflect.TypeToken<List<CustomCategoryData>>() {}.type

    companion object {

        private const val PREFS_NAME = "category_preferences"
        private const val KEY_EXPENSE_CATEGORIES = "expense_categories"
        private const val KEY_INCOME_CATEGORIES = "income_categories"
        private const val KEY_DELETED_DEFAULT_EXPENSE_CATEGORIES = "deleted_default_expense_categories"
        private const val KEY_DELETED_DEFAULT_INCOME_CATEGORIES = "deleted_default_income_categories"

        @Volatile
        private var instance: CategoryPreferences? = null

        fun getInstance(context: Context): CategoryPreferences {
            return instance ?: synchronized(this) {
                instance ?: CategoryPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    data class CustomCategoryData(
        val name: String,
        val iconName: String,
        val colorHex: String? = null,
        val isExpense: Boolean = true // Добавлено новое поле
    )

    /**
     * Сохраняет пользовательские категории расходов
     */
    fun saveExpenseCategories(categories: List<CustomCategoryData>) {
        try {
            val json = gson.toJson(categories)
            prefs.edit {
                putString(KEY_EXPENSE_CATEGORIES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving expense categories")
        }
    }

    /**
     * Загружает пользовательские категории расходов
     */
    fun loadExpenseCategories(): List<CustomCategoryData> {
        val json = prefs.getString(KEY_EXPENSE_CATEGORIES, null)
        return if (json != null) {
            try {
                gson.fromJson<List<CustomCategoryData>>(json, typeCustomCategoryList) ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing expense categories")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Сохраняет пользовательские категории доходов
     */
    fun saveIncomeCategories(categories: List<CustomCategoryData>) {
        try {
            val json = gson.toJson(categories)
            prefs.edit {
                putString(KEY_INCOME_CATEGORIES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving income categories")
        }
    }

    /**
     * Загружает пользовательские категории доходов
     */
    fun loadIncomeCategories(): List<CustomCategoryData> {
        val json = prefs.getString(KEY_INCOME_CATEGORIES, null)
        return if (json != null) {
            try {
                gson.fromJson<List<CustomCategoryData>>(json, typeCustomCategoryList) ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Error parsing income categories")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Добавляет новую категорию расходов
     */
    fun addExpenseCategory(category: CustomCategoryData) {
        val categories = loadExpenseCategories().toMutableList()
        if (categories.none { it.name == category.name }) {
            categories.add(category)
            saveExpenseCategories(categories)
        }
    }

    /**
     * Добавляет новую категорию доходов
     */
    fun addIncomeCategory(category: CustomCategoryData) {
        val categories = loadIncomeCategories().toMutableList()
        if (categories.none { it.name == category.name }) {
            categories.add(category)
            saveIncomeCategories(categories)
        }
    }

    /**
     * Удаляет категорию расходов
     */
    fun removeExpenseCategory(category: String) {
        val categories = loadExpenseCategories().toMutableList()
        if (categories.removeIf { it.name == category }) {
            saveExpenseCategories(categories)
        }
    }

    /**
     * Удаляет категорию доходов
     */
    fun removeIncomeCategory(category: String) {
        val categories = loadIncomeCategories().toMutableList()
        if (categories.removeIf { it.name == category }) {
            saveIncomeCategories(categories)
        }
    }

    /**
     * Сохраняет список удаленных дефолтных категорий расходов
     */
    fun saveDeletedDefaultExpenseCategories(categories: List<String>) {
        try {
            val json = GsonUtils.toJsonFromStringList(gson, categories)
            prefs.edit {
                putString(KEY_DELETED_DEFAULT_EXPENSE_CATEGORIES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving deleted default expense categories")
        }
    }

    /**
     * Загружает список удаленных дефолтных категорий расходов
     */
    fun loadDeletedDefaultExpenseCategories(): List<String> {
        val json = prefs.getString(KEY_DELETED_DEFAULT_EXPENSE_CATEGORIES, null)
        return if (json != null) {
            try {
                GsonUtils.fromJsonToStringList(gson, json)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing deleted default expense categories")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Сохраняет список удаленных дефолтных категорий доходов
     */
    fun saveDeletedDefaultIncomeCategories(categories: List<String>) {
        try {
            val json = GsonUtils.toJsonFromStringList(gson, categories)
            prefs.edit {
                putString(KEY_DELETED_DEFAULT_INCOME_CATEGORIES, json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving deleted default income categories")
        }
    }

    /**
     * Загружает список удаленных дефолтных категорий доходов
     */
    fun loadDeletedDefaultIncomeCategories(): List<String> {
        val json = prefs.getString(KEY_DELETED_DEFAULT_INCOME_CATEGORIES, null)
        return if (json != null) {
            try {
                GsonUtils.fromJsonToStringList(gson, json)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing deleted default income categories")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Добавляет категорию в список удаленных дефолтных категорий расходов
     */
    fun addDeletedDefaultExpenseCategory(category: String) {
        val categories = loadDeletedDefaultExpenseCategories().toMutableList()
        if (!categories.contains(category)) {
            categories.add(category)
            saveDeletedDefaultExpenseCategories(categories)
        }
    }

    /**
     * Добавляет категорию в список удаленных дефолтных категорий доходов
     */
    fun addDeletedDefaultIncomeCategory(category: String) {
        val categories = loadDeletedDefaultIncomeCategories().toMutableList()
        if (!categories.contains(category)) {
            categories.add(category)
            saveDeletedDefaultIncomeCategories(categories)
        }
    }

    /**
     * Проверяет, существует ли категория с указанным именем
     *
     * @param categoryName Имя категории для проверки
     * @return true, если категория существует
     */
    fun categoryExists(categoryName: String): Boolean {
        val expenseCategories = loadExpenseCategories()
        val incomeCategories = loadIncomeCategories()

        return expenseCategories.any { it.name == categoryName } ||
            incomeCategories.any { it.name == categoryName }
    }

    /**
     * Добавляет новую категорию в соответствующий список (расходы или доходы)
     *
     * @param categoryData Данные категории для добавления
     */
    fun addCategory(categoryData: CustomCategoryData) {
        if (categoryData.isExpense) {
            addExpenseCategory(categoryData)
        } else {
            addIncomeCategory(categoryData)
        }
    }
} 
