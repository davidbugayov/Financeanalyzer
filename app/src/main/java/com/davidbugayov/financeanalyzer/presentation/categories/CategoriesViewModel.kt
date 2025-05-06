package com.davidbugayov.financeanalyzer.presentation.categories

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences.CustomCategoryData
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryColorProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryIconProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import com.davidbugayov.financeanalyzer.R

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
                val color = data.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: run {
                    val generated = CategoryProvider.generateRandomCategoryColor()
                    // Сохраняем сгенерированный цвет обратно в preferences
                    val colorHex = String.format("#%02X%02X%02X", (generated.red * 255).toInt(), (generated.green * 255).toInt(), (generated.blue * 255).toInt())
                    // Обновляем preferences только если colorHex был null
                    val updated = data.copy(colorHex = colorHex)
                    val updatedList = savedExpenseCategories.map { if (it.name == data.name) updated else it }
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
                val color = data.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: run {
                    val generated = CategoryProvider.generateRandomCategoryColor()
                    val colorHex = String.format("#%02X%02X%02X", (generated.red * 255).toInt(), (generated.green * 255).toInt(), (generated.blue * 255).toInt())
                    val updated = data.copy(colorHex = colorHex)
                    val updatedList = savedIncomeCategories.map { if (it.name == data.name) updated else it }
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
            val otherExpenseCategory = allDefaultExpenseCategories.last()
            val otherIncomeCategory = allDefaultIncomeCategories.last()

            // Объединяем все категории (кроме "Другое")
            val allExpenseCategories = (filteredDefaultExpenseCategories + customExpenseCategories)
            val allIncomeCategories = (filteredDefaultIncomeCategories + customIncomeCategories)

            // Сортируем категории по частоте использования (по убыванию)
            val sortedExpenseCategories = allExpenseCategories.sortedByDescending {
                expenseCategoriesUsage[it.name] ?: 0
            }
            val sortedIncomeCategories = allIncomeCategories.sortedByDescending {
                incomeCategoriesUsage[it.name] ?: 0
            }

            // Добавляем "Другое" в конец списка
            _expenseCategories.value = sortedExpenseCategories + listOf(otherExpenseCategory)
            _incomeCategories.value = sortedIncomeCategories + listOf(otherIncomeCategory)

            Timber.d("[CategoriesVM] Итоговые категории расходов: " + _expenseCategories.value.joinToString { "${'$'}{it.name}: ${'$'}{it.color}" })
            Timber.d("[CategoriesVM] Итоговые категории доходов: " + _incomeCategories.value.joinToString { "${'$'}{it.name}: ${'$'}{it.color}" })
        }
    }

    /**
     * Добавляет новую пользовательскую категорию
     */
    fun addCustomCategory(category: String, isExpense: Boolean, icon: ImageVector?) {
        if (category.isBlank()) return
        val iconName = CategoryIconProvider.getIconName(icon)
        val color = CategoryProvider.generateRandomCategoryColor()
        val colorHex = String.format("#%02X%02X%02X", (color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt())
        val customCategoryData = CustomCategoryData(category, iconName, colorHex)
        viewModelScope.launch {
            if (isExpense) {
                categoryPreferences.addExpenseCategory(customCategoryData)
                val customCategory = UiCategory(0, category, true, true, 0, color = color, icon = icon)
                val currentCategories = _expenseCategories.value.toMutableList()
                currentCategories.add(currentCategories.size - 1, customCategory)
                _expenseCategories.value = currentCategories
            } else {
                categoryPreferences.addIncomeCategory(customCategoryData)
                val customCategory = UiCategory(0, category, false, true, 0, color = color, icon = icon)
                val currentCategories = _incomeCategories.value.toMutableList()
                currentCategories.add(currentCategories.size - 1, customCategory)
                _incomeCategories.value = currentCategories
            }
        }
    }

    /**
     * Удаляет категорию
     */
    fun removeCategory(category: String, isExpense: Boolean) {
        viewModelScope.launch {
            if (isExpense) {
                // Проверяем, является ли категория дефолтной
                val isDefaultCategory = CategoryProvider.getDefaultExpenseCategories(getApplication<Application>().applicationContext).any { it.name == category && it.name != "Другое" }

                if (isDefaultCategory) {
                    // Если это дефолтная категория, добавляем ее в список удаленных дефолтных категорий
                    categoryPreferences.addDeletedDefaultExpenseCategory(category)
                } else {
                    // Если это пользовательская категория, удаляем ее из списка пользовательских категорий
                    categoryPreferences.removeExpenseCategory(category)
                }

                // Удаляем категорию из текущего списка
                _expenseCategories.value = _expenseCategories.value.filterNot { it.name == category }
            } else {
                // Проверяем, является ли категория дефолтной
                val isDefaultCategory = CategoryProvider.getDefaultIncomeCategories(getApplication<Application>().applicationContext).any { it.name == category && it.name != "Другое" }

                if (isDefaultCategory) {
                    // Если это дефолтная категория, добавляем ее в список удаленных дефолтных категорий
                    categoryPreferences.addDeletedDefaultIncomeCategory(category)
                } else {
                    // Если это пользовательская категория, удаляем ее из списка пользовательских категорий
                    categoryPreferences.removeIncomeCategory(category)
                }

                // Удаляем категорию из текущего списка
                _incomeCategories.value = _incomeCategories.value.filterNot { it.name == category }
            }
        }
    }

    /**
     * Увеличивает счетчик использования категории и обновляет порядок категорий
     */
    fun incrementCategoryUsage(category: String, isExpense: Boolean) {
        viewModelScope.launch {
            if (isExpense) {
                categoryUsagePreferences.incrementExpenseCategoryUsage(category)
            } else {
                categoryUsagePreferences.incrementIncomeCategoryUsage(category)
            }
            // Перезагружаем категории с новой статистикой использования
            loadCategories()
        }
    }

    /**
     * Проверяет, является ли категория расходов стандартной
     */
    fun isDefaultExpenseCategory(category: String): Boolean {
        return CategoryProvider.getDefaultExpenseCategories(getApplication<Application>().applicationContext).any { it.name == category }
    }

    /**
     * Проверяет, является ли категория доходов стандартной
     */
    fun isDefaultIncomeCategory(category: String): Boolean {
        return CategoryProvider.getDefaultIncomeCategories(getApplication<Application>().applicationContext).any { it.name == category }
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