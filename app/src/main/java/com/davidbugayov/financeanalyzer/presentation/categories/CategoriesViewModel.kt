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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.data.preferences.CategoryUsagePreferences
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
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

    private val defaultExpenseCategories = listOf(
        CategoryItem("Продукты", Icons.Default.ShoppingCart),
        CategoryItem("Транспорт", Icons.Default.DirectionsCar),
        CategoryItem("Развлечения", Icons.Default.Movie),
        CategoryItem("Рестораны", Icons.Default.Restaurant),
        CategoryItem("Здоровье", Icons.Default.LocalHospital),
        CategoryItem("Одежда", Icons.Default.Checkroom),
        CategoryItem("Жилье", Icons.Default.Home),
        CategoryItem("Связь", Icons.Default.Phone),
        CategoryItem("Питомец", Icons.Default.Pets),
        CategoryItem("Услуги", Icons.Default.Work),
        CategoryItem("Благотворительность", Icons.Default.Payments),
        CategoryItem("Кредит", Icons.Default.CreditCard),
        CategoryItem("Переводы", Icons.Default.SwapHoriz),
        CategoryItem("Другое", Icons.Default.Add)
    )

    private val defaultIncomeCategories = listOf(
        CategoryItem("Зарплата", Icons.Default.Payments),
        CategoryItem("Фриланс", Icons.Default.Computer),
        CategoryItem("Подарки", Icons.Default.Payments),
        CategoryItem("Проценты", Icons.AutoMirrored.Filled.TrendingUp),
        CategoryItem("Аренда", Icons.Default.Work),
        CategoryItem("Прочее", Icons.Default.MoreHoriz),
        CategoryItem("Другое", Icons.Default.Add)
    )

    private val _expenseCategories = MutableStateFlow(defaultExpenseCategories)
    val expenseCategories: StateFlow<List<CategoryItem>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow(defaultIncomeCategories)
    val incomeCategories: StateFlow<List<CategoryItem>> = _incomeCategories.asStateFlow()

    init {
        Timber.d("[CategoriesVM] CategoriesViewModel создан: $this")
        loadCategories()
    }

    /**
     * Загружает сохраненные категории и сортирует их по частоте использования
     */
    private fun loadCategories() {
        viewModelScope.launch {
            val savedExpenseCategories = categoryPreferences.loadExpenseCategories()
            val savedIncomeCategories = categoryPreferences.loadIncomeCategories()
            val deletedDefaultExpenseCategories = categoryPreferences.loadDeletedDefaultExpenseCategories()
            val deletedDefaultIncomeCategories = categoryPreferences.loadDeletedDefaultIncomeCategories()

            // Загружаем статистику использования категорий
            val expenseCategoriesUsage = categoryUsagePreferences.loadExpenseCategoriesUsage()
            val incomeCategoriesUsage = categoryUsagePreferences.loadIncomeCategoriesUsage()

            // Фильтруем дефолтные категории, исключая удаленные
            val filteredDefaultExpenseCategories = defaultExpenseCategories.filter {
                !deletedDefaultExpenseCategories.contains(it.name) && it.name != "Другое"
            }

            val filteredDefaultIncomeCategories = defaultIncomeCategories.filter {
                !deletedDefaultIncomeCategories.contains(it.name) && it.name != "Другое"
            }

            // Добавляем пользовательские категории
            val customExpenseCategories = savedExpenseCategories.map {
                CategoryItem(it, Icons.Default.MoreHoriz)
            }
            val customIncomeCategories = savedIncomeCategories.map {
                CategoryItem(it, Icons.Default.MoreHoriz)
            }

            // Всегда оставляем категорию "Другое" в конце списка
            val otherExpenseCategory = defaultExpenseCategories.last()
            val otherIncomeCategory = defaultIncomeCategories.last()

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
        }
    }

    /**
     * Добавляет новую пользовательскую категорию
     */
    fun addCustomCategory(category: String, isExpense: Boolean, icon: ImageVector = Icons.Default.MoreHoriz) {
        if (category.isBlank()) return

        viewModelScope.launch {
            if (isExpense) {
                categoryPreferences.addExpenseCategory(category)
                val customCategory = CategoryItem(category, icon)
                val currentCategories = _expenseCategories.value.toMutableList()
                // Добавляем перед "Другое"
                currentCategories.add(currentCategories.size - 1, customCategory)
                _expenseCategories.value = currentCategories
            } else {
                categoryPreferences.addIncomeCategory(category)
                val customCategory = CategoryItem(category, icon)
                val currentCategories = _incomeCategories.value.toMutableList()
                // Добавляем перед "Другое"
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
                val isDefaultCategory = defaultExpenseCategories.any { it.name == category && it.name != "Другое" }

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
                val isDefaultCategory = defaultIncomeCategories.any { it.name == category && it.name != "Другое" }

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
        return defaultExpenseCategories.any { it.name == category }
    }

    /**
     * Проверяет, является ли категория доходов стандартной
     */
    fun isDefaultIncomeCategory(category: String): Boolean {
        return defaultIncomeCategories.any { it.name == category }
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