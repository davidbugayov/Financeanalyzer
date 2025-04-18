package com.davidbugayov.financeanalyzer.presentation.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.AddButton
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.AmountField
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.CategorySection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.ColorPickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.CommentField
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.CustomSourceDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.DateField
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.SourcePickerDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.SourceSection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.TransactionHeader
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.WalletSelectionSection
import com.davidbugayov.financeanalyzer.presentation.transaction.base.components.WalletSelectorDialog
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteCategoryConfirmDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteSourceConfirmDialog
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import timber.log.Timber
import org.koin.androidx.compose.koinViewModel

/**
 * Базовый экран для работы с транзакциями
 * Служит основой для AddTransactionScreen и EditTransactionScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTransactionScreen(
    viewModel: AddTransactionViewModel,
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    screenTitle: String = "Добавить транзакцию",
    buttonText: String = "Добавить",
    isEditMode: Boolean = false
) {
    val state by viewModel.state.collectAsState()

    // Логируем режим экрана
    LaunchedEffect(isEditMode) {
        Timber.d("Экран инициализирован в режиме ${if (isEditMode) "редактирования" else "добавления"} транзакции. editMode=${state.editMode}, transactionToEdit=${state.transactionToEdit?.id}")
    }

    // В режиме редактирования устанавливаем заголовок и текст кнопки
    val actualScreenTitle = if (isEditMode) "Редактирование транзакции" else screenTitle
    val actualButtonText = if (isEditMode) "Сохранить" else buttonText

    var showCancelConfirmation by remember { mutableStateOf(false) }

    // Функция для обработки выхода с экрана
    fun handleExit() {
        // Обновляем позиции категорий перед выходом
        viewModel.updateCategoryPositions()
        // Сбрасываем поля
        viewModel.resetFields()
        // Возвращаемся назад
        onNavigateBack()
    }

    // Цвета для типов транзакций
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val currentColor = if (state.isExpense) expenseColor else incomeColor
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = actualScreenTitle,
                showBackButton = true,
                onBackClick = {
                    if (state.title.isNotBlank() || state.amount.isNotBlank() || state.category.isNotBlank() || state.note.isNotBlank()) {
                        showCancelConfirmation = true
                    } else {
                        handleExit()
                    }
                },
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Заголовок с датой и типом транзакции
                TransactionHeader(
                    date = state.selectedDate,
                    isExpense = state.isExpense,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    onDateClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                    },
                    onToggleTransactionType = {
                        viewModel.onEvent(AddTransactionEvent.ToggleTransactionType)
                    },
                    forceExpense = state.forceExpense
                )

                // Секция "Откуда"
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (state.isExpense) stringResource(R.string.source) else "Куда",
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.spacing_normal),
                            vertical = dimensionResource(R.dimen.spacing_medium)
                        )
                    )

                    SourceSection(
                        sources = state.sources,
                        selectedSource = state.source,
                        onSourceSelected = { source ->
                            viewModel.onEvent(AddTransactionEvent.SetSource(source.name))
                            viewModel.onEvent(AddTransactionEvent.SetSourceColor(source.color))
                        },
                        onAddSourceClick = {
                            viewModel.onEvent(AddTransactionEvent.ShowCustomSourceDialog)
                        },
                        onSourceLongClick = { source ->
                            viewModel.onEvent(
                                AddTransactionEvent.ShowDeleteSourceConfirmDialog(
                                    source.name
                                )
                            )
                        }
                    )
                }

                // Секция категорий с передачей состояния ошибки
                CategorySection(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    selectedCategory = state.category,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category.name))
                    },
                    onAddCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                    },
                    onCategoryLongClick = { category ->
                        viewModel.onEvent(
                            AddTransactionEvent.ShowDeleteCategoryConfirmDialog(
                                category.name
                            )
                        )
                    },
                    isError = state.categoryError
                )

                // Поле ввода суммы
                AmountField(
                    amount = state.amount,
                    onAmountChange = { viewModel.onEvent(AddTransactionEvent.SetAmount(it)) },
                    isError = state.amountError,
                    accentColor = currentColor
                )

                // Поле выбора даты
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowDatePicker)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.spacing_normal))
                )

                // Поле для комментария без иконки прикрепления
                CommentField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(AddTransactionEvent.SetNote(note))
                    },
                    onAttachClick = { /* действие удалено */ }
                )
                
                // Секция выбора кошельков (показывается только для доходов)
                Timber.d("BaseTransactionScreen: isExpense=${state.isExpense}, addToWallet=${state.addToWallet}, selectedWallets=${state.selectedWallets}, targetWalletId=${state.targetWalletId}")
                
                WalletSelectionSection(
                    addToWallet = state.addToWallet,
                    selectedWallets = state.selectedWallets,
                    onToggleAddToWallet = {
                        viewModel.onEvent(AddTransactionEvent.ToggleAddToWallet)
                    },
                    onSelectWalletsClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowWalletSelector)
                    },
                    isVisible = !state.isExpense // Показываем только для доходов
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))

                // Кнопка добавления/сохранения
                AddButton(
                    onClick = { 
                        viewModel.submitTransaction()
                    },
                    text = actualButtonText,
                    color = currentColor,
                    isLoading = state.isLoading
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
            }

            // Диалоги
            if (showCancelConfirmation) {
                CancelConfirmationDialog(
                    onConfirm = {
                        showCancelConfirmation = false
                        handleExit()
                    },
                    onDismiss = {
                        showCancelConfirmation = false
                    }
                )
            }

            if (state.error != null) {
                ErrorDialog(
                    message = state.error!!,
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ClearError)
                    }
                )
            }

            if (state.isSuccess) {
                SuccessDialog(
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                        handleExit()
                    },
                    onAddAnother = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                    },
                    isEditMode = isEditMode
                )
            }

            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    onDateSelected = { date ->
                        viewModel.onEvent(AddTransactionEvent.SetDate(date))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDatePicker)
                    }
                )
            }

            if (state.showCategoryPicker) {
                CategoryPickerDialog(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    onCategorySelected = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCategory(category))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { name ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomCategory(name))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.AddCustomCategory(state.customCategory))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCustomCategoryDialog)
                    }
                )
            }

            if (state.showDeleteCategoryConfirmDialog) {
                DeleteCategoryConfirmDialog(
                    category = state.categoryToDelete ?: "",
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.DeleteCategory(state.categoryToDelete ?: ""))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDeleteCategoryConfirmDialog)
                    }
                )
            }

            if (state.showDeleteSourceConfirmDialog) {
                DeleteSourceConfirmDialog(
                    source = state.sourceToDelete ?: "",
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.DeleteSource(state.sourceToDelete ?: ""))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDeleteSourceConfirmDialog)
                    }
                )
            }

            if (state.showSourcePicker) {
                SourcePickerDialog(
                    sources = state.sources,
                    onSourceSelected = { source ->
                        viewModel.onEvent(AddTransactionEvent.SetSource(source.name))
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(source.color))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideSourcePicker)
                    },
                    onAddCustomSource = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomSourceDialog)
                    }
                )
            }

            if (state.showCustomSourceDialog) {
                CustomSourceDialog(
                    sourceName = state.customSource,
                    color = state.sourceColor,
                    onSourceNameChange = { name ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomSource(name))
                    },
                    onColorClick = { selectedColor ->
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(selectedColor))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.AddCustomSource(state.customSource, state.sourceColor))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCustomSourceDialog)
                    }
                )
            }

            if (state.showColorPicker) {
                ColorPickerDialog(
                    initialColor = state.sourceColor,
                    onColorSelected = { color ->
                        viewModel.onEvent(AddTransactionEvent.SetSourceColor(color))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    }
                )
            }
            
            if (state.showWalletSelector) {
                WalletSelectorDialog(
                    wallets = viewModel.wallets,
                    selectedWalletIds = state.selectedWallets,
                    onWalletSelected = { walletId, selected ->
                        viewModel.onEvent(AddTransactionEvent.SelectWallet(walletId, selected))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.HideWalletSelector)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideWalletSelector)
                    }
                )
            }
        }
    }
} 