package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.EditTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import java.util.Date
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Экран редактирования существующей транзакции
 */
@Composable
fun EditTransactionScreen(
    viewModel: EditTransactionViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    transactionId: String? = null
) {
    // Get context at the Composable level
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "edit_transaction",
            screenClass = "EditTransactionScreen"
        )
        transactionId?.let {
            if (it.isNotEmpty()) {
                viewModel.loadTransactionForEdit(it)
            }
        }
    }

    BaseTransactionScreen(
        viewModel = viewModel,
        categoriesViewModel = categoriesViewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = "Редактирование транзакции",
        buttonText = "Сохранить",
        isEditMode = true,
        eventFactory = { eventData ->
            when (eventData) {
                is Source -> EditTransactionEvent.SetSource(eventData.name)
                is CategoryItem -> EditTransactionEvent.SetCategory(eventData.name)
                is Date -> EditTransactionEvent.SetDate(eventData)
                is String -> when (eventData) {
                    "SubmitEdit" -> EditTransactionEvent.SubmitEdit
                    "ShowDatePicker" -> EditTransactionEvent.ShowDatePicker
                    "HideDatePicker" -> EditTransactionEvent.HideDatePicker
                    "ShowSourcePicker" -> EditTransactionEvent.ShowSourcePicker
                    "HideSourcePicker" -> EditTransactionEvent.HideSourcePicker
                    "ShowCustomSourceDialog" -> EditTransactionEvent.ShowCustomSourceDialog
                    "HideCustomSourceDialog" -> EditTransactionEvent.HideCustomSourceDialog
                    "ShowCustomCategoryDialog" -> EditTransactionEvent.ShowCustomCategoryDialog
                    "HideCustomCategoryDialog" -> EditTransactionEvent.HideCustomCategoryDialog
                    "ToggleTransactionType" -> EditTransactionEvent.ToggleTransactionType
                    "HideDeleteCategoryConfirmDialog" -> EditTransactionEvent.HideDeleteCategoryConfirmDialog
                    "HideDeleteSourceConfirmDialog" -> EditTransactionEvent.HideDeleteSourceConfirmDialog
                    "ClearError" -> EditTransactionEvent.ClearError
                    "HideSuccessDialog" -> EditTransactionEvent.HideSuccessDialog
                    "HideColorPicker" -> EditTransactionEvent.HideColorPicker
                    "HideWalletSelector" -> EditTransactionEvent.HideWalletSelector
                    "ToggleAddToWallet" -> EditTransactionEvent.ToggleAddToWallet
                    "ShowWalletSelector" -> EditTransactionEvent.ShowWalletSelector
                    else -> EditTransactionEvent.SubmitEdit
                }
                is Pair<*, *> -> when (eventData.first as? String) {
                    "DeleteSourceConfirm" -> {
                        val source = eventData.second as? Source
                        EditTransactionEvent.ShowDeleteSourceConfirmDialog(source?.name ?: "")
                    }
                    "DeleteCategoryConfirm" -> {
                        val category = eventData.second as? CategoryItem
                        EditTransactionEvent.ShowDeleteCategoryConfirmDialog(category?.name ?: "")
                    }
                    "SetAmount" -> EditTransactionEvent.SetAmount(eventData.second as String)
                    "SetNote" -> EditTransactionEvent.SetNote(eventData.second as String)
                    "SetCustomCategoryText" -> EditTransactionEvent.SetCustomCategory(eventData.second as String)
                    "AddCustomCategoryConfirm" -> EditTransactionEvent.AddCustomCategory(eventData.second as String)
                    "DeleteCategoryConfirmActual" -> EditTransactionEvent.DeleteCategory(eventData.second as String)
                    "DeleteSourceConfirmActual" -> EditTransactionEvent.DeleteSource(eventData.second as String)
                    "SetCustomSourceName" -> EditTransactionEvent.SetCustomSource(eventData.second as String)
                    "SetCustomSourceColor" -> EditTransactionEvent.SetSourceColor(eventData.second as Int)
                    "SetSourceColor" -> EditTransactionEvent.SetSourceColor(eventData.second as Int)
                    else -> EditTransactionEvent.SubmitEdit
                }
                is Triple<*, *, *> -> when (eventData.first as? String) {
                    "AddCustomSourceConfirm" -> {
                        val name = eventData.second as String
                        val color = eventData.third as Int
                        EditTransactionEvent.AddCustomSource(name, color)
                    }
                    "SelectWallet" -> {
                        val walletId = eventData.second as String
                        val selected = eventData.third as Boolean
                        EditTransactionEvent.SelectWallet(walletId, selected)
                    }
                    else -> EditTransactionEvent.SubmitEdit
                }
                else -> EditTransactionEvent.SubmitEdit
            }
        },
        submitEvent = EditTransactionEvent.SubmitEdit
    )
} 