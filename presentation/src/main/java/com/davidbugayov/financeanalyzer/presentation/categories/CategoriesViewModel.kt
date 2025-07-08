package com.davidbugayov.financeanalyzer.presentation.categories

import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import kotlinx.coroutines.flow.StateFlow

/**
 * Lightweight interface used by feature modules to interact with categories without
 * depending on a concrete implementation. Real implementation is provided in the
 * application module via DI.
 */
interface CategoriesViewModel {
    val expenseCategories: StateFlow<List<UiCategory>>
    val incomeCategories: StateFlow<List<UiCategory>>

    fun addCustomCategory(
        name: String,
        isExpense: Boolean,
        icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    )

    fun deleteExpenseCategory(name: String)

    fun deleteIncomeCategory(name: String)

    fun isDefaultExpenseCategory(
        category: com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory,
    ): Boolean = category.isExpense && !category.isCustom

    fun isDefaultIncomeCategory(
        category: com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory,
    ): Boolean = !category.isExpense && !category.isCustom
}
