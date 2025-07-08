package com.davidbugayov.financeanalyzer.feature.transaction.base

import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import kotlinx.coroutines.flow.StateFlow

interface TransactionScreenViewModel<S, E> {
    val state: StateFlow<S>
    val wallets: List<Wallet>

    fun onEvent(
        event: E,
        context: android.content.Context,
    )

    fun resetFields()

    fun updateCategoryPositions()

    fun updateSourcePositions()

    fun submitTransaction(context: android.content.Context)

    fun clearSelectedWallets()

    fun selectAllWallets(context: android.content.Context)
}

interface BaseTransactionState {
    val title: String
    val amount: String
    val amountError: Boolean
    val category: String
    val categoryError: Boolean
    val note: String
    val selectedDate: java.util.Date
    val isExpense: Boolean
    val showDatePicker: Boolean
    val showCategoryPicker: Boolean
    val showCustomCategoryDialog: Boolean
    val showCancelConfirmation: Boolean
    val customCategory: String
    val showSourcePicker: Boolean
    val showCustomSourceDialog: Boolean
    val customSource: String
    val source: String
    val sourceColor: Int
    val showColorPicker: Boolean
    val isLoading: Boolean
    val error: String?
    val isSuccess: Boolean
    val successMessage: String
    val expenseCategories: List<UiCategory>
    val incomeCategories: List<UiCategory>
    val sources: List<com.davidbugayov.financeanalyzer.domain.model.Source>
    val categoryToDelete: String?
    val sourceToDelete: String?
    val showDeleteCategoryConfirmDialog: Boolean
    val showDeleteSourceConfirmDialog: Boolean
    val editMode: Boolean
    val transactionToEdit: com.davidbugayov.financeanalyzer.domain.model.Transaction?
    val addToWallet: Boolean
    val selectedWallets: List<String>
    val showWalletSelector: Boolean
    val targetWalletId: String?
    val forceExpense: Boolean
    val sourceError: Boolean
    val preventAutoSubmit: Boolean
    val selectedExpenseCategory: String
    val selectedIncomeCategory: String
    val selectedCategory: UiCategory?
    val categories: List<UiCategory>
    val availableCategoryIcons: List<ImageVector>
    val customCategoryIcon: ImageVector?
}

fun defaultTransactionEventFactory(isEditMode: Boolean = false): (Any) -> BaseTransactionEvent =
    { eventData ->
        when (eventData) {
            is com.davidbugayov.financeanalyzer.domain.model.Source ->
                BaseTransactionEvent.SetSource(
                    eventData.name,
                )
            is UiCategory -> BaseTransactionEvent.SetCategory(eventData.name)
            is java.util.Date -> BaseTransactionEvent.SetDate(eventData)
            is BaseTransactionEvent -> eventData
            else -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
        }
    }
