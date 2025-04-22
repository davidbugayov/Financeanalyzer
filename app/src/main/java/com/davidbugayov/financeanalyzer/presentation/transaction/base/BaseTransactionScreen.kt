package com.davidbugayov.financeanalyzer.presentation.transaction.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.ErrorDialog
import com.davidbugayov.financeanalyzer.presentation.components.SuccessDialog
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.AddTransactionState
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
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Базовый экран для работы с транзакциями
 * Служит основой для AddTransactionScreen и EditTransactionScreen
 */
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
    // Логируем состояние ошибок
    LaunchedEffect(state.categoryError, state.sourceError, state.amountError) {
        Timber.d("Состояние ошибок: categoryError=${state.categoryError}, sourceError=${state.sourceError}, amountError=${state.amountError}")
    }

    // Специальная обработка для типа транзакции на основе forceExpense
    LaunchedEffect(state.forceExpense) {
        Timber.d("forceExpense изменен: ${state.forceExpense}")
        // Если forceExpense=true и isExpense=false, переключаем на расход
        if (state.forceExpense && !state.isExpense) {
            viewModel.onEvent(eventFactory("ToggleTransactionType"), context)
        }
        // Если forceExpense=false и isExpense=true, переключаем на доход
        else if (!state.forceExpense && state.isExpense) {
            viewModel.onEvent(eventFactory("ToggleTransactionType"), context)
        }
    }

    // В режиме редактирования устанавливаем заголовок и текст кнопки
    val actualScreenTitle = if (isEditMode) "Редактирование транзакции" else screenTitle
    val actualButtonText = if (isEditMode) "Сохранить" else buttonText

    var showCancelConfirmation by remember { mutableStateOf(false) }

    // Цвета для типов транзакций
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val currentColor = if (state.isExpense) expenseColor else incomeColor

    // Функция для обработки выхода с экрана
    fun handleExit() {
        // Обновляем позиции категорий перед выходом
        viewModel.updateCategoryPositions()
        // Сбрасываем поля
        viewModel.resetFields()
        // Возвращаемся назад
        onNavigateBack()
    }

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
                ImportInfoBanner()

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

                // Секция "Откуда/Куда" (Source) - теперь первая
                Column {
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
                        },
                        isError = state.sourceError
                    )
                }

                // Секция категорий - теперь вторая
                Column {
                    CategorySection(
                        categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                        selectedCategory = if (state.isExpense) state.selectedExpenseCategory else state.selectedIncomeCategory,
                        onCategorySelected = { selectedCategory -> 
                            Timber.d("Category selected directly: " + selectedCategory.name)
                            if (state.isExpense) {
                                viewModel.onEvent(eventFactory(Pair("SetExpenseCategory", selectedCategory.name)), context)
                                // Обновление счетчика использования категории расходов
                                categoriesViewModel.incrementCategoryUsage(selectedCategory.name, true)
                            } else {
                                viewModel.onEvent(eventFactory(Pair("SetIncomeCategory", selectedCategory.name)), context)
                                // Обновление счетчика использования категории доходов
                                categoriesViewModel.incrementCategoryUsage(selectedCategory.name, false)
                            }
                        },
                        onAddCategoryClick = {
                            viewModel.onEvent(eventFactory("ShowCustomCategoryDialog"), context)
                        },
                        onCategoryLongClick = { selectedCategory ->
                            Timber.d("Category long click in BaseTransactionScreen: " + selectedCategory.name)
                            // Don't allow long press on "Другое" and "Переводы"
                            if (selectedCategory.name != "Другое" && selectedCategory.name != "Переводы") {
                                viewModel.onEvent(eventFactory(Pair("DeleteCategoryConfirm", selectedCategory)), context)
                            } else {
                                Timber.d("Ignoring long press on protected category: " + selectedCategory.name)
                            }
                        },
                        isError = state.categoryError
                    )
                }

                // Поле ввода суммы
                Column {
                    AmountField(
                        amount = state.amount,
                        onAmountChange = { amount -> 
                            viewModel.onEvent(eventFactory(Pair("SetAmount", amount)), context) },
                        isError = state.amountError,
                        accentColor = currentColor
                    )
                }

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
                    isVisible = !state.isExpense && viewModel.wallets.isNotEmpty() // Показываем только для доходов и если есть кошельки
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))

                // Кнопка добавления/сохранения
                AddButton(
                    onClick = {
                        Timber.d("UI: Кнопка 'Добавить' нажата")
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
                AnimatedVisibility(visible = state.isSuccess, enter = scaleIn(animationSpec = tween(400)), exit = scaleOut(animationSpec = tween(400))) {
                    SuccessDialog(
                        onDismiss = {
                            viewModel.onEvent(eventFactory("HideSuccessDialog"), context)
                            handleExit()
                        },
                        onAddAnother = {
                            Timber.d("UI: Нажато 'Добавить еще'")
                            viewModel.onEvent(eventFactory("PreventAutoSubmit"), context)
                            viewModel.onEvent(eventFactory("HideSuccessDialog"), context)
                            viewModel.onEvent(eventFactory("ResetAmountOnly"), context)
                        },
                        isEditMode = isEditMode
                    )
                }
            }

            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    maxDate = java.util.Date(),
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
                        if (state.isExpense) {
                            viewModel.onEvent(eventFactory(Pair("SetExpenseCategory", categoryName)), context)
                            // Обновление счетчика использования категории расходов через диалог
                            categoriesViewModel.incrementCategoryUsage(categoryName, true)
                        } else {
                            viewModel.onEvent(eventFactory(Pair("SetIncomeCategory", categoryName)), context)
                            // Обновление счетчика использования категории доходов через диалог
                            categoriesViewModel.incrementCategoryUsage(categoryName, false)
                        }
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
                val addState = state as? AddTransactionState
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { name ->
                        viewModel.onEvent(eventFactory(Pair("SetCustomCategoryText", name)), context)
                    },
                    selectedIcon = addState?.customCategoryIcon ?: Icons.Default.MoreHoriz,
                    onIconSelected = { icon ->
                        viewModel.onEvent(eventFactory(Pair("SetCustomCategoryIcon", icon)), context)
                    },
                    availableIcons = addState?.availableCategoryIcons ?: emptyList(),
                    onConfirm = {
                        viewModel.onEvent(eventFactory(Pair("AddCustomCategoryConfirm", state.customCategory)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory("HideCustomCategoryDialog"), context)
                    }
                )
            }

            if (state.showDeleteCategoryConfirmDialog) {
                state.categoryToDelete?.let { category ->
                    AlertDialog(
                        onDismissRequest = { viewModel.onEvent(eventFactory("HideDeleteCategoryConfirmDialog"), context) },
                        title = { Text("Удаление категории") },
                        text = { Text("Вы уверены, что хотите удалить категорию '$category'?") },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.onEvent(eventFactory(Pair("DeleteCategoryConfirmActual", category)), context) }
                            ) {
                                Text("Удалить")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.onEvent(eventFactory("HideDeleteCategoryConfirmDialog"), context) }
                            ) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }

            if (state.showDeleteSourceConfirmDialog) {
                state.sourceToDelete?.let { source ->
                    AlertDialog(
                        onDismissRequest = { viewModel.onEvent(eventFactory("HideDeleteSourceConfirmDialog"), context) },
                        title = { Text("Удаление источника") },
                        text = { Text("Вы уверены, что хотите удалить источник '$source'?") },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.onEvent(eventFactory(Pair("DeleteSourceConfirmActual", source)), context) }
                            ) {
                                Text("Удалить")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.onEvent(eventFactory("HideDeleteSourceConfirmDialog"), context) }
                            ) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }

            if (state.showSourcePicker) {
                SourcePickerDialog(
                    sources = state.sources,
                    onSourceSelected = { viewModel.onEvent(eventFactory(Pair("SetSource", it.name)), context) },
                    onAddCustomSource = { viewModel.onEvent(eventFactory("ShowCustomSourceDialog"), context) },
                    onDismiss = { viewModel.onEvent(eventFactory("HideSourcePicker"), context) },
                    onDeleteSource = { sourceName ->
                        Timber.d("Delete source requested: $sourceName")
                        viewModel.onEvent(eventFactory(Pair("ShowDeleteSourceConfirmDialog", sourceName)), context) 
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

@Composable
fun ImportInfoBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferencesManager = remember(context) { PreferencesManager(context) }
    var visible by remember { mutableStateOf(!preferencesManager.getImportInfoDismissed()) }
    val coroutineScope = rememberCoroutineScope()

    if (visible) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.spacing_normal), vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Новая функция!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        coroutineScope.launch {
                            preferencesManager.setImportInfoDismissed(true)
                            visible = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Вы можете импортировать транзакции из других банков автоматически! Это упростит учет ваших финансов.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Для этого перейдите в профиль и выберите 'Импортировать транзакции' внизу экрана.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
} 