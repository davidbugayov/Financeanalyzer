package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.ConfirmCancelDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.CustomSourceDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.DeleteCategoryConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.DeleteSourceConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.SourceColorPickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.SourcePickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.SourceSection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.WalletSelectorDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs.WalletSelectionSection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.SourceItem as ModelSourceItem
import org.koin.androidx.compose.koinViewModel

/**
 * Экран добавления транзакции
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<AddTransactionViewModel>()
    val state by viewModel.state.collectAsState()
    
    // Модальные диалоги
    /*
    // Диалог успешного добавления
    if (state.showSuccessDialog) {
        SuccessDialog(
            message = stringResource(R.string.transaction_added_success),
            onDismiss = { onEvent(AddTransactionEvent.HideSuccessDialog) }
        )
    }

    // Диалог выбора категории
    if (state.dialogState.showCategoryPicker) {
        CategoryPickerDialog(
            categories = state.categories,
            onCategorySelected = { category ->
                onEvent(AddTransactionEvent.SetCategory(category.name))
                onEvent(AddTransactionEvent.HideCategoryPicker)
            },
            onDismiss = { onEvent(AddTransactionEvent.HideCategoryPicker) },
            onCreateCustom = { onEvent(AddTransactionEvent.ShowCustomCategoryDialog) }
        )
    }

    // Диалог создания кастомной категории
    if (state.dialogState.showCustomCategoryDialog) {
        CustomCategoryDialog(
            onConfirm = { categoryName ->
                onEvent(AddTransactionEvent.AddCustomCategory(categoryName))
                onEvent(AddTransactionEvent.HideCustomCategoryDialog)
            },
            onDismiss = { onEvent(AddTransactionEvent.HideCustomCategoryDialog) }
        )
    }

    // Диалог выбора источника
    if (state.dialogState.showSourcePicker) {
        SourcePickerDialog(
            sources = state.sources,
            onSourceSelected = { source ->
                onEvent(AddTransactionEvent.SetSource(source.name))
                onEvent(AddTransactionEvent.SetSourceColor(source.color))
                onEvent(AddTransactionEvent.HideSourcePicker)
            },
            onDismiss = { onEvent(AddTransactionEvent.HideSourcePicker) },
            onCreateCustom = { onEvent(AddTransactionEvent.ShowCustomSourceDialog) }
        )
    }

    // Диалог создания кастомного источника
    if (state.dialogState.showCustomSourceDialog) {
        CustomSourceDialog(
            onConfirm = { sourceName, color ->
                onEvent(AddTransactionEvent.AddCustomSource(sourceName, color))
                onEvent(AddTransactionEvent.HideCustomSourceDialog)
            },
            onDismiss = { onEvent(AddTransactionEvent.HideCustomSourceDialog) },
            onColorClick = { onEvent(AddTransactionEvent.ShowSourceColorPicker) },
            selectedColor = state.selectedSourceColor
        )
    }

    // Диалог выбора цвета для источника
    if (state.dialogState.showSourceColorPicker) {
        SourceColorPickerDialog(
            onColorSelected = { color ->
                onEvent(AddTransactionEvent.SetSourceColor(color))
                onEvent(AddTransactionEvent.HideSourceColorPicker)
            },
            onDismiss = { onEvent(AddTransactionEvent.HideSourceColorPicker) }
        )
    }

    // Диалог подтверждения отмены транзакции
    if (state.dialogState.showConfirmCancelDialog) {
        ConfirmCancelDialog(
            onDismiss = { onEvent(AddTransactionEvent.HideConfirmCancelDialog) },
            onConfirm = {
                onEvent(AddTransactionEvent.HideConfirmCancelDialog)
                navController.popBackStack()
            }
        )
    }

    // Диалог подтверждения удаления категории
    if (state.dialogState.showDeleteCategoryConfirmationDialog) {
        val categoryToDelete = state.categoryToDelete
        if (categoryToDelete != null) {
            DeleteCategoryConfirmationDialog(
                categoryName = categoryToDelete.name,
                onDismiss = { onEvent(AddTransactionEvent.HideDeleteCategoryConfirmationDialog) },
                onConfirm = {
                    onEvent(AddTransactionEvent.DeleteCategory(categoryToDelete))
                    onEvent(AddTransactionEvent.HideDeleteCategoryConfirmationDialog)
                }
            )
        }
    }

    // Диалог выбора кошельков для добавления дохода
    if (state.dialogState.showWalletSelector) {
        WalletSelectorDialog(
            wallets = state.wallets,
            selectedWallets = state.selectedWallets,
            onWalletSelected = { wallet, isSelected ->
                onEvent(AddTransactionEvent.ToggleWalletSelection(wallet, isSelected))
            },
            onDismiss = { onEvent(AddTransactionEvent.HideWalletSelector) },
            onConfirm = { onEvent(AddTransactionEvent.HideWalletSelector) }
        )
    }

    // Диалог подтверждения удаления источника
    if (state.dialogState.showDeleteSourceConfirmationDialog) {
        val sourceToDelete = state.sourceToDelete
        if (sourceToDelete != null) {
            DeleteSourceConfirmationDialog(
                sourceName = sourceToDelete.name,
                onDismiss = { onEvent(AddTransactionEvent.HideDeleteSourceConfirmationDialog) },
                onConfirm = {
                    onEvent(AddTransactionEvent.DeleteSource(sourceToDelete))
                    onEvent(AddTransactionEvent.HideDeleteSourceConfirmationDialog)
                }
            )
        }
    }
    */
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (state.transactionData.amount.isNotEmpty() || state.transactionData.category.isNotEmpty() || state.transactionData.note.isNotEmpty()) {
                            viewModel.onEvent(BaseTransactionEvent.ShowCancelConfirmation)
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Добавляем кнопку сброса полей
                    IconButton(onClick = { viewModel.resetFields() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Основное содержимое экрана транзакции
        BaseTransactionScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onSubmit = { viewModel.onEvent(BaseTransactionEvent.SubmitAddTransaction) },
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            submitButtonText = stringResource(R.string.confirm)
        )

        // Добавляем секцию кошельков для доходных операций
        if (!state.transactionData.isExpense) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 600.dp))
                WalletSelectionSection(
                    walletsList = state.walletState.wallets,
                    addToWallet = state.walletState.addToWallet,
                    selectedWallets = state.walletState.selectedWallets,
                    onToggleAddToWallet = { 
                        viewModel.onEvent(BaseTransactionEvent.ToggleAddToWallet(!state.walletState.addToWallet))
                    },
                    onSelectWalletsClick = { 
                        viewModel.onEvent(BaseTransactionEvent.ShowWalletSelector) 
                    },
                    isVisible = true,
                    targetWalletName = state.walletState.wallets.firstOrNull()?.name
                )
            }
        }

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
        
        if (state.dialogStateTransaction.showWalletSelector) {
            WalletSelectorDialog(
                wallets = state.walletState.wallets.map { it.name },
                selectedWallets = state.walletState.selectedWallets,
                onWalletToggle = { wallet ->
                    viewModel.onEvent(BaseTransactionEvent.ToggleWalletSelection(wallet))
                },
                onConfirm = {
                    viewModel.onEvent(BaseTransactionEvent.HideWalletSelector)
                },
                onDismiss = {
                    viewModel.onEvent(BaseTransactionEvent.HideWalletSelector)
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

private fun ModelSourceItem.toSourceModel(): Source {
    return Source(
        id = 0L, // Assuming new sources have default ID
        name = this.name,
        color = this.color,
        isCustom = this.isCustom
    )
} 