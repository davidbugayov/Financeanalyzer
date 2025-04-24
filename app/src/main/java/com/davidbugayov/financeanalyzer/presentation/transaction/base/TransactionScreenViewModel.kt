package com.davidbugayov.financeanalyzer.presentation.transaction.base

import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import kotlinx.coroutines.flow.StateFlow

interface TransactionScreenViewModel<S, E> {

    val state: StateFlow<S>
    val wallets: List<Wallet>
    fun onEvent(event: E, context: android.content.Context)
    fun resetFields()
    fun updateCategoryPositions()
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
    val expenseCategories: List<com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem>
    val incomeCategories: List<com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem>
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
    val customCategoryIcon: ImageVector
    val availableCategoryIcons: List<ImageVector>
}

fun defaultTransactionEventFactory(isEditMode: Boolean = false): (Any) -> BaseTransactionEvent = { eventData ->
    when (eventData) {
        is com.davidbugayov.financeanalyzer.domain.model.Source -> BaseTransactionEvent.SetSource(eventData.name)
        is com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem -> BaseTransactionEvent.SetCategory(eventData.name)
        is java.util.Date -> BaseTransactionEvent.SetDate(eventData)
        is BaseTransactionEvent -> eventData
        else -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
    }
} 