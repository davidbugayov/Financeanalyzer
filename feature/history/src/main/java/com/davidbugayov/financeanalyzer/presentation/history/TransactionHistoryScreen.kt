@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.davidbugayov.financeanalyzer.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.subcategory.GetSubcategoryByIdUseCase
import com.davidbugayov.financeanalyzer.feature.transaction.base.util.getInitialSources
import com.davidbugayov.financeanalyzer.feature.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.history.components.CategoryStatsCard
import com.davidbugayov.financeanalyzer.presentation.history.components.GroupingChips
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.CategorySelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteCategoryConfirmDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteSourceConfirmDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.SourceSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.event.TransactionHistoryEvent
import com.davidbugayov.financeanalyzer.presentation.history.state.TransactionHistoryState
import com.davidbugayov.financeanalyzer.presentation.util.UiUtils
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.ui.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.ui.components.EmptyContent
import com.davidbugayov.financeanalyzer.ui.components.ErrorContent
import com.davidbugayov.financeanalyzer.ui.components.TransactionActionsDialog
import com.davidbugayov.financeanalyzer.ui.components.TransactionActionsHandler
import com.davidbugayov.financeanalyzer.ui.components.TransactionDetailDialog
import com.davidbugayov.financeanalyzer.ui.components.TransactionDialogState
import com.davidbugayov.financeanalyzer.ui.components.TransactionEvent
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryLocalization
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Преобразует TransactionHistoryState в TransactionDialogState
 */
fun TransactionHistoryState.toTransactionDialogState(): TransactionDialogState =
    TransactionDialogState(
        transactionToDelete = this.transactionToDelete,
        showDeleteConfirmDialog = this.transactionToDelete != null,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel = koinViewModel(),
    editTransactionViewModel: EditTransactionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Локальное состояние для контекстного меню транзакций
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var categoryIconForActions by remember { mutableStateOf<ImageVector?>(null) }
    var categoryColorForActions by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }

    // Состояние для подкатегории
    val getSubcategoryByIdUseCase: GetSubcategoryByIdUseCase = koinInject()
    var subcategoryNameForActions by remember { mutableStateOf("") }
    var subcategoryNameForDetail by remember { mutableStateOf("") }

    // Загрузка подкатегории для диалога действий
    LaunchedEffect(selectedTransaction?.subcategoryId) {
        selectedTransaction?.subcategoryId?.let { subcategoryId ->
            try {
                val subcategory = getSubcategoryByIdUseCase(subcategoryId)
                subcategoryNameForActions = subcategory?.name ?: ""
            } catch (_: Exception) {
                subcategoryNameForActions = ""
            }
        } ?: run {
            subcategoryNameForActions = ""
        }
    }

    // Загрузка подкатегории для диалога детальной информации
    LaunchedEffect(selectedTransactionForDetail?.subcategoryId) {
        selectedTransactionForDetail?.subcategoryId?.let { subcategoryId ->
            try {
                val subcategory = getSubcategoryByIdUseCase(subcategoryId)
                subcategoryNameForDetail = subcategory?.name ?: ""
            } catch (_: Exception) {
                subcategoryNameForDetail = ""
            }
        } ?: run {
            subcategoryNameForDetail = ""
        }
    }

    // Функция для обработки событий транзакций
    fun handleTransactionEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.ShowDeleteConfirmDialog -> {
                showActionsDialog = true
                selectedTransaction = event.transaction
            }
            is TransactionEvent.HideDeleteConfirmDialog -> {
                showActionsDialog = false
                selectedTransaction = null
            }
            is TransactionEvent.DeleteTransaction -> {
                viewModel.onEvent(TransactionHistoryEvent.DeleteTransaction(event.transaction))
                // Закрываем диалог подтверждения после удаления
                viewModel.onEvent(TransactionHistoryEvent.HideDeleteConfirmDialog)
            }
            is TransactionEvent.ShowEditDialog -> {
                // Навигация на экран редактирования
                viewModel.onEditTransaction(event.transactionId)
            }

            is TransactionEvent.HideEditDialog -> {
                // Ничего не делаем, так как мы переходим на новый экран
            }
        }
    }

    // Логируем открытие экрана истории
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "transaction_history",
            screenClass = "TransactionHistoryScreen",
        )
        Timber.d("TransactionHistoryScreen открыт, текущий период: ${state.periodType}")
        viewModel.loadTransactions()
        viewModel.checkTransactionCount()
    }

    // Отслеживаем изменения периода
    LaunchedEffect(state.periodType) {
        Timber.d(
            "Период изменен: ${state.periodType}, транзакций: ${state.transactions.size}",
        )
    }

    // Получаем список всех категорий из CategoriesViewModel
    val expenseCategories by viewModel.categoriesViewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.categoriesViewModel.incomeCategories.collectAsState()

    // Подбор иконки при смене выбранной транзакции
    LaunchedEffect(selectedTransaction?.category, expenseCategories, incomeCategories) {
        val rawName = selectedTransaction?.category
        if (rawName.isNullOrBlank()) {
            categoryIconForActions = null
            categoryColorForActions = null
        } else {
            val key = CategoryLocalization.keyFor(rawName) ?: rawName.trim().lowercase()
            val match =
                expenseCategories.firstOrNull { (CategoryLocalization.keyFor(it.name) ?: it.name.lowercase()) == key }
                    ?: incomeCategories.firstOrNull { (CategoryLocalization.keyFor(it.name) ?: it.name.lowercase()) == key }
            categoryIconForActions = match?.icon
            categoryColorForActions = match?.color
        }
    }

    // Преобразуем категории в списки строк для отображения
    val expenseCategoryNames =
        remember(expenseCategories) {
            expenseCategories.map { it.name }.filter { it != "Другое" }.sorted()
        }

    val incomeCategoryNames =
        remember(incomeCategories) {
            incomeCategories.map { it.name }.filter { it != "Другое" }.sorted()
        }

    // Диалог выбора периода
    if (state.showPeriodDialog) {
        PeriodSelectionDialog(
            selectedPeriod = state.periodType,
            startDate = state.startDate,
            endDate = state.endDate,
            onPeriodSelected = {
                viewModel.onEvent(TransactionHistoryEvent.SetPeriodType(it))
                if (it != PeriodType.CUSTOM) {
                    viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog)
                }
            },
            onStartDateClick = {
                viewModel.onEvent(TransactionHistoryEvent.ShowStartDatePicker)
            },
            onEndDateClick = {
                viewModel.onEvent(TransactionHistoryEvent.ShowEndDatePicker)
            },
            onConfirm = {
                viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog)
            },
        )
    }

    // Диалоги выбора дат для кастомного периода
    if (state.showStartDatePicker) {
        DatePickerDialog(
            initialDate = state.startDate,
            maxDate =
                minOf(
                    state.endDate,
                    java.util.Calendar
                        .getInstance()
                        .time,
                ),
            onDateSelected = { date ->
                viewModel.onEvent(TransactionHistoryEvent.SetStartDate(date))
                viewModel.onEvent(TransactionHistoryEvent.HideStartDatePicker)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideStartDatePicker)
            },
        )
    }

    if (state.showEndDatePicker) {
        DatePickerDialog(
            initialDate = state.endDate,
            minDate = state.startDate,
            maxDate =
                java.util.Calendar
                    .getInstance()
                    .time,
            onDateSelected = { date ->
                viewModel.onEvent(TransactionHistoryEvent.SetEndDate(date))
                viewModel.onEvent(TransactionHistoryEvent.HideEndDatePicker)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideEndDatePicker)
            },
        )
    }

    // Диалог выбора категорий
    if (state.showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategories = state.selectedCategories,
            expenseCategories = expenseCategoryNames,
            incomeCategories = incomeCategoryNames,
            onCategoriesSelected = { categories ->
                viewModel.onEvent(TransactionHistoryEvent.SetCategories(categories))
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
            },
        )
    }

    // Диалог подтверждения удаления категории
    state.categoryToDelete?.let { (category, isExpense) ->
        // Проверяем, является ли категория стандартной (не пользовательской)
        val isDefaultCategory =
            if (isExpense) {
                expenseCategories.find { it.name == category }?.let { !it.isCustom } ?: false
            } else {
                incomeCategories.find { it.name == category }?.let { !it.isCustom } ?: false
            }

        DeleteCategoryConfirmDialog(
            category = category,
            onConfirm = {
                viewModel.onEvent(TransactionHistoryEvent.DeleteCategory(category, isExpense))
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideDeleteCategoryConfirmDialog)
            },
            isDefaultCategory = isDefaultCategory,
        )
    }

    // Диалог с действиями для транзакции
    if (showActionsDialog && selectedTransaction != null) {
        TransactionActionsDialog(
            transaction = selectedTransaction!!,
            onDismiss = { showActionsDialog = false },
            onDelete = { transaction ->
                // Вместо локального состояния вызываем showDeleteConfirmDialog через viewModel
                viewModel.onEvent(TransactionHistoryEvent.ShowDeleteConfirmDialog(transaction))
                showActionsDialog = false
                selectedTransaction = null
            },
            onEdit = { transaction ->
                showActionsDialog = false
                handleTransactionEvent(TransactionEvent.ShowEditDialog(transaction.id))
            },
            subcategoryName = subcategoryNameForActions,
            categoryIcon = categoryIconForActions,
            categoryColor = categoryColorForActions,
        )
    }

    // Диалог подтверждения удаления транзакции через TransactionActionsHandler (как в HomeScreen)
    TransactionActionsHandler(
        transactionDialogState = state.toTransactionDialogState(),
        onEvent = { event -> handleTransactionEvent(event) },
        onNavigateToEdit = { transactionId ->
            editTransactionViewModel.loadTransactionForEditById(transactionId)
            viewModel.onEditTransaction(transactionId)
            Timber.d("Navigating to edit transaction with ID: $transactionId")
        },
    )

    // Диалог выбора источника
    if (state.showSourceDialog) {
        val context = LocalContext.current
        val sourcePreferences = SourcePreferences.getInstance(context)
        val sources = getInitialSources(sourcePreferences, context.resources)

        SourceSelectionDialog(
            selectedSources = state.selectedSources,
            sources = sources,
            onSourcesSelected = { selectedSources ->
                viewModel.onEvent(TransactionHistoryEvent.SetSources(selectedSources))
                viewModel.onEvent(TransactionHistoryEvent.HideSourceDialog)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideSourceDialog)
            },
        )
    }

    // Диалог подтверждения удаления источника
    state.sourceToDelete?.let { source ->
        DeleteSourceConfirmDialog(
            source = source,
            onConfirm = {
                viewModel.onEvent(TransactionHistoryEvent.DeleteSource(source))
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideDeleteSourceConfirmDialog)
            },
        )
    }

    // Диалог детальной информации о транзакции
    if (showDetailDialog && selectedTransactionForDetail != null) {
        TransactionDetailDialog(
            transaction = selectedTransactionForDetail!!,
            onDismiss = {
                showDetailDialog = false
                selectedTransactionForDetail = null
            },
            subcategoryName = subcategoryNameForDetail,
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(UiR.string.transaction_history),
                showBackButton = true,
                onBackClick = viewModel::onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowCategoryDialog) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Category,
                            contentDescription = stringResource(UiR.string.category_filter),
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowSourceDialog) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = stringResource(UiR.string.source_filter),
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowPeriodDialog) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = stringResource(UiR.string.filter),
                        )
                    }
                },
            )
        },
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(TransactionHistoryEvent.NavigateToAddTransaction) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(UiR.string.add_transaction),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensionResource(UiR.dimen.spacing_normal)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GroupingChips(
                        currentGrouping = state.groupingType,
                        onGroupingSelected = {
                            viewModel.onEvent(TransactionHistoryEvent.SetGroupingType(it))
                        },
                    )
                }

                // Отображение выбранного периода
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(UiR.dimen.spacing_small)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val context = LocalContext.current
                    Text(
                        text =
                            UiUtils.formatPeriod(
                                context,
                                state.periodType,
                                state.startDate,
                                state.endDate,
                            ),
                        fontSize = dimensionResource(UiR.dimen.text_size_medium).value.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Отображение выбранных фильтров
                if (state.selectedCategories.isNotEmpty() || state.selectedSources.isNotEmpty()) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimensionResource(UiR.dimen.spacing_small)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Выбранные категории
                        if (state.selectedCategories.isNotEmpty()) {
                            Text(
                                text =
                                    stringResource(
                                        UiR.string.categories_selected,
                                        state.selectedCategories.joinToString(", "),
                                    ),
                                fontSize = dimensionResource(UiR.dimen.text_size_small).value.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(end = dimensionResource(UiR.dimen.spacing_medium)),
                            )
                        }

                        // Выбранные источники
                        if (state.selectedSources.isNotEmpty()) {
                            Text(
                                text =
                                    stringResource(
                                        UiR.string.sources_selected,
                                        state.selectedSources.joinToString(", "),
                                    ),
                                fontSize = dimensionResource(UiR.dimen.text_size_small).value.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Показываем статистику по категории
                state.categoryStats?.let { (currentTotal, previousTotal, percentChange) ->
                    if (state.selectedCategories.size == 1) {
                        CategoryStatsCard(
                            category = state.selectedCategories.first(),
                            currentTotal = currentTotal,
                            previousTotal = previousTotal,
                            percentChange = percentChange,
                        )
                    }
                }

                if (state.error != null) {
                    ErrorContent(
                        error = state.error,
                        onRetry = { viewModel.onEvent(TransactionHistoryEvent.ReloadTransactions) },
                    )
                } else if (state.isLoading && !state.isLoadingMore) {
                    CenteredLoadingIndicator(
                        message = stringResource(UiR.string.loading_data),
                    )
                } else if (state.filteredTransactions.isEmpty()) {
                    EmptyContent()
                } else {
                    // Paging список транзакций без группировки
                    val pagingItems = viewModel.pagedUiModels.collectAsLazyPagingItems()

                    when {
                        pagingItems.loadState.refresh is LoadState.Loading -> {
                            CenteredLoadingIndicator(
                                message = stringResource(UiR.string.loading_data),
                            )
                        }
                        pagingItems.loadState.refresh is LoadState.Error -> {
                            val e = (pagingItems.loadState.refresh as LoadState.Error).error
                            ErrorContent(error = e.localizedMessage ?: "Error") {
                                pagingItems.retry()
                            }
                        }
                        pagingItems.itemCount == 0 -> {
                            EmptyContent()
                        }
                        else -> {
                            if (state.groupedTransactions.isNotEmpty()) {
                                // новый аккордеон-список
                                com.davidbugayov.financeanalyzer.presentation.components.groupedTransactionList(
                                    groups = state.groupedTransactions,
                                    categoriesViewModel = viewModel.categoriesViewModel,
                                    onTransactionClick = { tx ->
                                        selectedTransaction = tx
                                        showActionsDialog = true
                                    },
                                    onTransactionLongClick = { _ ->
                                        // long tap отключён
                                    },
                                )
                            } else {
                                com.davidbugayov.financeanalyzer.presentation.components.paging.transactionPagingList(
                                    items = pagingItems,
                                    categoriesViewModel = viewModel.categoriesViewModel,
                                    onTransactionClick = { transaction ->
                                        selectedTransaction = transaction
                                        showActionsDialog = true
                                    },
                                    onTransactionLongClick = { _ ->
                                        // long tap отключён
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
