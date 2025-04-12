package com.davidbugayov.financeanalyzer.presentation.add

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
import androidx.compose.runtime.DisposableEffect
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
import com.davidbugayov.financeanalyzer.presentation.add.components.AddButton
import com.davidbugayov.financeanalyzer.presentation.add.components.AmountField
import com.davidbugayov.financeanalyzer.presentation.add.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CategorySection
import com.davidbugayov.financeanalyzer.presentation.add.components.ColorPickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CommentField
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.CustomSourceDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.DateField
import com.davidbugayov.financeanalyzer.presentation.add.components.NoteField
import com.davidbugayov.financeanalyzer.presentation.add.components.SourcePickerDialog
import com.davidbugayov.financeanalyzer.presentation.add.components.SourceSection
import com.davidbugayov.financeanalyzer.presentation.add.components.TransactionHeader
import com.davidbugayov.financeanalyzer.presentation.add.components.WalletSelectionSection
import com.davidbugayov.financeanalyzer.presentation.add.components.WalletSelectorDialog
import com.davidbugayov.financeanalyzer.presentation.add.model.AddTransactionEvent
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
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Экран добавления новой транзакции в стиле CoinKeeper
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showCancelConfirmation by remember { mutableStateOf(false) }

    // Логируем открытие экрана добавления транзакции
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "add_transaction",
            screenClass = "AddTransactionScreen"
        )

        // Устанавливаем callback для навигации назад
        viewModel.navigateBackCallback = onNavigateBack
    }
    
    // Гарантируем, что режим "Доход" будет активен при lockExpenseSelection
    LaunchedEffect(viewModel.lockExpenseSelection) {
        if (viewModel.lockExpenseSelection && state.isExpense) {
            // Если установлена блокировка выбора расхода, но состояние - расход,
            // принудительно переключаем на доход
            viewModel.onEvent(AddTransactionEvent.ForceSetIncomeType)
        }
    }

    // Очищаем callback при выходе из композиции
    DisposableEffect(Unit) {
        onDispose {
            viewModel.navigateBackCallback = null
        }
    }

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
                title = if (state.editMode) 
                    "Редактирование транзакции"
                else 
                    stringResource(R.string.add_transaction),
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
                    lockExpenseSelection = viewModel.lockExpenseSelection
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

                // Поле для комментария с иконкой прикрепления
                CommentField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(AddTransactionEvent.SetNote(note))
                    },
                    onAttachClick = {
                        viewModel.onEvent(AddTransactionEvent.AttachReceipt)
                    }
                )
                
                // Секция выбора кошельков (показывается только для доходов)
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

                // Кнопка добавления
                AddButton(
                    onClick = { 
                        viewModel.submitTransaction()
                    },
                    text = if (state.editMode) 
                        "Сохранить"
                    else 
                        "Добавить",
                    color = currentColor,
                    isLoading = state.isLoading
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
            }

            // Диалоги
            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    onDateSelected = { date ->
                        viewModel.onEvent(AddTransactionEvent.SetDate(date))
                        viewModel.onEvent(AddTransactionEvent.HideDatePicker)
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
                    onCustomCategoryClick = {
                        viewModel.onEvent(AddTransactionEvent.ShowCustomCategoryDialog)
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCategoryPicker)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { category ->
                        viewModel.onEvent(AddTransactionEvent.SetCustomCategory(category))
                    },
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.AddCustomCategory(state.customCategory))
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideCustomCategoryDialog)
                    }
                )
            }

            // Диалоги для отображения результатов операций
            if (state.error != null) {
                ErrorDialog(
                    message = state.error ?: "",
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ClearError)
                    }
                )
            } else if (state.isSuccess) {
                SuccessDialog(
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                        // Возвращаемся на предыдущий экран при нажатии "Готово"
                        handleExit()
                    },
                    onAddAnother = {
                        viewModel.onEvent(AddTransactionEvent.HideSuccessDialog)
                    },
                    isEditMode = state.editMode
                )
            }

            state.error?.let { error ->
                ErrorDialog(
                    message = error,
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.ClearError)
                    }
                )
            }

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
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideColorPicker)
                    }
                )
            }

            // Диалог подтверждения удаления категории
            if (state.showDeleteCategoryConfirmDialog && state.categoryToDelete != null) {
                val categoryToDelete = state.categoryToDelete ?: ""
                DeleteCategoryConfirmDialog(
                    category = categoryToDelete,
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.DeleteCategory(categoryToDelete))
                        viewModel.onEvent(AddTransactionEvent.HideDeleteCategoryConfirmDialog)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDeleteCategoryConfirmDialog)
                    },
                    isDefaultCategory = if (state.isExpense) {
                        categoriesViewModel.isDefaultExpenseCategory(categoryToDelete)
                    } else {
                        categoriesViewModel.isDefaultIncomeCategory(categoryToDelete)
                    }
                )
            }

            // Диалог подтверждения удаления источника
            if (state.showDeleteSourceConfirmDialog && state.sourceToDelete != null) {
                val sourceToDelete = state.sourceToDelete ?: ""
                DeleteSourceConfirmDialog(
                    source = sourceToDelete,
                    onConfirm = {
                        viewModel.onEvent(AddTransactionEvent.DeleteSource(sourceToDelete))
                        viewModel.onEvent(AddTransactionEvent.HideDeleteSourceConfirmDialog)
                    },
                    onDismiss = {
                        viewModel.onEvent(AddTransactionEvent.HideDeleteSourceConfirmDialog)
                    }
                )
            }
            
            // Диалог выбора кошельков для добавления дохода
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