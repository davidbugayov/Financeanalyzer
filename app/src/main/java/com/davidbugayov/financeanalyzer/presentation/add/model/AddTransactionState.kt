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
    val amountError: Boolean = false,
    val category: String = "",
    val categoryError: Boolean = false,
    val note: String = "",
    val selectedDate: Date = Date(),
    val isExpense: Boolean = true,
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val showCancelConfirmation: Boolean = false,
    val customCategory: String = "",
    val showSourcePicker: Boolean = false,
    val showCustomSourceDialog: Boolean = false,
    val customSource: String = "",
    val source: String = "Сбер",
    val sourceColor: Int = 0xFF21A038.toInt(), // Цвет Сбера
    val showColorPicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val expenseCategories: List<CategoryItem> = emptyList(),
    val incomeCategories: List<CategoryItem> = emptyList(),
    val sources: List<Source> = emptyList(),
    val categoryToDelete: String? = null,
    val sourceToDelete: String? = null,
    val showDeleteCategoryConfirmDialog: Boolean = false,
    val showDeleteSourceConfirmDialog: Boolean = false
) 