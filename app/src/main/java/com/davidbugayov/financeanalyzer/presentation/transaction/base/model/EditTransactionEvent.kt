package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import java.util.Date

sealed class EditTransactionEvent : BaseTransactionEvent() {
    data class LoadTransaction(val id: String) : EditTransactionEvent()
    object SubmitEdit : EditTransactionEvent()
    // События для работы с источниками
    data class SetSource(val source: String) : EditTransactionEvent()
    data class SetSourceColor(val color: Int) : EditTransactionEvent()
    object ShowSourcePicker : EditTransactionEvent()
    object HideSourcePicker : EditTransactionEvent()
    object ShowCustomSourceDialog : EditTransactionEvent()
    object HideCustomSourceDialog : EditTransactionEvent()
    data class AddCustomSource(val source: String, val color: Int) : EditTransactionEvent()
    data class SetCustomSource(val source: String) : EditTransactionEvent()
    data class ShowDeleteSourceConfirmDialog(val source: String) : EditTransactionEvent()
    object HideDeleteSourceConfirmDialog : EditTransactionEvent()
    data class DeleteSource(val source: String) : EditTransactionEvent()
    // ...другие специфичные для редактирования события
    object ShowCustomCategoryDialog : EditTransactionEvent()
    object HideCustomCategoryDialog : EditTransactionEvent()
    data class AddCustomCategory(val category: String) : EditTransactionEvent()
    data class SetCustomCategory(val category: String) : EditTransactionEvent()
    data class ShowDeleteCategoryConfirmDialog(val category: String) : EditTransactionEvent()
    object HideDeleteCategoryConfirmDialog : EditTransactionEvent()
    data class DeleteCategory(val category: String) : EditTransactionEvent()
    // Базовые события из BaseTransactionEvent
    object ToggleTransactionType : EditTransactionEvent()
    data class SetAmount(val amount: String) : EditTransactionEvent()
    data class SetNote(val note: String) : EditTransactionEvent()
    data class SetCategory(val category: String) : EditTransactionEvent()
    object ShowDatePicker : EditTransactionEvent()
    object HideDatePicker : EditTransactionEvent()
    data class SetDate(val date: Date) : EditTransactionEvent()
    object ClearError : EditTransactionEvent()
    object HideSuccessDialog : EditTransactionEvent()
    object HideColorPicker : EditTransactionEvent()
    object ShowWalletSelector : EditTransactionEvent()
    object HideWalletSelector : EditTransactionEvent()
    data class SelectWallet(val walletId: String, val selected: Boolean) : EditTransactionEvent()
    object ToggleAddToWallet : EditTransactionEvent()
} 