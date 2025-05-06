package com.davidbugayov.financeanalyzer.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.components.AnimatedBottomNavigationBar
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.components.DeleteTransactionDialog
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackMessage
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackType
import com.davidbugayov.financeanalyzer.presentation.components.TransactionActionsDialog
import com.davidbugayov.financeanalyzer.presentation.home.components.CompactLayout
import com.davidbugayov.financeanalyzer.presentation.home.components.ExpandedLayout
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
@Composable
private fun HomeTopBar(
    onGenerateTestData: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    AppTopBar(
        title = stringResource(R.string.app_title),
        navigationIcon = {
            if (BuildConfig.DEBUG) {
                IconButton(
                    onClick = onGenerateTestData
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.generate_test_data)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile)
                )
            }
        },
        titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
    )
}

@Composable
private fun HomeBottomBar(
    onNavigateToChart: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    AnimatedBottomNavigationBar(
        visible = true,
        onChartClick = onNavigateToChart,
        onHistoryClick = onNavigateToHistory,
        onAddClick = onNavigateToAdd
    )
}

@Composable
private fun HomeMainContent(
    windowSizeIsCompact: Boolean,
    state: HomeState,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit
) {
    if (windowSizeIsCompact) {
        CompactLayout(
            state = state,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick
        )
    } else {
        ExpandedLayout(
            state = state,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick
        )
    }
}

@Composable
private fun HomeDialogs(
    showActionsDialog: Boolean,
    selectedTransaction: Transaction?,
    onDismissActionsDialog: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    transactionToDelete: Transaction?,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit
) {
    if (showActionsDialog && selectedTransaction != null) {
        TransactionActionsDialog(
            transaction = selectedTransaction,
            onDismiss = onDismissActionsDialog,
            onDelete = onDeleteTransaction,
            onEdit = onEditTransaction
        )
    }
    transactionToDelete?.let { transaction ->
        DeleteTransactionDialog(
            transaction = transaction,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete
        )
    }
}

@Composable
private fun HomeFeedback(
    feedbackMessage: String,
    feedbackType: FeedbackType,
    showFeedback: Boolean,
    onDismiss: () -> Unit
) {
    FeedbackMessage(
        message = feedbackMessage,
        type = feedbackType,
        visible = showFeedback,
        onDismiss = onDismiss,
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.padding_small))
    )
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    editTransactionViewModel: EditTransactionViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToChart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val windowSize = rememberWindowSize()
    val updateWidgetsUseCase: UpdateWidgetsUseCase = koinInject()

    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(FeedbackType.INFO) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)

    val testDataGeneratedMsg = stringResource(R.string.test_data_generated)
    val transactionDeletedMsg = stringResource(R.string.transaction_deleted)
    val emptyTransactionIdErrorMsg = stringResource(R.string.empty_transaction_id_error)

    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "home",
            screenClass = "HomeScreen"
        )
        updateWidgetsUseCase(context)
    }
    LaunchedEffect(Unit) {
        val savedShowSummary = sharedPreferences.getBoolean("show_group_summary", false)
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(savedShowSummary))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var lastRefreshTime = 0L
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRefreshTime > 2000) {
                    viewModel.onEvent(HomeEvent.LoadTransactions)
                    lastRefreshTime = currentTime
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val onTransactionClick = remember<(Transaction) -> Unit> {
        { transaction ->
            selectedTransaction = transaction
            showActionsDialog = true
        }
    }
    val onTransactionLongClick = remember<(Transaction) -> Unit> {
        { transaction ->
            selectedTransaction = transaction
            showActionsDialog = true
        }
    }
    val onToggleGroupSummary = remember<(Boolean) -> Unit> {
        { newValue ->
            viewModel.onEvent(HomeEvent.SetShowGroupSummary(newValue))
            sharedPreferences.edit { putBoolean("show_group_summary", newValue) }
        }
    }
    val onFilterSelected = remember<(TransactionFilter) -> Unit> {
        { filter -> viewModel.onEvent(HomeEvent.SetFilter(filter)) }
    }
    Scaffold(
        topBar = {
            HomeTopBar(
                onGenerateTestData = {
                    viewModel.onEvent(HomeEvent.GenerateTestData)
                    feedbackMessage = testDataGeneratedMsg
                    feedbackType = FeedbackType.SUCCESS
                    showFeedback = true
                },
                onNavigateToProfile = onNavigateToProfile
            )
        },
        bottomBar = {
            HomeBottomBar(
                onNavigateToChart = onNavigateToChart,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToAdd = onNavigateToAdd
            )
        }
    ) { paddingValues ->
        HomeScreenContent(
            paddingValues = paddingValues,
            windowSizeIsCompact = windowSize.isCompact(),
            state = state,
            showGroupSummary = state.showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onNavigateToAdd,
            showActionsDialog = showActionsDialog,
            selectedTransaction = selectedTransaction,
            onDismissActionsDialog = { showActionsDialog = false },
            onDeleteTransaction = { transaction ->
                showActionsDialog = false
                viewModel.onEvent(HomeEvent.ShowDeleteConfirmDialog(transaction))
            },
            onEditTransaction = { transaction ->
                showActionsDialog = false
                editTransactionViewModel.loadTransactionForEdit(transaction)
                if (transaction.id.isNotBlank()) {
                    onNavigateToEdit(transaction.id)
                } else {
                    Timber.e(emptyTransactionIdErrorMsg)
                }
            },
            transactionToDelete = state.transactionToDelete,
            onConfirmDelete = {
                state.transactionToDelete?.let { transaction ->
                    viewModel.onEvent(HomeEvent.DeleteTransaction(transaction))
                    feedbackMessage = transactionDeletedMsg
                    feedbackType = FeedbackType.SUCCESS
                    showFeedback = true
                }
            },
            onDismissDelete = {
                viewModel.onEvent(HomeEvent.HideDeleteConfirmDialog)
            },
            feedbackMessage = feedbackMessage,
            feedbackType = feedbackType,
            showFeedback = showFeedback,
            onDismissFeedback = { showFeedback = false },
            isLoading = state.isLoading
        )
    }
}

@Composable
private fun HomeScreenContent(
    paddingValues: PaddingValues,
    windowSizeIsCompact: Boolean,
    state: HomeState,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
    showActionsDialog: Boolean,
    selectedTransaction: Transaction?,
    onDismissActionsDialog: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    transactionToDelete: Transaction?,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    feedbackMessage: String,
    feedbackType: FeedbackType,
    showFeedback: Boolean,
    onDismissFeedback: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        HomeMainContent(
            windowSizeIsCompact = windowSizeIsCompact,
            state = state,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick
        )
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CenteredLoadingIndicator(
                message = stringResource(R.string.loading_data),
                modifier = Modifier
            )
        }
        HomeDialogs(
            showActionsDialog = showActionsDialog,
            selectedTransaction = selectedTransaction,
            onDismissActionsDialog = onDismissActionsDialog,
            onDeleteTransaction = onDeleteTransaction,
            onEditTransaction = onEditTransaction,
            transactionToDelete = transactionToDelete,
            onConfirmDelete = onConfirmDelete,
            onDismissDelete = onDismissDelete
        )
        HomeFeedback(
            feedbackMessage = feedbackMessage,
            feedbackType = feedbackType,
            showFeedback = showFeedback,
            onDismiss = onDismissFeedback
        )
    }
}