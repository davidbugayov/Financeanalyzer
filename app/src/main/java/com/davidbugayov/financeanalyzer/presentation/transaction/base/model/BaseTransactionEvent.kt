package com.davidbugayov.financeanalyzer.presentation.transaction.base.model

import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.TransactionData
import java.util.Date

/**
 * Базовые события для экранов транзакций.
 * Включает общие события для добавления и редактирования транзакций.
 */
sealed class BaseTransactionEvent {
    // События для управления данными транзакции
    data class SetAmount(val amount: String) : BaseTransactionEvent()
    data class SetCategory(val category: String) : BaseTransactionEvent()
    data class SetNote(val note: String) : BaseTransactionEvent()
    data class SetDate(val date: Date) : BaseTransactionEvent()
    data class SetSource(val source: String) : BaseTransactionEvent()
    data class SetSourceColor(val color: Int) : BaseTransactionEvent()
    data class ToggleExpense(val isExpense: Boolean) : BaseTransactionEvent()
    data class SetHasUnsavedChanges(val hasChanges: Boolean) : BaseTransactionEvent()
    data class SetOriginalData(val data: TransactionData) : BaseTransactionEvent()
    
    // События для управления кошельками
    data class SetTargetWalletId(val walletId: String) : BaseTransactionEvent()
    data class ToggleAddToWallet(val add: Boolean) : BaseTransactionEvent()
    data class ToggleWalletSelection(val walletId: String) : BaseTransactionEvent()
    data class SelectWallet(val walletId: String, val selected: Boolean) : BaseTransactionEvent()
    data class SelectWallets(val walletIds: List<String>) : BaseTransactionEvent()
    
    // События для управления видимостью диалогов
    object ShowDatePicker : BaseTransactionEvent()
    object HideDatePicker : BaseTransactionEvent()
    object ShowCategoryPicker : BaseTransactionEvent()
    object HideCategoryPicker : BaseTransactionEvent()
    object ShowCancelConfirmation : BaseTransactionEvent()
    object HideCancelConfirmation : BaseTransactionEvent()
    object ShowSourcePicker : BaseTransactionEvent()
    object HideSourcePicker : BaseTransactionEvent()
    object ShowCustomCategoryDialog : BaseTransactionEvent()
    object HideCustomCategoryDialog : BaseTransactionEvent()
    object ShowCustomSourceDialog : BaseTransactionEvent()
    object HideCustomSourceDialog : BaseTransactionEvent()
    object ShowColorPicker : BaseTransactionEvent()
    object HideColorPicker : BaseTransactionEvent()
    object ShowWalletSelector : BaseTransactionEvent()
    object HideWalletSelector : BaseTransactionEvent()
    object ShowDeleteCategoryConfirmation : BaseTransactionEvent()
    object HideDeleteCategoryConfirmation : BaseTransactionEvent()
    object ShowDeleteSourceConfirmation : BaseTransactionEvent()
    object HideDeleteSourceConfirmation : BaseTransactionEvent()
    
    // События для редактирования кастомных элементов
    data class SetCustomCategory(val category: String) : BaseTransactionEvent()
    data class SetCustomSource(val source: String) : BaseTransactionEvent()
    data class SetCustomSourceName(val name: String) : BaseTransactionEvent()
    data class SetCategoryToDelete(val category: String) : BaseTransactionEvent()
    data class SetSourceToDelete(val source: String) : BaseTransactionEvent()
    
    // События для добавления новых элементов
    data class AddCustomCategory(val name: String) : BaseTransactionEvent()
    data class AddCustomSource(val name: String, val color: Int) : BaseTransactionEvent()
    
    // События для удаления элементов
    data class DeleteCategory(val name: String) : BaseTransactionEvent()
    data class DeleteSource(val name: String) : BaseTransactionEvent()
    
    // Основные события для транзакций
    object SaveTransaction : BaseTransactionEvent()
    object Reset : BaseTransactionEvent()
    object Cancel : BaseTransactionEvent()
    object Load : BaseTransactionEvent()
    
    // События для управления ошибками
    object ClearError : BaseTransactionEvent()
    
    // События для специфичных действий
    object SubmitAddTransaction : BaseTransactionEvent()
    object SubmitChanges : BaseTransactionEvent()
    object CancelEditing : BaseTransactionEvent()
    object RevertChanges : BaseTransactionEvent()
    object HideSuccessDialog : BaseTransactionEvent()
    
    // События для управления типом транзакции
    object ForceSetIncomeType : BaseTransactionEvent()
    object ForceSetExpenseType : BaseTransactionEvent()
    object ToggleTransactionType : BaseTransactionEvent()
    
    // Toggle опции "Добавить еще"
    object ToggleAddAnotherOption : BaseTransactionEvent()
} 