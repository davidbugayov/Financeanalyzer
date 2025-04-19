package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import java.util.Date

sealed class BaseTransactionEvent {
    data class SetTitle(val title: String) : BaseTransactionEvent()
    data class SetAmount(val amount: String) : BaseTransactionEvent()
    data class SetCategory(val category: String) : BaseTransactionEvent()
    data class SetNote(val note: String) : BaseTransactionEvent()
    data class SetDate(val date: Date) : BaseTransactionEvent()
    data class SetCustomCategory(val category: String) : BaseTransactionEvent()
    data class AddCustomCategory(val category: String) : BaseTransactionEvent()
    data class SetSource(val source: String) : BaseTransactionEvent()
    data class SetCustomSource(val source: String) : BaseTransactionEvent()
    data class AddCustomSource(val source: String, val color: Int) : BaseTransactionEvent()
    data class SetSourceColor(val color: Int) : BaseTransactionEvent()
    data class DeleteCategory(val category: String) : BaseTransactionEvent()
    data class DeleteSource(val source: String) : BaseTransactionEvent()
    data class ShowDeleteCategoryConfirmDialog(val category: String) : BaseTransactionEvent()
    data class ShowDeleteSourceConfirmDialog(val source: String) : BaseTransactionEvent()
    object HideDeleteCategoryConfirmDialog : BaseTransactionEvent()
    object HideDeleteSourceConfirmDialog : BaseTransactionEvent()
    object ToggleTransactionType : BaseTransactionEvent()
    object ShowDatePicker : BaseTransactionEvent()
    object HideDatePicker : BaseTransactionEvent()
    object ShowCategoryPicker : BaseTransactionEvent()
    object HideCategoryPicker : BaseTransactionEvent()
    object ShowCustomCategoryDialog : BaseTransactionEvent()
    object HideCustomCategoryDialog : BaseTransactionEvent()
    object ShowCancelConfirmation : BaseTransactionEvent()
    object HideCancelConfirmation : BaseTransactionEvent()
    object ShowSourcePicker : BaseTransactionEvent()
    object HideSourcePicker : BaseTransactionEvent()
    object ShowCustomSourceDialog : BaseTransactionEvent()
    object HideCustomSourceDialog : BaseTransactionEvent()
    object ShowColorPicker : BaseTransactionEvent()
    object HideColorPicker : BaseTransactionEvent()
    object ClearError : BaseTransactionEvent()
    object HideSuccessDialog : BaseTransactionEvent()
    // Для управления кошельками
    data class SetTargetWalletId(val walletId: String) : BaseTransactionEvent()
    object ToggleAddToWallet : BaseTransactionEvent()
    object ShowWalletSelector : BaseTransactionEvent()
    object HideWalletSelector : BaseTransactionEvent()
    data class SelectWallet(val walletId: String, val selected: Boolean) : BaseTransactionEvent()
    data class SelectWallets(val walletIds: List<String>) : BaseTransactionEvent()
    // ...другие общие события, если потребуется
    object Submit : BaseTransactionEvent()
    object SubmitEdit : BaseTransactionEvent()
    object ForceSetIncomeType : BaseTransactionEvent()
    object ForceSetExpenseType : BaseTransactionEvent()
    data class LoadTransaction(val id: String) : BaseTransactionEvent()
} 