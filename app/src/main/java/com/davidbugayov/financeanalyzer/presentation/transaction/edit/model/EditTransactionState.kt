package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem

/**
 * Состояние экрана редактирования транзакции.
 */
data class EditTransactionState(
    override val title: String = "",
    override val amount: String = "",
    override val amountError: Boolean = false,
    override val category: String = "",
    override val categoryError: Boolean = false,
    override val note: String = "",
    override val selectedDate: Date = Date(),
    override val isExpense: Boolean = true,
    override val showDatePicker: Boolean = false,
    override val showCategoryPicker: Boolean = false,
    override val showCustomCategoryDialog: Boolean = false,
    override val showCancelConfirmation: Boolean = false,
    override val customCategory: String = "",
    override val showSourcePicker: Boolean = false,
    override val showCustomSourceDialog: Boolean = false,
    override val customSource: String = "",
    override val source: String = "Сбер",
    override val sourceColor: Int = 0xFF21A038.toInt(),
    override val showColorPicker: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: String? = null,
    override val isSuccess: Boolean = false,
    override val successMessage: String = "Операция выполнена успешно",
    override val expenseCategories: List<CategoryItem> = emptyList(),
    override val incomeCategories: List<CategoryItem> = emptyList(),
    override val sources: List<Source> = emptyList(),
    override val categoryToDelete: String? = null,
    override val sourceToDelete: String? = null,
    override val showDeleteCategoryConfirmDialog: Boolean = false,
    override val showDeleteSourceConfirmDialog: Boolean = false,
    override val editMode: Boolean = true,
    override val transactionToEdit: Transaction? = null,
    override val addToWallet: Boolean = false,
    override val selectedWallets: List<String> = emptyList(),
    override val showWalletSelector: Boolean = false,
    override val targetWalletId: String? = null,
    override val forceExpense: Boolean = false,
    val isEdited: Boolean = false,
    override val sourceError: Boolean = false
) : com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionState 