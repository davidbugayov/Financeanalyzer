package com.davidbugayov.financeanalyzer.presentation.transaction.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem
import org.koin.androidx.compose.koinViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.ConfirmCancelDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CustomSourceDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.DeleteCategoryConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.DeleteSourceConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.SourceColorPickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.SourcePickerDialog
import com.davidbugayov.financeanalyzer.domain.model.Source

/**
 * Экран редактирования транзакции
 */
@Composable
fun EditTransactionScreen(
    transactionId: String,
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<EditTransactionViewModel>()
    val state by viewModel.state.collectAsState()
    
    // Загружаем транзакцию при первом запуске
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }
    
    // Обработка успешного редактирования
    if (state.isSuccess) {
        SuccessDialog(
            onDismiss = {
                viewModel.onEvent(BaseTransactionEvent.HideSuccessDialog)
                onBackClick()
            },
            onAddAnother = {
                viewModel.onEvent(BaseTransactionEvent.HideSuccessDialog)
                onBackClick()
            },
            message = stringResource(R.string.transaction_updated_success)
        )
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.edit_transaction),
                showBackButton = true,
                onBackClick = {
                    if (state.hasUnsavedChanges) {
                        viewModel.onEvent(BaseTransactionEvent.ShowCancelConfirmation)
                    } else {
                        onBackClick()
                    }
                }
            )
        }
    ) { paddingValues ->
        // Основное содержимое экрана транзакции
        BaseTransactionScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onSubmit = { viewModel.onEvent(BaseTransactionEvent.SubmitChanges) },
            submitButtonText = stringResource(R.string.save),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
        
        // Диалоги
        if (state.dialogStateTransaction.showCategoryPicker) {
            CategoryPickerDialog(
                categories = (if (state.transactionData.isExpense) 
                    state.expenseCategories 
                else 
                    state.incomeCategories).map { categoryItem ->
                        CategoryItem(
                            name = categoryItem.name,
                            count = categoryItem.count,
                            image = categoryItem.image,
                            isCustom = categoryItem.isCustom
                        )
                    },
                onCategorySelected = { category ->
                    viewModel.onEvent(BaseTransactionEvent.SetCategory(category.name))
                    viewModel.onEvent(BaseTransactionEvent.HideCategoryPicker)
                },
                onCategoryLongClick = { category ->
                    viewModel.onEvent(BaseTransactionEvent.SetCategoryToDelete(category))
                    viewModel.onEvent(BaseTransactionEvent.ShowDeleteCategoryConfirmation)
                },
                onCustomCategoryClick = { 
                    viewModel.onEvent(BaseTransactionEvent.HideCategoryPicker)
                    viewModel.onEvent(BaseTransactionEvent.ShowCustomCategoryDialog) 
                },
                onDismiss = { 
                    viewModel.onEvent(BaseTransactionEvent.HideCategoryPicker) 
                }
            )
        }
        
        if (state.dialogStateTransaction.showCustomCategoryDialog) {
            CustomCategoryDialog(
                isVisible = state.dialogStateTransaction.showCustomCategoryDialog,
                onDismiss = { viewModel.onEvent(BaseTransactionEvent.HideCustomCategoryDialog) },
                onCustomCategoryConfirm = { categoryName ->
                    viewModel.onEvent(BaseTransactionEvent.AddCustomCategory(categoryName))
                    viewModel.onEvent(BaseTransactionEvent.HideCustomCategoryDialog)
                }
            )
        }
        
        if (state.dialogStateTransaction.showSourcePicker) {
            SourcePickerDialog(
                sources = state.sources.map { it.toSourceModel() },
                onSourceSelected = { source ->
                    viewModel.onEvent(BaseTransactionEvent.SetSource(source.name))
                    viewModel.onEvent(BaseTransactionEvent.SetSourceColor(source.color))
                    viewModel.onEvent(BaseTransactionEvent.HideSourcePicker)
                },
                onDismiss = { 
                    viewModel.onEvent(BaseTransactionEvent.HideSourcePicker) 
                },
                onAddCustomSource = {
                    viewModel.onEvent(BaseTransactionEvent.HideSourcePicker)
                    viewModel.onEvent(BaseTransactionEvent.ShowCustomSourceDialog)
                }
            )
        }
        
        if (state.dialogStateTransaction.showCustomSourceDialog) {
            CustomSourceDialog(
                sourceName = state.editingState.sourceName,
                color = state.editingState.sourceColor,
                onSourceNameChange = { viewModel.onEvent(BaseTransactionEvent.SetCustomSourceName(it)) },
                onColorClick = { color -> viewModel.onEvent(BaseTransactionEvent.SetSourceColor(color)) },
                onConfirm = {
                    viewModel.onEvent(BaseTransactionEvent.AddCustomSource(
                        name = state.editingState.sourceName,
                        color = state.editingState.sourceColor
                    ))
                    viewModel.onEvent(BaseTransactionEvent.HideCustomSourceDialog)
                },
                onDismiss = { viewModel.onEvent(BaseTransactionEvent.HideCustomSourceDialog) }
            )
        }
        
        if (state.dialogStateTransaction.showColorPicker) {
            SourceColorPickerDialog(
                initialColor = state.editingState.sourceColor,
                onColorSelected = { color ->
                    viewModel.onEvent(BaseTransactionEvent.SetSourceColor(color))
                    viewModel.onEvent(BaseTransactionEvent.HideColorPicker)
                },
                onDismiss = { 
                    viewModel.onEvent(BaseTransactionEvent.HideColorPicker) 
                }
            )
        }
        
        if (state.dialogStateTransaction.showCancelConfirmation) {
            ConfirmCancelDialog(
                onConfirm = {
                    viewModel.onEvent(BaseTransactionEvent.HideCancelConfirmation)
                    onBackClick()
                },
                onDismiss = { 
                    viewModel.onEvent(BaseTransactionEvent.HideCancelConfirmation) 
                }
            )
        }
        
        if (state.dialogStateTransaction.showDeleteCategoryConfirmation && state.editingState.categoryToDelete != null) {
            DeleteCategoryConfirmationDialog(
                categoryName = state.editingState.categoryToDelete!!,
                onConfirm = {
                    viewModel.onEvent(BaseTransactionEvent.DeleteCategory(state.editingState.categoryToDelete!!))
                    viewModel.onEvent(BaseTransactionEvent.HideDeleteCategoryConfirmation)
                },
                onDismiss = { 
                    viewModel.onEvent(BaseTransactionEvent.HideDeleteCategoryConfirmation) 
                }
            )
        }
        
        if (state.dialogStateTransaction.showDeleteSourceConfirmation && state.editingState.sourceToDelete != null) {
            DeleteSourceConfirmationDialog(
                sourceName = state.editingState.sourceToDelete!!,
                onConfirm = {
                    viewModel.onEvent(BaseTransactionEvent.DeleteSource(state.editingState.sourceToDelete!!))
                    viewModel.onEvent(BaseTransactionEvent.HideDeleteSourceConfirmation)
                },
                onDismiss = { 
                    viewModel.onEvent(BaseTransactionEvent.HideDeleteSourceConfirmation) 
                }
            )
        }
    }
}

private fun com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem.toSourceModel(): Source {
    return Source(
        id = 0L,
        name = this.name,
        color = this.color,
        isCustom = this.isCustom
    )
} 