package com.davidbugayov.financeanalyzer.presentation.add.state

import java.util.Date

/**
 * Состояние экрана добавления транзакции.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
data class AddTransactionState(
    val title: String = "",
    val amount: String = "",
    val category: String = "",
    val note: String = "",
    val isExpense: Boolean = true,
    val selectedDate: Date = Date(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val customCategory: String = ""
) 