package com.davidbugayov.financeanalyzer.presentation.add.model

import java.util.Date

/**
 * События экрана добавления транзакции.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 */
sealed class AddTransactionEvent {

    data class SetTitle(val title: String) : AddTransactionEvent()
    data class SetAmount(val amount: String) : AddTransactionEvent()
    data class SetCategory(val category: String) : AddTransactionEvent()
    data class SetNote(val note: String) : AddTransactionEvent()
    data class SetDate(val date: Date) : AddTransactionEvent()
    data class SetCustomCategory(val category: String) : AddTransactionEvent()
    data class AddCustomCategory(val category: String) : AddTransactionEvent()
    object ToggleTransactionType : AddTransactionEvent()
    object ShowDatePicker : AddTransactionEvent()
    object HideDatePicker : AddTransactionEvent()
    object ShowCategoryPicker : AddTransactionEvent()
    object HideCategoryPicker : AddTransactionEvent()
    object ShowCustomCategoryDialog : AddTransactionEvent()
    object HideCustomCategoryDialog : AddTransactionEvent()
    object ShowCancelConfirmation : AddTransactionEvent()
    object HideCancelConfirmation : AddTransactionEvent()
    object Submit : AddTransactionEvent()
    object ClearError : AddTransactionEvent()
    object HideSuccessDialog : AddTransactionEvent()
} 