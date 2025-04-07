package com.davidbugayov.financeanalyzer.presentation.budget.model

import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory

data class BudgetState(
    val categories: List<BudgetCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddCategoryDialog: Boolean = false,
    val selectedCategory: BudgetCategory? = null
) 