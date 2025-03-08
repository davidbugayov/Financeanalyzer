package com.davidbugayov.financeanalyzer.presentation.categories

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel для управления категориями транзакций.
 * Следует принципам MVVM и Clean Architecture.
 */
class CategoriesViewModel(
    application: Application
) : AndroidViewModel(application), KoinComponent {

    private val categoryPreferences: CategoryPreferences by inject()

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
        CategoryItem("Прочее", Icons.Default.MoreHoriz),
        CategoryItem("Другое", Icons.Default.Add)
    )

    private val defaultIncomeCategories = listOf(
        CategoryItem("Зарплата", Icons.Default.Payments),
        CategoryItem("Фриланс", Icons.Default.Computer),
        CategoryItem("Подарки", Icons.Default.CardGiftcard),
        CategoryItem("Проценты", Icons.AutoMirrored.Filled.TrendingUp),
        CategoryItem("Аренда", Icons.Default.HomeWork),
        CategoryItem("Прочее", Icons.Default.MoreHoriz),
        CategoryItem("Другое", Icons.Default.Add)
    )

    private val _expenseCategories = MutableStateFlow(defaultExpenseCategories)
    val expenseCategories: StateFlow<List<CategoryItem>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow(defaultIncomeCategories)
    val incomeCategories: StateFlow<List<CategoryItem>> = _incomeCategories.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Загружает сохраненные категории
     */
    private fun loadCategories() {
        viewModelScope.launch {
            val savedExpenseCategories = categoryPreferences.loadExpenseCategories()
            val savedIncomeCategories = categoryPreferences.loadIncomeCategories()

            // Добавляем пользовательские категории перед "Другое"
            val customExpenseCategories = savedExpenseCategories.map {
                CategoryItem(it, Icons.Default.MoreHoriz)
            }
            val customIncomeCategories = savedIncomeCategories.map {
                CategoryItem(it, Icons.Default.MoreHoriz)
            }

            _expenseCategories.value = defaultExpenseCategories.dropLast(1) +
                    customExpenseCategories +
                    defaultExpenseCategories.takeLast(1)

            _incomeCategories.value = defaultIncomeCategories.dropLast(1) +
                    customIncomeCategories +
                    defaultIncomeCategories.takeLast(1)
        }
    }

    /**
     * Добавляет новую пользовательскую категорию
     */
    fun addCustomCategory(category: String, isExpense: Boolean) {
        if (category.isBlank()) return

        viewModelScope.launch {
            if (isExpense) {
                categoryPreferences.addExpenseCategory(category)
                val customCategory = CategoryItem(category, Icons.Default.MoreHoriz)
                val currentCategories = _expenseCategories.value.toMutableList()
                // Добавляем перед "Другое"
                currentCategories.add(currentCategories.size - 1, customCategory)
                _expenseCategories.value = currentCategories
            } else {
                categoryPreferences.addIncomeCategory(category)
                val customCategory = CategoryItem(category, Icons.Default.MoreHoriz)
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
                categoryPreferences.removeExpenseCategory(category)
                _expenseCategories.value = _expenseCategories.value.filterNot { it.name == category }
            } else {
                categoryPreferences.removeIncomeCategory(category)
                _incomeCategories.value = _incomeCategories.value.filterNot { it.name == category }
            }
        }
    }
} 