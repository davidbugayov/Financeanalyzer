package com.davidbugayov.financeanalyzer.feature.transaction.base.model
import androidx.compose.ui.graphics.vector.ImageVector
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

    data object HideDeleteCategoryConfirmDialog : BaseTransactionEvent()

    data object HideDeleteSourceConfirmDialog : BaseTransactionEvent()

    data object ToggleTransactionType : BaseTransactionEvent()

    data object ShowDatePicker : BaseTransactionEvent()

    data object HideDatePicker : BaseTransactionEvent()

    data object ShowCategoryPicker : BaseTransactionEvent()

    data object HideCategoryPicker : BaseTransactionEvent()

    data object ShowCustomCategoryDialog : BaseTransactionEvent()

    data object HideCustomCategoryDialog : BaseTransactionEvent()

    data object ShowCancelConfirmation : BaseTransactionEvent()

    data object HideCancelConfirmation : BaseTransactionEvent()

    data object ShowSourcePicker : BaseTransactionEvent()

    data object HideSourcePicker : BaseTransactionEvent()

    data object ShowCustomSourceDialog : BaseTransactionEvent()

    data object HideCustomSourceDialog : BaseTransactionEvent()

    data object ShowColorPicker : BaseTransactionEvent()

    data object HideColorPicker : BaseTransactionEvent()

    data object ClearError : BaseTransactionEvent()

    data object HideSuccessDialog : BaseTransactionEvent()

    // Для управления кошельками
    data object ToggleAddToWallet : BaseTransactionEvent()

    data object ShowWalletSelector : BaseTransactionEvent()

    data object HideWalletSelector : BaseTransactionEvent()

    data class SelectWallet(val walletId: String, val selected: Boolean) : BaseTransactionEvent()

    data class SelectWallets(val walletIds: List<String>) : BaseTransactionEvent()

    // ...другие общие события, если потребуется
    data object Submit : BaseTransactionEvent()

    data object SubmitEdit : BaseTransactionEvent()

    data object ForceSetIncomeType : BaseTransactionEvent()

    data object ForceSetExpenseType : BaseTransactionEvent()

    data object ResetFieldsForNewTransaction : BaseTransactionEvent()

    data object PreventAutoSubmit : BaseTransactionEvent()

    data class SetExpenseCategory(val category: String) : BaseTransactionEvent()

    data class SetIncomeCategory(val category: String) : BaseTransactionEvent()

    data class SetCustomCategoryIcon(val icon: ImageVector) : BaseTransactionEvent()

    data class SetAmountError(val isError: Boolean) : BaseTransactionEvent()

    // События для работы с сабкатегориями
    data class SetSubcategory(val subcategory: String) : BaseTransactionEvent()

    data class SetCustomSubcategory(val subcategory: String) : BaseTransactionEvent()

    data class AddCustomSubcategory(val subcategory: String) : BaseTransactionEvent()

    data object ShowSubcategoryPicker : BaseTransactionEvent()

    data object HideSubcategoryPicker : BaseTransactionEvent()

    data object ShowCustomSubcategoryDialog : BaseTransactionEvent()

    data object HideCustomSubcategoryDialog : BaseTransactionEvent()
}
