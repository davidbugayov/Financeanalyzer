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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteCategoryConfirmDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteSourceConfirmDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
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
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionState
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.TransactionScreenViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.util.Date
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Базовый экран для работы с транзакциями
 * Служит основой для AddTransactionScreen и EditTransactionScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <E> BaseTransactionScreen(
    viewModel: TransactionScreenViewModel<out BaseTransactionState, E>,
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    screenTitle: String = "Добавить транзакцию",
    buttonText: String = "Добавить",
    isEditMode: Boolean = false,
    eventFactory: (Any) -> E,
    submitEvent: E
) {
    val context = LocalContext.current
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
                        viewModel.onEvent(eventFactory("ShowDatePicker"), context)
                    },
                    onToggleTransactionType = {
                        viewModel.onEvent(eventFactory("ToggleTransactionType"), context)
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

                    Timber.d("Rendering SourceSection with isExpense=" + state.isExpense + ", selectedSource=" + state.source + ", sources count=" + state.sources.size)
                    
                    SourceSection(
                        sources = state.sources,
                        selectedSource = state.source,
                        onSourceSelected = { selectedSource -> 
                            Timber.d("Source selected directly: " + selectedSource.name + " with color " + selectedSource.color)
                            viewModel.onEvent(eventFactory(selectedSource), context)
                        },
                        onAddSourceClick = {
                            Timber.d("Add source button clicked")
                            viewModel.onEvent(eventFactory("ShowCustomSourceDialog"), context)
                        },
                        onSourceLongClick = { selectedSource -> 
                            Timber.d("Source long clicked: " + selectedSource.name)
                            viewModel.onEvent(eventFactory(Pair("DeleteSourceConfirm", selectedSource)), context)
                        }
                    )
                }

                // Секция категорий с передачей состояния ошибки
                CategorySection(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    selectedCategory = state.category,
                    onCategorySelected = { selectedCategory -> 
                        Timber.d("Category selected directly: " + selectedCategory.name)
                        viewModel.onEvent(eventFactory(selectedCategory), context)
                    },
                    onAddCategoryClick = {
                        viewModel.onEvent(eventFactory("ShowCustomCategoryDialog"), context)
                    },
                    onCategoryLongClick = { selectedCategory -> 
                        Timber.d("Category long clicked: " + selectedCategory.name)
                        viewModel.onEvent(eventFactory(Pair("DeleteCategoryConfirm", selectedCategory)), context)
                    },
                    isError = state.categoryError
                )

                // Поле ввода суммы
                AmountField(
                    amount = state.amount,
                    onAmountChange = { amount -> 
                        viewModel.onEvent(eventFactory(Pair("SetAmount", amount)), context) },
                    isError = state.amountError,
                    accentColor = currentColor
                )

                // Поле выбора даты
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(eventFactory("ShowDatePicker"), context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.spacing_normal))
                )

                // Поле для комментария без иконки прикрепления
                CommentField(
                    note = state.note,
                    onNoteChange = { note -> 
                        viewModel.onEvent(eventFactory(Pair("SetNote", note)), context)
                    },
                    onAttachClick = { /* действие удалено */ }
                )
                
                // Секция выбора кошельков (показывается только для доходов)
                Timber.d("BaseTransactionScreen: isExpense=${state.isExpense}, addToWallet=${state.addToWallet}, selectedWallets=${state.selectedWallets}, targetWalletId=${state.targetWalletId}")
                
                WalletSelectionSection(
                    addToWallet = state.addToWallet,
                    selectedWallets = state.selectedWallets,
                    onToggleAddToWallet = {
                        viewModel.onEvent(eventFactory("ToggleAddToWallet"), context)
                    },
                    onSelectWalletsClick = {
                        viewModel.onEvent(eventFactory("ShowWalletSelector"), context)
                    },
                    isVisible = !state.isExpense // Показываем только для доходов
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))

                // Кнопка добавления/сохранения
                AddButton(
                    onClick = { 
                        viewModel.onEvent(submitEvent, context)
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
                        viewModel.onEvent(eventFactory("ClearError"), context)
                    }
                )
            }

            if (state.isSuccess) {
                SuccessDialog(
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideSuccessDialog"), context)
                        handleExit()
                    },
                    onAddAnother = {
                        viewModel.onEvent(eventFactory("HideSuccessDialog"), context)
                    },
                    isEditMode = isEditMode
                )
            }

            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    onDateSelected = { date -> 
                        viewModel.onEvent(eventFactory(date), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideDatePicker"), context)
                    }
                )
            }

            if (state.showCategoryPicker) {
                CategoryPickerDialog(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    onCategorySelected = { categoryName ->
                        Timber.d("Category selected from dialog: $categoryName")
                        viewModel.onEvent(eventFactory(categoryName), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideCategoryPicker"), context)
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(eventFactory("ShowCustomCategoryDialog"), context)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { name -> 
                        viewModel.onEvent(eventFactory(Pair("SetCustomCategoryText", name)), context)
                    },
                    onConfirm = {
                        viewModel.onEvent(eventFactory(Pair("AddCustomCategoryConfirm", state.customCategory)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideCustomCategoryDialog"), context)
                    }
                )
            }

            if (state.showDeleteCategoryConfirmDialog) {
                DeleteCategoryConfirmDialog(
                    category = state.categoryToDelete ?: "",
                    onConfirm = {
                        viewModel.onEvent(eventFactory(Pair("DeleteCategoryConfirmActual", state.categoryToDelete ?: "")), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideDeleteCategoryConfirmDialog"), context)
                    }
                )
            }

            if (state.showDeleteSourceConfirmDialog) {
                DeleteSourceConfirmDialog(
                    source = state.sourceToDelete ?: "",
                    onConfirm = {
                        viewModel.onEvent(eventFactory(Pair("DeleteSourceConfirmActual", state.sourceToDelete ?: "")), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideDeleteSourceConfirmDialog"), context)
                    }
                )
            }

            if (state.showSourcePicker) {
                SourcePickerDialog(
                    sources = state.sources,
                    onSourceSelected = { source ->
                        viewModel.onEvent(eventFactory(source), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideSourcePicker"), context)
                    },
                    onAddCustomSource = {
                        viewModel.onEvent(eventFactory("ShowCustomSourceDialog"), context)
                    }
                )
            }

            if (state.showCustomSourceDialog) {
                CustomSourceDialog(
                    sourceName = state.customSource,
                    color = state.sourceColor,
                    onSourceNameChange = { name ->
                        viewModel.onEvent(eventFactory(Pair("SetCustomSourceName", name)), context)
                    },
                    onColorClick = { selectedColor ->
                        viewModel.onEvent(eventFactory(Pair("SetCustomSourceColor", selectedColor)), context)
                    },
                    onConfirm = {
                        viewModel.onEvent(eventFactory(Triple("AddCustomSourceConfirm", state.customSource, state.sourceColor)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideCustomSourceDialog"), context)
                    }
                )
            }

            if (state.showColorPicker) {
                ColorPickerDialog(
                    initialColor = state.sourceColor,
                    onColorSelected = { color ->
                        viewModel.onEvent(eventFactory(Pair("SetSourceColor", color)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideColorPicker"), context)
                    }
                )
            }
            
            if (state.showWalletSelector) {
                WalletSelectorDialog(
                    wallets = viewModel.wallets,
                    selectedWalletIds = state.selectedWallets,
                    onWalletSelected = { walletId, selected ->
                        viewModel.onEvent(eventFactory(Triple("SelectWallet", walletId, selected)), context)
                    },
                    onConfirm = {
                        viewModel.onEvent(eventFactory("HideWalletSelector"), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideWalletSelector"), context)
                    }
                )
            }
        }
    }
} 