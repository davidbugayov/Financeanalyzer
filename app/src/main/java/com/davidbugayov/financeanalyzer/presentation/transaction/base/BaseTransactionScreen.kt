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
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Upload
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
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import com.davidbugayov.financeanalyzer.presentation.components.FeatureAnnouncement

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
    submitEvent: E,
    onNavigateToImport: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Логируем режим экрана
    LaunchedEffect(isEditMode) {
        Timber.d("ТРАНЗАКЦИЯ-ЭКРАН: Инициализирован в режиме ${if (isEditMode) "редактирования" else "добавления"}. editMode=${state.editMode}, transactionToEdit=${state.transactionToEdit?.id}")
        
        if (isEditMode && state.transactionToEdit != null) {
            val transaction = state.transactionToEdit!!
            Timber.d("ТРАНЗАКЦИЯ-ЭКРАН: Данные загруженной транзакции: ID=${transaction.id}, amount=${transaction.amount}, category=${transaction.category}, source=${transaction.source}, date=${transaction.date}")
            Timber.d("ТРАНЗАКЦИЯ-ЭКРАН: Текущее состояние полей: amount=${state.amount}, category=${state.category}, source=${state.source}, isExpense=${state.isExpense}")
        }
    }
    // Логируем состояние ошибок
    LaunchedEffect(state.categoryError, state.sourceError, state.amountError) {
        Timber.d("Состояние ошибок: categoryError=${state.categoryError}, sourceError=${state.sourceError}, amountError=${state.amountError}")
    }

    // Отслеживаем изменения в загруженной транзакции
    LaunchedEffect(state.transactionToEdit, state.amount, state.category, state.source) {
        if (isEditMode && state.transactionToEdit != null) {
            Timber.d("ТРАНЗАКЦИЯ-ЭКРАН: Изменение состояния: amount=${state.amount}, category=${state.category}, source=${state.source}, isExpense=${state.isExpense}")
        }
    }

    // Отображение диалога с ошибкой, если она есть
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = {
                // Очищаем ошибку при закрытии диалога
                viewModel.onEvent(eventFactory(BaseTransactionEvent.ClearError), context)
            },
            title = { 
                Text(
                    text = "Ошибка", 
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = { 
                Text(
                    text = state.error ?: "Произошла неизвестная ошибка",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(eventFactory(BaseTransactionEvent.ClearError), context)
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Специальная обработка для типа транзакции на основе forceExpense
    LaunchedEffect(state.forceExpense) {
        Timber.d("forceExpense изменен: ${state.forceExpense}")
        // Если forceExpense=true и isExpense=false, переключаем на расход
        if (state.forceExpense && !state.isExpense) {
            viewModel.onEvent(eventFactory(BaseTransactionEvent.ToggleTransactionType), context)
        }
        // Если forceExpense=false и isExpense=true, переключаем на доход
        else if (!state.forceExpense && state.isExpense) {
            viewModel.onEvent(eventFactory(BaseTransactionEvent.ToggleTransactionType), context)
        }
    }

    // В режиме редактирования устанавливаем заголовок и текст кнопки
    val actualScreenTitle = if (isEditMode) "Редактирование транзакции" else screenTitle
    val actualButtonText = if (isEditMode) "Сохранить" else buttonText

    var showCancelConfirmation by remember { mutableStateOf(false) }
    var showImportConfirmation by remember { mutableStateOf(false) }

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

    // Функция для перехода на экран импорта
    fun navigateToImport() {
        if (state.title.isNotBlank() || state.amount.isNotBlank() || state.category.isNotBlank() || state.note.isNotBlank()) {
            showImportConfirmation = true
        } else if (onNavigateToImport != null) {
            onNavigateToImport()
        }
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
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt(),
                actions = {
                    if (!isEditMode && onNavigateToImport != null) {
                        IconButton(onClick = { navigateToImport() }) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Импорт транзакций",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
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
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowDatePicker), context)
                    },
                    onToggleTransactionType = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ToggleTransactionType), context)
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
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowCustomSourceDialog), context)
                        },
                        onSourceLongClick = { selectedSource ->
                            Timber.d("Source long clicked: " + selectedSource.name)
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowDeleteSourceConfirmDialog(selectedSource.name)), context)
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
                                viewModel.onEvent(eventFactory(BaseTransactionEvent.SetExpenseCategory(selectedCategory.name)), context)
                                // Обновление счетчика использования категории расходов
                                categoriesViewModel.incrementCategoryUsage(selectedCategory.name, true)
                            } else {
                                viewModel.onEvent(eventFactory(BaseTransactionEvent.SetIncomeCategory(selectedCategory.name)), context)
                                // Обновление счетчика использования категории доходов
                                categoriesViewModel.incrementCategoryUsage(selectedCategory.name, false)
                            }
                        },
                        onAddCategoryClick = {
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowCustomCategoryDialog), context)
                        },
                        onCategoryLongClick = { selectedCategory ->
                            Timber.d("Category long click in BaseTransactionScreen: " + selectedCategory.name)
                            // Don't allow long press on "Другое" and "Переводы"
                            if (selectedCategory.name != "Другое" && selectedCategory.name != "Переводы") {
                                viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowDeleteCategoryConfirmDialog(selectedCategory.name)), context)
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
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.SetAmount(amount)), context)
                        },
                        isError = state.amountError,
                        accentColor = currentColor
                    )
                }

                // Поле выбора даты
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowDatePicker), context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.spacing_normal))
                )

                // Поле для комментария без иконки прикрепления
                CommentField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetNote(note)), context)
                    }
                )

                // Секция выбора кошельков (показывается только для доходов)
                Timber.d("BaseTransactionScreen: isExpense=${state.isExpense}, addToWallet=${state.addToWallet}, selectedWallets=${state.selectedWallets}, targetWalletId=${state.targetWalletId}")
                
                WalletSelectionSection(
                    addToWallet = state.addToWallet,
                    selectedWallets = state.selectedWallets,
                    onToggleAddToWallet = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ToggleAddToWallet), context)
                    },
                    onSelectWalletsClick = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowWalletSelector), context)
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

            if (showImportConfirmation) {
                AlertDialog(
                    onDismissRequest = { showImportConfirmation = false },
                    title = { Text("Внимание") },
                    text = { Text("У вас есть несохраненные данные. Если вы перейдете к импорту, они будут потеряны.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showImportConfirmation = false
                                if (onNavigateToImport != null) {
                                    // Сбрасываем поля перед переходом
                                    viewModel.resetFields()
                                    onNavigateToImport()
                                }
                            }
                        ) {
                            Text("Перейти к импорту")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showImportConfirmation = false }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }

            if (state.isSuccess) {
                AnimatedVisibility(
                    visible = state.isSuccess,
                    enter = scaleIn(animationSpec = tween(400)),
                    exit = scaleOut(animationSpec = tween(400))
                ) {
                    SuccessDialog(
                        message = "Транзакция успешно сохранена!",
                        onDismiss = {
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.HideSuccessDialog), context)
                            handleExit()
                        },
                        onAddAnother = {
                            Timber.d("UI: Нажато 'Добавить еще'")
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.PreventAutoSubmit), context)
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.HideSuccessDialog), context)
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.ResetAmountOnly), context)
                        }
                    )
                }
            }

            if (state.showDatePicker) {
                DatePickerDialog(
                    initialDate = state.selectedDate,
                    maxDate = java.util.Date(),
                    onDateSelected = { date ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetDate(date)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideDatePicker), context)
                    }
                )
            }

            if (state.showCategoryPicker) {
                CategoryPickerDialog(
                    categories = if (state.isExpense) state.expenseCategories else state.incomeCategories,
                    onCategorySelected = { categoryName ->
                        Timber.d("Category selected from dialog: $categoryName")
                        if (state.isExpense) {
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.SetExpenseCategory(categoryName)), context)
                            // Обновление счетчика использования категории расходов через диалог
                            categoriesViewModel.incrementCategoryUsage(categoryName, true)
                        } else {
                            viewModel.onEvent(eventFactory(BaseTransactionEvent.SetIncomeCategory(categoryName)), context)
                            // Обновление счетчика использования категории доходов через диалог
                            categoriesViewModel.incrementCategoryUsage(categoryName, false)
                        }
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideCategoryPicker), context)
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowCustomCategoryDialog), context)
                    }
                )
            }

            if (state.showCustomCategoryDialog) {
                val addState = state as? AddTransactionState
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { name ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetCustomCategory(name)), context)
                    },
                    selectedIcon = addState?.customCategoryIcon ?: Icons.Default.MoreHoriz,
                    onIconSelected = { icon ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetCustomCategoryIcon(icon)), context)
                    },
                    availableIcons = addState?.availableCategoryIcons ?: emptyList(),
                    onConfirm = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.AddCustomCategory(state.customCategory)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideCustomCategoryDialog), context)
                    }
                )
            }

            if (state.showDeleteCategoryConfirmDialog) {
                state.categoryToDelete?.let { category ->
                    AlertDialog(
                        onDismissRequest = { viewModel.onEvent(eventFactory(BaseTransactionEvent.HideDeleteCategoryConfirmDialog), context) },
                        title = { Text("Удаление категории") },
                        text = { Text("Вы уверены, что хотите удалить категорию '$category'?") },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.onEvent(eventFactory(BaseTransactionEvent.DeleteCategory(category)), context) }
                            ) {
                                Text("Удалить")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.onEvent(eventFactory(BaseTransactionEvent.HideDeleteCategoryConfirmDialog), context) }
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
                        onDismissRequest = { viewModel.onEvent(eventFactory(BaseTransactionEvent.HideDeleteSourceConfirmDialog), context) },
                        title = { Text("Удаление источника") },
                        text = { Text("Вы уверены, что хотите удалить источник '$source'?") },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.onEvent(eventFactory(BaseTransactionEvent.DeleteSource(source)), context) }
                            ) {
                                Text("Удалить")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.onEvent(eventFactory(BaseTransactionEvent.HideDeleteSourceConfirmDialog), context) }
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
                    onSourceSelected = { viewModel.onEvent(eventFactory(BaseTransactionEvent.SetSource(it.name)), context) },
                    onAddCustomSource = { viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowCustomSourceDialog), context) },
                    onDismiss = { viewModel.onEvent(eventFactory(BaseTransactionEvent.HideSourcePicker), context) },
                    onDeleteSource = { sourceName ->
                        Timber.d("Delete source requested: $sourceName")
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.ShowDeleteSourceConfirmDialog(sourceName)), context)
                    }
                )
            }

            if (state.showCustomSourceDialog) {
                CustomSourceDialog(
                    sourceName = state.customSource,
                    color = state.sourceColor,
                    onSourceNameChange = { name ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetCustomSource(name)), context)
                    },
                    onColorClick = { selectedColor ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetSourceColor(selectedColor)), context)
                    },
                    onConfirm = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.AddCustomSource(state.customSource, state.sourceColor)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideCustomSourceDialog), context)
                    }
                )
            }

            if (state.showColorPicker) {
                ColorPickerDialog(
                    initialColor = state.sourceColor,
                    onColorSelected = { color ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetSourceColor(color)), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideColorPicker), context)
                    }
                )
            }

            if (state.showWalletSelector) {
                WalletSelectorDialog(
                    wallets = viewModel.wallets,
                    selectedWalletIds = state.selectedWallets,
                    onWalletSelected = { walletId, selected ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SelectWallet(walletId, selected)), context)
                    },
                    onConfirm = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideWalletSelector), context)
                    },
                    onDismiss = {
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.HideWalletSelector), context)
                    }
                )
            }
        }
    }
}

@Composable
fun ImportInfoBanner(modifier: Modifier = Modifier, onNavigateToImport: () -> Unit) {
    Timber.d("ImportInfoBanner: Attempting to show import info banner")
    FeatureAnnouncement(
        title = "Новая функция!",
        description = "Вы можете импортировать транзакции из других банков автоматически! Это упростит учет ваших финансов.",
        actionText = "Нажмите, чтобы перейти к импорту транзакций",
        preferencesKey = "import_info_add_transaction_dismissed",
        onActionClick = onNavigateToImport,
        modifier = modifier
    )
} 