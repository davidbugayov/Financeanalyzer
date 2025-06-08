package com.davidbugayov.financeanalyzer.presentation.categories

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences.CustomCategoryData
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryIconProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.ui.theme.toHexString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * ViewModel для управления категориями транзакций.
 * Следует принципам MVVM и Clean Architecture.
 */
class CategoriesViewModel(
    application: Application
) : AndroidViewModel(application), KoinComponent {

    private val categoryPreferences: CategoryPreferences by inject()
    private val categoryUsagePreferences: CategoryUsagePreferences by inject()

    private val _expenseCategories = MutableStateFlow(emptyList<UiCategory>())
    val expenseCategories: StateFlow<List<UiCategory>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow(emptyList<UiCategory>())
    val incomeCategories: StateFlow<List<UiCategory>> = _incomeCategories.asStateFlow()

    init {
        Timber.d("[CategoriesVM] CategoriesViewModel создан: $this")
        loadCategories()
    }

    /**
     * Загружает сохраненные категории и сортирует их по частоте использования
     */
    private fun loadCategories() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val savedExpenseCategories = categoryPreferences.loadExpenseCategories()
            val savedIncomeCategories = categoryPreferences.loadIncomeCategories()
            val deletedDefaultExpenseCategories = categoryPreferences.loadDeletedDefaultExpenseCategories()
            val deletedDefaultIncomeCategories = categoryPreferences.loadDeletedDefaultIncomeCategories()

            // Загружаем статистику использования категорий
            val expenseCategoriesUsage = categoryUsagePreferences.loadExpenseCategoriesUsage()
            val incomeCategoriesUsage = categoryUsagePreferences.loadIncomeCategoriesUsage()

            // Получаем дефолтные категории через провайдер
            val allDefaultExpenseCategories = CategoryProvider.getDefaultExpenseCategories(context)
            val allDefaultIncomeCategories = CategoryProvider.getDefaultIncomeCategories(context)

            // Фильтруем дефолтные категории, исключая удаленные
            val filteredDefaultExpenseCategories = allDefaultExpenseCategories.filter {
                !deletedDefaultExpenseCategories.contains(it.name) && it.name != "Другое"
            }
            val filteredDefaultIncomeCategories = allDefaultIncomeCategories.filter {
                !deletedDefaultIncomeCategories.contains(it.name) && it.name != "Другое"
            }

            // Добавляем пользовательские категории
            val customExpenseCategories = savedExpenseCategories.map { data ->
                val color = data.colorHex?.let {
                    try {
                        Color(it.toColorInt())
                    } catch (e: IllegalArgumentException) {
                        Timber.w(
                            e,
                            "Invalid color hex for expense category '${data.name}': $it. Generating new one."
                        )
                        val generated = CategoryProvider.generateCategoryColorFromPalette(
                            isExpense = true
                        )
                        val newColorHex = generated.toHexString()
                        val updated = data.copy(colorHex = newColorHex)
                        val updatedList = savedExpenseCategories.map { cat -> if (cat.name == data.name) updated else cat }
                        categoryPreferences.saveExpenseCategories(updatedList)
                        generated
                    }
                } ?: run {
                    val generated = CategoryProvider.generateCategoryColorFromPalette(
                        isExpense = true
                    )
                    val colorHex = generated.toHexString()
                    val updated = data.copy(colorHex = colorHex)
                    val updatedList = savedExpenseCategories.map { cat -> if (cat.name == data.name) updated else cat }
                    categoryPreferences.saveExpenseCategories(updatedList)
                    generated
                }
                UiCategory(
                    id = 0,
                    name = data.name,
                    isExpense = true,
                    isCustom = true,
                    count = 0,
                    color = color,
                    icon = CategoryIconProvider.getIconByName(data.iconName),
                    original = Category.expense(data.name)
                )
            }
            val customIncomeCategories = savedIncomeCategories.map { data ->
                val color = data.colorHex?.let {
                    try {
                        Color(it.toColorInt())
                    } catch (e: IllegalArgumentException) {
                        Timber.w(
                            e,
                            "Invalid color hex for income category '${data.name}': $it. Generating new one."
                        )
                        val generated = CategoryProvider.generateCategoryColorFromPalette(
                            isExpense = false
                        )
                        val newColorHex = generated.toHexString()
                        val updated = data.copy(colorHex = newColorHex)
                        val updatedList = savedIncomeCategories.map { cat -> if (cat.name == data.name) updated else cat }
                        categoryPreferences.saveIncomeCategories(updatedList)
                        generated
                    }
                } ?: run {
                    val generated = CategoryProvider.generateCategoryColorFromPalette(
                        isExpense = false
                    )
                    val colorHex = generated.toHexString()
                    val updated = data.copy(colorHex = colorHex)
                    val updatedList = savedIncomeCategories.map { cat -> if (cat.name == data.name) updated else cat }
                    categoryPreferences.saveIncomeCategories(updatedList)
                    generated
                }
                UiCategory(
                    id = 0,
                    name = data.name,
                    isExpense = false,
                    isCustom = true,
                    count = 0,
                    color = color,
                    icon = CategoryIconProvider.getIconByName(data.iconName),
                    original = Category.income(data.name)
                )
            }

            // Всегда оставляем категорию "Другое" в конце списка
            val otherExpenseCategory = allDefaultExpenseCategories.find { it.name == "Другое" } ?: allDefaultExpenseCategories.last()
            val otherIncomeCategory = allDefaultIncomeCategories.find { it.name == "Другое" } ?: allDefaultIncomeCategories.last()

            // Объединяем все категории (кроме "Другое")
            val allExpenseCategories = (filteredDefaultExpenseCategories + customExpenseCategories).distinctBy { it.name }
            val allIncomeCategories = (filteredDefaultIncomeCategories + customIncomeCategories).distinctBy { it.name }

            // Сортируем категории по частоте использования (по убыванию)
            val sortedExpenseCategories = allExpenseCategories.sortedByDescending {
                expenseCategoriesUsage[it.name] ?: 0
            }
            val sortedIncomeCategories = allIncomeCategories.sortedByDescending {
                incomeCategoriesUsage[it.name] ?: 0
            }

            // Добавляем "Другое" в конец списка
            _expenseCategories.value = sortedExpenseCategories.let { list ->
                val other = list.find { it.name == "Другое" } ?: otherExpenseCategory
                (list.filterNot { it.name == "Другое" } + other)
            }
            _incomeCategories.value = sortedIncomeCategories.let { list ->
                val other = list.find { it.name == "Другое" } ?: otherIncomeCategory
                (list.filterNot { it.name == "Другое" } + other)
            }

            Timber.d(
                "[CategoriesVM] Итоговые категории расходов: %s",
                _expenseCategories.value.joinToString { "${it.name}: ${it.color.toHexString()}" }
            )
            Timber.d(
                "[CategoriesVM] Итоговые категории доходов: %s",
                _incomeCategories.value.joinToString { "${it.name}: ${it.color.toHexString()}" }
            )
        }
    }

    /**
     * Добавляет новую пользовательскую категорию
     */
    fun addCustomCategory(categoryName: String, isExpense: Boolean, icon: ImageVector?) {
        if (categoryName.isBlank()) return
        val iconName = CategoryIconProvider.getIconName(icon)
        val color = CategoryProvider.generateCategoryColorFromPalette(isExpense)
        val colorHex = color.toHexString()
        val customCategoryData = CustomCategoryData(categoryName, iconName, colorHex)
        viewModelScope.launch {
            if (isExpense) {
                categoryPreferences.addExpenseCategory(customCategoryData)
                val uiCategory = UiCategory(
                    0,
                    categoryName,
                    true,
                    true,
                    0,
                    color = color,
                    icon = icon,
                    original = Category.expense(categoryName)
                )
                val currentCategories = _expenseCategories.value.toMutableList()
                val otherIndex = currentCategories.indexOfFirst { it.name == "Другое" }
                if (otherIndex != -1) {
                    currentCategories.add(otherIndex, uiCategory)
                } else {
                    currentCategories.add(uiCategory)
                }
                _expenseCategories.value = currentCategories.distinctBy { it.name }

                // Логируем событие в аналитику
                com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logCategoryAdded(
                    category = categoryName,
                    isExpense = true
                )
            } else {
                categoryPreferences.addIncomeCategory(customCategoryData)
                val uiCategory = UiCategory(
                    0,
                    categoryName,
                    false,
                    true,
                    0,
                    color = color,
                    icon = icon,
                    original = Category.income(categoryName)
                )
                val currentCategories = _incomeCategories.value.toMutableList()
                val otherIndex = currentCategories.indexOfFirst { it.name == "Другое" }
                if (otherIndex != -1) {
                    currentCategories.add(otherIndex, uiCategory)
                } else {
                    currentCategories.add(uiCategory)
                }
                _incomeCategories.value = currentCategories.distinctBy { it.name }

                // Логируем событие в аналитику
                com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logCategoryAdded(
                    category = categoryName,
                    isExpense = false
                )
            }
        }
    }

    /**
     * Удаляет категорию
     */
    fun removeCategory(categoryName: String, isExpense: Boolean) {
        viewModelScope.launch {
            if (isExpense) {
                val isDefaultCategory = CategoryProvider.getDefaultExpenseCategories(
                    getApplication<Application>().applicationContext
                )
                    .any { it.name == categoryName && it.name != "Другое" }

                if (isDefaultCategory) {
                    categoryPreferences.addDeletedDefaultExpenseCategory(categoryName)
                } else {
                    categoryPreferences.removeExpenseCategory(categoryName)
                }
                _expenseCategories.value = _expenseCategories.value.filterNot { it.name == categoryName }

                // Логируем событие в аналитику
                com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logCategoryDeleted(
                    category = categoryName,
                    isExpense = true
                )
            } else {
                val isDefaultCategory = CategoryProvider.getDefaultIncomeCategories(
                    getApplication<Application>().applicationContext
                )
                    .any { it.name == categoryName && it.name != "Другое" }

                if (isDefaultCategory) {
                    categoryPreferences.addDeletedDefaultIncomeCategory(categoryName)
                } else {
                    categoryPreferences.removeIncomeCategory(categoryName)
                }
                _incomeCategories.value = _incomeCategories.value.filterNot { it.name == categoryName }

                // Логируем событие в аналитику
                com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logCategoryDeleted(
                    category = categoryName,
                    isExpense = false
                )
            }
        }
    }

    /**
     * Проверяет, является ли категория расходов стандартной
     */
    fun isDefaultExpenseCategory(category: String): Boolean {
        return CategoryProvider.getDefaultExpenseCategories(
            getApplication<Application>().applicationContext
        ).any { it.name == category }
    }

    /**
     * Проверяет, является ли категория доходов стандартной
     */
    fun isDefaultIncomeCategory(category: String): Boolean {
        return CategoryProvider.getDefaultIncomeCategories(
            getApplication<Application>().applicationContext
        ).any { it.name == category }
    }

    /**
     * Удаляет категорию расходов (для использования в TransactionHistoryViewModel)
     */
    fun deleteExpenseCategory(category: String) {
        // Не даем удалить "Другое"
        if (category == "Другое") return

        removeCategory(category, true)
    }

    /**
     * Удаляет категорию доходов (для использования в TransactionHistoryViewModel)
     */
    fun deleteIncomeCategory(category: String) {
        // Не даем удалить "Другое"
        if (category == "Другое") return

        removeCategory(category, false)
    }
} 
