package com.davidbugayov.financeanalyzer.presentation.add.model

import com.davidbugayov.financeanalyzer.domain.model.Source
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
    val showCancelConfirmation: Boolean = false,
    val customCategory: String = "",
    val expenseCategories: List<CategoryItem> = emptyList(),
    val incomeCategories: List<CategoryItem> = emptyList(),
    val titleError: Boolean = false,
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val source: String = "Сбер",
    val sourceColor: Int = 0xFF21A038.toInt(), // Цвет Сбера
    val sourceError: Boolean = false,
    val showSourcePicker: Boolean = false,
    val showCustomSourceDialog: Boolean = false,
    val showColorPicker: Boolean = false,
    val customSource: String = "",
    val sources: List<Source> = emptyList()
) 