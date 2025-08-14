package com.davidbugayov.financeanalyzer.presentation.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Default implementation of [CategoriesViewModel] that holds category lists in memory.
 * Real persistence (DB/Prefs) may be added later, but this is enough for compilation
 * and basic runtime usage inside feature modules.
 */
class AppCategoriesViewModel(
    application: Application,
) : AndroidViewModel(application),
    CategoriesViewModel {
    private val _expenseCategories =
        MutableStateFlow<List<UiCategory>>(
            CategoryProvider.getDefaultExpenseCategories(application),
        )
    override val expenseCategories: StateFlow<List<UiCategory>> = _expenseCategories

    private val _incomeCategories =
        MutableStateFlow<List<UiCategory>>(
            CategoryProvider.getDefaultIncomeCategories(application),
        )
    override val incomeCategories: StateFlow<List<UiCategory>> = _incomeCategories

    override fun addCustomCategory(
        name: String,
        isExpense: Boolean,
        icon: androidx.compose.ui.graphics.vector.ImageVector?,
    ) {
        val newCategory =
            UiCategory.custom(
                name = name,
                isExpense = isExpense,
                icon = icon,
                color = CategoryProvider.ensureNonBlackWhite(CategoryProvider.generateRandomCategoryColor()),
            )
        if (isExpense) {
            _expenseCategories.update { it + newCategory }
        } else {
            _incomeCategories.update { it + newCategory }
        }
    }

    override fun deleteExpenseCategory(name: String) {
        _expenseCategories.update { list -> list.filterNot { it.name == name } }
    }

    override fun deleteIncomeCategory(name: String) {
        _incomeCategories.update { list -> list.filterNot { it.name == name } }
    }
}
