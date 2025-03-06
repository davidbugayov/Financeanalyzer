package com.davidbugayov.financeanalyzer.presentation.add.event

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

/**
 * События для экрана добавления транзакции.
 * Следует принципу открытости/закрытости (OCP) из SOLID.
 */
sealed class AddTransactionEvent {

    data class SetTitle(val title: String) : AddTransactionEvent()
    data class SetAmount(val amount: String) : AddTransactionEvent()
    data class SetCategory(val category: String) : AddTransactionEvent()
    data class SetNote(val note: String) : AddTransactionEvent()
    data class SetExpenseType(val isExpense: Boolean) : AddTransactionEvent()
    data class SetDate(val date: Date) : AddTransactionEvent()
    data class AddTransaction(val transaction: Transaction) : AddTransactionEvent()
    data object ResetSuccess : AddTransactionEvent()
    data object ResetError : AddTransactionEvent()
    data object ShowDatePicker : AddTransactionEvent()
    data object HideDatePicker : AddTransactionEvent()
    data object ShowCategoryPicker : AddTransactionEvent()
    data object HideCategoryPicker : AddTransactionEvent()
    data object ShowCustomCategoryDialog : AddTransactionEvent()
    data object HideCustomCategoryDialog : AddTransactionEvent()
    data class SetCustomCategory(val category: String) : AddTransactionEvent()
    data class AddCustomCategory(val category: String) : AddTransactionEvent()
} 