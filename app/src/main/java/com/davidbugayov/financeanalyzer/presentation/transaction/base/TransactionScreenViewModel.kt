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
        is String -> when (eventData) {
            if (isEditMode) "SubmitEdit" else "Submit" -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
            "ShowDatePicker" -> BaseTransactionEvent.ShowDatePicker
            "HideDatePicker" -> BaseTransactionEvent.HideDatePicker
            "ShowSourcePicker" -> BaseTransactionEvent.ShowSourcePicker
            "HideSourcePicker" -> BaseTransactionEvent.HideSourcePicker
            "ShowCustomSourceDialog" -> BaseTransactionEvent.ShowCustomSourceDialog
            "HideCustomSourceDialog" -> BaseTransactionEvent.HideCustomSourceDialog
            "ShowCustomCategoryDialog" -> BaseTransactionEvent.ShowCustomCategoryDialog
            "HideCustomCategoryDialog" -> BaseTransactionEvent.HideCustomCategoryDialog
            "ToggleTransactionType" -> BaseTransactionEvent.ToggleTransactionType
            "HideDeleteCategoryConfirmDialog" -> BaseTransactionEvent.HideDeleteCategoryConfirmDialog
            "HideDeleteSourceConfirmDialog" -> BaseTransactionEvent.HideDeleteSourceConfirmDialog
            "ClearError" -> BaseTransactionEvent.ClearError
            "HideSuccessDialog" -> BaseTransactionEvent.HideSuccessDialog
            "HideColorPicker" -> BaseTransactionEvent.HideColorPicker
            "HideWalletSelector" -> BaseTransactionEvent.HideWalletSelector
            "ToggleAddToWallet" -> BaseTransactionEvent.ToggleAddToWallet
            "ShowWalletSelector" -> BaseTransactionEvent.ShowWalletSelector
            "PreventAutoSubmit" -> BaseTransactionEvent.PreventAutoSubmit
            "ResetAmountOnly" -> BaseTransactionEvent.ResetAmountOnly
            else -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
        }
        is Pair<*, *> -> when (eventData.first as? String) {
            "SetExpenseCategory" -> BaseTransactionEvent.SetExpenseCategory(eventData.second as String)
            "SetIncomeCategory" -> BaseTransactionEvent.SetIncomeCategory(eventData.second as String)
            "SetSource" -> BaseTransactionEvent.SetSource(eventData.second as String)
            "ShowDeleteSourceConfirmDialog" -> BaseTransactionEvent.ShowDeleteSourceConfirmDialog(eventData.second as String)
            "DeleteSourceConfirm" -> {
                val source = eventData.second as? com.davidbugayov.financeanalyzer.domain.model.Source
                BaseTransactionEvent.ShowDeleteSourceConfirmDialog(source?.name ?: "")
            }
            "DeleteCategoryConfirm" -> {
                val category = eventData.second as? com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
                BaseTransactionEvent.ShowDeleteCategoryConfirmDialog(category?.name ?: "")
            }
            "SetAmount" -> BaseTransactionEvent.SetAmount(eventData.second as String)
            "SetNote" -> BaseTransactionEvent.SetNote(eventData.second as String)
            "SetCustomCategoryText" -> BaseTransactionEvent.SetCustomCategory(eventData.second as String)
            "AddCustomCategoryConfirm" -> BaseTransactionEvent.AddCustomCategory(eventData.second as String)
            "DeleteCategoryConfirmActual" -> BaseTransactionEvent.DeleteCategory(eventData.second as String)
            "DeleteSourceConfirmActual" -> BaseTransactionEvent.DeleteSource(eventData.second as String)
            "SetCustomSourceName" -> BaseTransactionEvent.SetCustomSource(eventData.second as String)
            "SetCustomSourceColor" -> BaseTransactionEvent.SetSourceColor(eventData.second as Int)
            "SetSourceColor" -> BaseTransactionEvent.SetSourceColor(eventData.second as Int)
            "SetCustomCategoryIcon" -> BaseTransactionEvent.SetCustomCategoryIcon(eventData.second as ImageVector)
            else -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
        }
        is Triple<*, *, *> -> when (eventData.first as? String) {
            "AddCustomSourceConfirm" -> {
                val name = eventData.second as String
                val color = eventData.third as Int
                BaseTransactionEvent.AddCustomSource(name, color)
            }
            "SelectWallet" -> {
                val walletId = eventData.second as String
                val selected = eventData.third as Boolean
                BaseTransactionEvent.SelectWallet(walletId, selected)
            }
            else -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
        }
        else -> if (isEditMode) BaseTransactionEvent.SubmitEdit else BaseTransactionEvent.Submit
    }
} 