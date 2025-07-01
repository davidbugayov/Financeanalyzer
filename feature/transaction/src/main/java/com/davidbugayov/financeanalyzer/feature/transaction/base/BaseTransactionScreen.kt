package com.davidbugayov.financeanalyzer.feature.transaction.base
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.CancelConfirmationDialog
import com.davidbugayov.financeanalyzer.ui.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.ui.components.SuccessDialog
import com.davidbugayov.financeanalyzer.feature.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.AddButton
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.AmountField
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.CategoryPickerDialog
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.CategorySection
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.ColorPickerDialog
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.CommentField
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.CustomCategoryDialog
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.CustomSourceDialog
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.DateField
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.SourcePickerDialog
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.SourceSection
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.TransactionHeader
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.WalletSelectionSection
import com.davidbugayov.financeanalyzer.feature.transaction.base.components.WalletSelectorDialog
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.compose.koinInject
import timber.log.Timber
import com.davidbugayov.financeanalyzer.ui.components.ReminderBubble

/**
 * Базовый экран для работы с транзакциями
 * Служит основой для AddTransactionScreen и EditTransactionScreen
 */
@Composable
fun <E> BaseTransactionScreen(
    viewModel: TransactionScreenViewModel<out BaseTransactionState, E>,
    onNavigateBack: () -> Unit,
    screenTitle: String? = null,
    buttonText: String? = null,
    isEditMode: Boolean = false,
    eventFactory: (Any) -> E,
    submitEvent: E,
    onNavigateToImport: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val preferencesManager: PreferencesManager = koinInject()

    // Состояние для показа напоминания об импорте (только для экрана добавления)
    var showImportReminder by remember {
        mutableStateOf(!isEditMode && onNavigateToImport != null && !preferencesManager.isImportReminderShown())
    }

    // Устанавливаем значения по умолчанию для строковых ресурсов
    val actualScreenTitle = if (isEditMode) {
        stringResource(R.string.edit_transaction_title)
    } else {
        screenTitle ?: stringResource(R.string.add_transaction)
    }

    val actualButtonText = if (isEditMode) {
        stringResource(R.string.save_button_text)
    } else {
        buttonText ?: stringResource(R.string.add_button_text)
    }

    // Сортируем категории по частоте использования при инициализации экрана
    val sortedExpenseCategories = remember(state.expenseCategories) {
        state.expenseCategories.sortedByDescending {
            (viewModel as BaseTransactionViewModel<*, *>).getCategoryUsage(it.name, true)
        }
    }

    val sortedIncomeCategories = remember(state.incomeCategories) {
        state.incomeCategories.sortedByDescending {
            (viewModel as BaseTransactionViewModel<*, *>).getCategoryUsage(it.name, false)
        }
    }

    // Сортируем источники по частоте использования
    val sortedSources = remember(state.sources) {
        state.sources.sortedByDescending {
            (viewModel as BaseTransactionViewModel<*, *>).getSourceUsage(it.name)
        }
    }

    // Строковые ресурсы для категорий
    val categoryOther = stringResource(R.string.category_other)
    val categoryTransfer = stringResource(R.string.category_transfer)

    // Устанавливаем первый источник из отсортированного списка при инициализации
    LaunchedEffect(sortedSources) {
        if (sortedSources.isNotEmpty()) {
            val firstSource = sortedSources.first()
            Timber.d(
                "[SOURCE_SORT] Принудительная установка первого источника из отсортированного списка: %s (текущий: %s)",
                firstSource.name,
                state.source,
            )
            viewModel.onEvent(
                eventFactory(BaseTransactionEvent.SetSource(firstSource.name)),
                context,
            )
        }
    }

    // Логируем отсортированные категории при инициализации
    LaunchedEffect(sortedExpenseCategories, sortedIncomeCategories, sortedSources) {
        val baseViewModel = viewModel as BaseTransactionViewModel<*, *>
        Timber.d(
            "[CATEGORY_SORT] Expense categories sorted on init: %s",
            sortedExpenseCategories.joinToString(", ") {
                "${it.name}(${baseViewModel.getCategoryUsage(
                    it.name,
                    true,
                )})"
            },
        )
        Timber.d(
            "[CATEGORY_SORT] Income categories sorted on init: %s",
            sortedIncomeCategories.joinToString(", ") {
                "${it.name}(${baseViewModel.getCategoryUsage(
                    it.name,
                    false,
                )})"
            },
        )
        Timber.d(
            "[SOURCE_SORT] Sources sorted on init: %s",
            sortedSources.joinToString(", ") { "${it.name}(${baseViewModel.getSourceUsage(it.name)})" },
        )
    }

    // Логируем режим экрана
    LaunchedEffect(isEditMode) {
        Timber.d(
            "ТРАНЗАКЦИЯ-ЭКРАН: Инициализирован в режиме %s. editMode=%b, transactionToEdit=%s",
            if (isEditMode) "редактирования" else "добавления",
            state.editMode,
            state.transactionToEdit?.id,
        )

        if (isEditMode && state.transactionToEdit != null) {
            val transaction = state.transactionToEdit!!
            Timber.d(
                "ТРАНЗАКЦИЯ-ЭКРАН: Данные загруженной транзакции: ID=%s, amount=%s, category=%s, source=%s, date=%s",
                transaction.id,
                transaction.amount,
                transaction.category,
                transaction.source,
                transaction.date,
            )
            Timber.d(
                "ТРАНЗАКЦИЯ-ЭКРАН: Текущее состояние полей: amount=%s, category=%s, source=%s, isExpense=%b",
                state.amount,
                state.category,
                state.source,
                state.isExpense,
            )
        }
    }
    // Логируем состояние ошибок
    LaunchedEffect(state.categoryError, state.sourceError, state.amountError) {
        Timber.d(
            "Состояние ошибок: categoryError=%b, sourceError=%b, amountError=%b",
            state.categoryError,
            state.sourceError,
            state.amountError,
        )
    }

    // Отслеживаем изменения в загруженной транзакции
    LaunchedEffect(state.transactionToEdit, state.amount, state.category, state.source) {
        if (isEditMode && state.transactionToEdit != null) {
            Timber.d(
                "ТРАНЗАКЦИЯ-ЭКРАН: Изменение состояния: amount=%s, category=%s, source=%s, isExpense=%b",
                state.amount,
                state.category,
                state.source,
                state.isExpense,
            )
        }
    }

    // Отображение диалога с ошибкой, если она есть
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = {
                // Очищаем ошибку при закрытии диалога
                viewModel.onEvent(eventFactory(BaseTransactionEvent.ClearError), context)
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.error_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = state.error ?: stringResource(R.string.unknown_error_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(eventFactory(BaseTransactionEvent.ClearError), context)
                }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    // Специальная обработка для типа транзакции на основе forceExpense
    LaunchedEffect(state.forceExpense) {
        Timber.d("BaseTransactionScreen: forceExpense изменен: %b, текущий isExpense: %b", state.forceExpense, state.isExpense)
        // Если forceExpense=true и isExpense=false, принудительно устанавливаем расход
        if (state.forceExpense && !state.isExpense) {
            Timber.d("BaseTransactionScreen: Принудительно устанавливаем тип транзакции на РАСХОД")
            viewModel.onEvent(eventFactory(BaseTransactionEvent.ForceSetExpenseType), context)
        }
        // Если forceExpense=false и isExpense=true, принудительно устанавливаем доход
        else if (!state.forceExpense && state.isExpense) {
            Timber.d("BaseTransactionScreen: Принудительно устанавливаем тип транзакции на ДОХОД")
            viewModel.onEvent(eventFactory(BaseTransactionEvent.ForceSetIncomeType), context)
        }
    }

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
        // Обновляем позиции источников перед выходом
        viewModel.updateSourcePositions()
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

    // Функция для обработки закрытия напоминания об импорте
    fun handleImportReminderDismiss() {
        showImportReminder = false
        preferencesManager.setImportReminderShown()
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
                actions = {
                    if (!isEditMode && onNavigateToImport != null) {
                        IconButton(onClick = { navigateToImport() }) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = stringResource(
                                    R.string.import_transactions_content_description,
                                ),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
            ) {
                // Показываем напоминание об импорте только на экране добавления транзакции
                if (!isEditMode && onNavigateToImport != null) {
                    ReminderBubble(
                        visible = showImportReminder,
                        title = stringResource(R.string.import_transactions_title),
                        description = stringResource(R.string.import_transactions_hint),
                        actionButtonText = stringResource(R.string.import_button),
                        dismissButtonText = stringResource(R.string.close),
                        onDismiss = { handleImportReminderDismiss() },
                        onAction = {
                            handleImportReminderDismiss()
                            navigateToImport()
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Заголовок с датой и типом транзакции
                TransactionHeader(
                    date = state.selectedDate,
                    isExpense = state.isExpense,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    onDateClick = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ShowDatePicker),
                            context,
                        )
                    },
                    onToggleTransactionType = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ToggleTransactionType),
                            context,
                        )
                    },
                    forceExpense = state.forceExpense,
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                // Секция "Откуда/Куда" (Source)
                Column {
                    Timber.d(
                        "Rendering SourceSection with isExpense=%b, selectedSource=%s, sources count=%d",
                        state.isExpense,
                        state.source,
                        state.sources.size,
                    )
                    SourceSection(
                        sources = sortedSources,
                        selectedSource = state.source,
                        onSourceSelected = { selectedSource ->
                            Timber.d(
                                "Source selected directly: %s with color %s",
                                selectedSource.name,
                                selectedSource.color,
                            )
                            viewModel.onEvent(eventFactory(selectedSource), context)
                        },
                        onAddSourceClick = {
                            Timber.d("Add source button clicked")
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.ShowCustomSourceDialog),
                                context,
                            )
                        },
                        onSourceLongClick = { selectedSource ->
                            Timber.d("Source long clicked: %s", selectedSource.name)
                            viewModel.onEvent(
                                eventFactory(
                                    BaseTransactionEvent.ShowDeleteSourceConfirmDialog(
                                        selectedSource.name,
                                    ),
                                ),
                                context,
                            )
                        },
                        isError = state.sourceError,
                    )
                }
                // Секция выбора категории
                if (state.isExpense) {
                    CategorySection(
                        categories = sortedExpenseCategories,
                        selectedCategory = state.category,
                        onCategorySelected = { category ->
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.SetCategory(category.name)),
                                context,
                            )
                        },
                        onAddCategoryClick = {
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.ShowCustomCategoryDialog),
                                context,
                            )
                        },
                        onCategoryLongClick = { selectedCategory ->
                            Timber.d(
                                "Category long click in BaseTransactionScreen: %s",
                                selectedCategory.name,
                            )
                            // Don't allow long press on "Другое" and "Переводы"
                            if (selectedCategory.name != categoryOther && selectedCategory.name != categoryTransfer) {
                                viewModel.onEvent(
                                    eventFactory(
                                        BaseTransactionEvent.ShowDeleteCategoryConfirmDialog(
                                            selectedCategory.name,
                                        ),
                                    ),
                                    context,
                                )
                            } else {
                                Timber.d(
                                    "Ignoring long press on protected category: %s",
                                    selectedCategory.name,
                                )
                            }
                        },
                        isError = state.categoryError,
                    )
                } else {
                    CategorySection(
                        categories = sortedIncomeCategories,
                        selectedCategory = state.category,
                        onCategorySelected = { category ->
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.SetCategory(category.name)),
                                context,
                            )
                        },
                        onAddCategoryClick = {
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.ShowCustomCategoryDialog),
                                context,
                            )
                        },
                        onCategoryLongClick = { selectedCategory ->
                            Timber.d(
                                "Category long click in BaseTransactionScreen: %s",
                                selectedCategory.name,
                            )
                            if (selectedCategory.name != categoryOther && selectedCategory.name != categoryTransfer) {
                                viewModel.onEvent(
                                    eventFactory(
                                        BaseTransactionEvent.ShowDeleteCategoryConfirmDialog(
                                            selectedCategory.name,
                                        ),
                                    ),
                                    context,
                                )
                            } else {
                                Timber.d(
                                    "Ignoring long press on protected category: %s",
                                    selectedCategory.name,
                                )
                            }
                        },
                        isError = state.categoryError,
                    )
                }
                // Поле ввода суммы и кнопки операций — после категории
                AmountField(
                    amount = state.amount,
                    onAmountChange = { amount ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetAmount(amount)),
                            context,
                        )
                    },
                    isError = state.amountError,
                    accentColor = currentColor,
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
                // Поле выбора даты
                DateField(
                    date = state.selectedDate,
                    onClick = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ShowDatePicker),
                            context,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.spacing_normal)),
                )

                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

                // Поле для комментария без иконки прикрепления
                CommentField(
                    note = state.note,
                    onNoteChange = { note ->
                        viewModel.onEvent(eventFactory(BaseTransactionEvent.SetNote(note)), context)
                    },
                )

                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))

                // Секция выбора кошельков (показывается только для доходов)
                Timber.d(
                    "BaseTransactionScreen: isExpense=${state.isExpense}, addToWallet=${state.addToWallet}, selectedWallets=${state.selectedWallets}, targetWalletId=${state.targetWalletId}",
                )

                WalletSelectionSection(
                    addToWallet = state.addToWallet,
                    selectedWallets = state.selectedWallets,
                    onToggleAddToWallet = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ToggleAddToWallet),
                            context,
                        )
                    },
                    onSelectWalletsClick = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ShowWalletSelector),
                            context,
                        )
                    },
                    isVisible = !state.isExpense && viewModel.wallets.isNotEmpty(), // Показываем только для доходов и если есть кошельки
                )

                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                // Кнопка добавления/сохранения
                AddButton(
                    onClick = {
                        Timber.d("UI: Кнопка 'Добавить' нажата")
                        viewModel.onEvent(submitEvent, context)
                    },
                    text = actualButtonText,
                    color = currentColor,
                    isLoading = state.isLoading,
                )

                Spacer(Modifier.height(dimensionResource(R.dimen.spacing_medium)))
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
                    },
                )
            }

            if (showImportConfirmation) {
                AlertDialog(
                    onDismissRequest = { showImportConfirmation = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = { Text(stringResource(R.string.attention_title)) },
                    text = { Text(stringResource(R.string.unsaved_data_warning)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showImportConfirmation = false
                                if (onNavigateToImport != null) {
                                    // Сбрасываем поля перед переходом
                                    viewModel.resetFields()
                                    onNavigateToImport()
                                }
                            },
                        ) {
                            Text(stringResource(R.string.proceed_to_import))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showImportConfirmation = false },
                        ) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                    },
                )
            }

            if (state.isSuccess) {
                AnimatedVisibility(
                    visible = state.isSuccess,
                    enter = scaleIn(animationSpec = tween(400)),
                    exit = scaleOut(animationSpec = tween(400)),
                ) {
                    SuccessDialog(
                        message = stringResource(R.string.transaction_saved_success),
                        onDismiss = {
                            Timber.d("UI: Нажато 'OK' в диалоге успеха, вызываем handleExit()")
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.HideSuccessDialog),
                                context,
                            )
                            handleExit()
                        },
                        onAddAnother = {
                            Timber.d("UI: Нажато 'Добавить еще'")
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.PreventAutoSubmit),
                                context,
                            )
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.HideSuccessDialog),
                                context,
                            )
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.ResetFieldsForNewTransaction),
                                context,
                            )
                        },
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
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideDatePicker),
                            context,
                        )
                    },
                )
            }

            if (state.showCategoryPicker) {
                val categories = if (state.isExpense) state.expenseCategories else state.incomeCategories
                // Sort categories by usage frequency before displaying them
                val sortedCategories = categories.sortedByDescending {
                    if (state.isExpense) {
                        (viewModel as BaseTransactionViewModel<*, *>).getCategoryUsage(
                            it.name,
                            true,
                        )
                    } else {
                        (viewModel as BaseTransactionViewModel<*, *>).getCategoryUsage(
                            it.name,
                            false,
                        )
                    }
                }

                CategoryPickerDialog(
                    categories = sortedCategories,
                    onCategorySelected = { categoryName ->
                        Timber.d("Category selected from dialog: $categoryName")
                        if (state.isExpense) {
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.SetExpenseCategory(categoryName)),
                                context,
                            )
                            // Обновление счетчика использования категории расходов через диалог
                            (viewModel as BaseTransactionViewModel<*, *>).incrementCategoryUsage(
                                categoryName,
                                true,
                            )
                        } else {
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.SetIncomeCategory(categoryName)),
                                context,
                            )
                            // Обновление счетчика использования категории доходов через диалог
                            (viewModel as BaseTransactionViewModel<*, *>).incrementCategoryUsage(
                                categoryName,
                                false,
                            )
                        }
                    },
                    onDismiss = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideCategoryPicker),
                            context,
                        )
                    },
                    onCustomCategoryClick = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ShowCustomCategoryDialog),
                            context,
                        )
                    },
                )
            }

            if (state.showCustomCategoryDialog) {
                val addState = state as? AddTransactionState
                CustomCategoryDialog(
                    categoryText = state.customCategory,
                    onCategoryTextChange = { name ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetCustomCategory(name)),
                            context,
                        )
                    },
                    onConfirm = {
                        viewModel.onEvent(
                            eventFactory(
                                BaseTransactionEvent.AddCustomCategory(state.customCategory),
                            ),
                            context,
                        )
                    },
                    onDismiss = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideCustomCategoryDialog),
                            context,
                        )
                    },
                    availableIcons = addState?.availableCategoryIcons ?: emptyList(),
                    selectedIcon = addState?.customCategoryIcon,
                    onIconSelected = { icon ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetCustomCategoryIcon(icon)),
                            context,
                        )
                    },
                )
            }

            if (state.showDeleteCategoryConfirmDialog) {
                state.categoryToDelete?.let { category ->
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.HideDeleteCategoryConfirmDialog),
                                context,
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = { Text(stringResource(R.string.delete_category_title)) },
                        text = {
                            Text(
                                stringResource(R.string.delete_category_confirmation, category),
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.onEvent(
                                        eventFactory(BaseTransactionEvent.DeleteCategory(category)),
                                        context,
                                    )
                                },
                            ) {
                                Text(stringResource(R.string.dialog_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(
                                        eventFactory(
                                            BaseTransactionEvent.HideDeleteCategoryConfirmDialog,
                                        ),
                                        context,
                                    )
                                },
                            ) {
                                Text(stringResource(R.string.dialog_cancel))
                            }
                        },
                    )
                }
            }

            if (state.showDeleteSourceConfirmDialog) {
                state.sourceToDelete?.let { source ->
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.onEvent(
                                eventFactory(BaseTransactionEvent.HideDeleteSourceConfirmDialog),
                                context,
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = { Text(stringResource(R.string.delete_source_title)) },
                        text = { Text(stringResource(R.string.delete_source_confirmation, source)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.onEvent(
                                        eventFactory(BaseTransactionEvent.DeleteSource(source)),
                                        context,
                                    )
                                },
                            ) {
                                Text(stringResource(R.string.dialog_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(
                                        eventFactory(BaseTransactionEvent.HideDeleteSourceConfirmDialog),
                                        context,
                                    )
                                },
                            ) {
                                Text(stringResource(R.string.dialog_cancel))
                            }
                        },
                    )
                }
            }

            if (state.showSourcePicker) {
                SourcePickerDialog(
                    sources = state.sources,
                    onSourceSelected = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetSource(it.name)),
                            context,
                        )
                    },
                    onAddCustomSource = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.ShowCustomSourceDialog),
                            context,
                        )
                    },
                    onDismiss = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideSourcePicker),
                            context,
                        )
                    },
                    onDeleteSource = { sourceName ->
                        Timber.d("Delete source requested: $sourceName")
                        viewModel.onEvent(
                            eventFactory(
                                BaseTransactionEvent.ShowDeleteSourceConfirmDialog(sourceName),
                            ),
                            context,
                        )
                    },
                )
            }

            if (state.showCustomSourceDialog) {
                CustomSourceDialog(
                    sourceName = state.customSource,
                    color = state.sourceColor,
                    onSourceNameChange = { name ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetCustomSource(name)),
                            context,
                        )
                    },
                    onColorClick = { selectedColor ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetSourceColor(selectedColor)),
                            context,
                        )
                    },
                    onConfirm = {
                        viewModel.onEvent(
                            eventFactory(
                                BaseTransactionEvent.AddCustomSource(
                                    state.customSource,
                                    state.sourceColor,
                                ),
                            ),
                            context,
                        )
                    },
                    onDismiss = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideCustomSourceDialog),
                            context,
                        )
                    },
                )
            }

            if (state.showColorPicker) {
                ColorPickerDialog(
                    initialColor = state.sourceColor,
                    onColorSelected = { color ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SetSourceColor(color)),
                            context,
                        )
                    },
                    onDismiss = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideColorPicker),
                            context,
                        )
                    },
                )
            }

            if (state.showWalletSelector) {
                WalletSelectorDialog(
                    wallets = viewModel.wallets,
                    selectedWalletIds = state.selectedWallets,
                    onWalletSelected = { walletId, selected ->
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.SelectWallet(walletId, selected)),
                            context,
                        )
                    },
                    onConfirm = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideWalletSelector),
                            context,
                        )
                    },
                    onDismiss = {
                        viewModel.onEvent(
                            eventFactory(BaseTransactionEvent.HideWalletSelector),
                            context,
                        )
                    },
                )
            }
        }
    }
}
