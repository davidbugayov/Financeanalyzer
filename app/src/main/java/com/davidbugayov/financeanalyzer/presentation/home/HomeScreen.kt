package com.davidbugayov.financeanalyzer.presentation.home
import androidx.compose.foundation.layout.Box
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
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AnimatedBottomNavigationBar
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.CenteredLoadingIndicator
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
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber
import com.davidbugayov.financeanalyzer.presentation.components.FeatureAnnouncement

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
@Composable
private fun HomeTopBar(onGenerateTestData: () -> Unit, onNavigateToProfile: () -> Unit) {
    AppTopBar(
        title = stringResource(R.string.app_title),
        actions = {
            if (BuildConfig.DEBUG) {
                IconButton(
                    onClick = onGenerateTestData,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.generate_test_data),
                    )
                }
            }
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile),
                )
            }
        }
    )
}

@Composable
private fun HomeBottomBar(onNavigateToChart: () -> Unit, onNavigateToHistory: () -> Unit, onNavigateToAdd: () -> Unit) {
    AnimatedBottomNavigationBar(
        visible = true,
        onChartClick = onNavigateToChart,
        onHistoryClick = onNavigateToHistory,
        onAddClick = onNavigateToAdd,
    )
}

@Composable
private fun HomeMainContent(
    windowSizeIsCompact: Boolean,
    state: HomeState,
    categoriesViewModel: CategoriesViewModel,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
) {
    if (windowSizeIsCompact) {
        CompactLayout(
            state = state,
            categoriesViewModel = categoriesViewModel,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick,
        )
    } else {
        ExpandedLayout(
            state = state,
            categoriesViewModel = categoriesViewModel,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick,
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
    onDismissDelete: () -> Unit,
) {
    if (showActionsDialog && selectedTransaction != null) {
        TransactionActionsDialog(
            transaction = selectedTransaction,
            onDismiss = onDismissActionsDialog,
            onDelete = onDeleteTransaction,
            onEdit = onEditTransaction,
        )
    }
    transactionToDelete?.let { transaction ->
        DeleteTransactionDialog(
            transaction = transaction,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
}

@Composable
private fun HomeFeedback(
    title: String,
    feedbackMessage: String,
    feedbackType: FeedbackType,
    showFeedback: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeedbackMessage(
        title = title,
        message = feedbackMessage,
        type = feedbackType,
        visible = showFeedback,
        onDismiss = onDismiss,
        modifier = modifier
            .padding(top = dimensionResource(R.dimen.padding_small)),
    )
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    editViewModel: EditTransactionViewModel = koinViewModel(),
    updateWidgetsUseCase: UpdateWidgetsUseCase = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val windowSize = rememberWindowSize()

    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(FeedbackType.INFO) }
    var selectedTransactionForActions by remember { mutableStateOf<Transaction?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)

    val testDataGeneratedMsg = stringResource(R.string.test_data_generated)
    val transactionDeletedMsg = stringResource(R.string.transaction_deleted)
    val emptyTransactionIdErrorMsg = stringResource(R.string.empty_transaction_id_error)

    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "home",
            screenClass = "HomeScreen",
        )
        updateWidgetsUseCase(context)

        // Проверяем наличие обновлений при отображении главного экрана
        try {
            // Проверка обновлений в RuStore будет выполняться только если это rustore flavor
            // Для других flavor это будет заглушка, которая не вызывает ошибок
            com.davidbugayov.financeanalyzer.utils.RuStoreUtils.checkForUpdates(context)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при проверке обновлений")
        }
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
            selectedTransactionForActions = transaction
            showActionsDialog = true
        }
    }
    val onTransactionLongClick = remember<(Transaction) -> Unit> {
        { transaction ->
            selectedTransactionForActions = transaction
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
                onNavigateToProfile = { viewModel.onEvent(HomeEvent.NavigateToProfile) },
            )
        },
        bottomBar = {
            HomeBottomBar(
                onNavigateToChart = { viewModel.onEvent(HomeEvent.NavigateToChart) },
                onNavigateToHistory = { viewModel.onEvent(HomeEvent.NavigateToHistory) },
                onNavigateToAdd = { viewModel.onEvent(HomeEvent.NavigateToAddTransaction) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.isLoading && state.transactions.isEmpty()) {
                CenteredLoadingIndicator(message = stringResource(R.string.loading_data))
            } else {
                HomeMainContent(
                    windowSizeIsCompact = windowSize.isCompact(),
                    state = state,
                    categoriesViewModel = categoriesViewModel,
                    showGroupSummary = state.showGroupSummary,
                    onToggleGroupSummary = onToggleGroupSummary,
                    onFilterSelected = onFilterSelected,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick,
                    onAddClick = { viewModel.onEvent(HomeEvent.NavigateToAddTransaction) },
                )
            }
            HomeFeedback(
                title = when (feedbackType) {
                    FeedbackType.SUCCESS -> "Успех"
                    FeedbackType.ERROR -> "Ошибка"
                    FeedbackType.WARNING -> "Внимание"
                    FeedbackType.INFO -> "Уведомление"
                },
                feedbackMessage = feedbackMessage,
                feedbackType = feedbackType,
                showFeedback = showFeedback,
                onDismiss = { showFeedback = false },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

    HomeDialogs(
        showActionsDialog = showActionsDialog,
        selectedTransaction = selectedTransactionForActions,
        onDismissActionsDialog = {
            showActionsDialog = false
            selectedTransactionForActions = null
        },
        onDeleteTransaction = { transaction ->
            viewModel.onEvent(HomeEvent.ShowDeleteConfirmDialog(transaction))
            showActionsDialog = false
            selectedTransactionForActions = null
        },
        onEditTransaction = { transaction ->
            if (transaction.id.isBlank()) {
                Timber.e(emptyTransactionIdErrorMsg)
                feedbackMessage = emptyTransactionIdErrorMsg
                feedbackType = FeedbackType.ERROR
                showFeedback = true
            } else {
                editViewModel.loadTransactionForEditById(transaction.id)
                viewModel.onEvent(HomeEvent.EditTransaction(transaction))
            }
            showActionsDialog = false
            selectedTransactionForActions = null
        },
        transactionToDelete = state.transactionToDelete,
        onConfirmDelete = {
            state.transactionToDelete?.let { transactionToDelete ->
                viewModel.onEvent(HomeEvent.DeleteTransaction(transactionToDelete))
                feedbackMessage = transactionDeletedMsg
                feedbackType = FeedbackType.SUCCESS
                showFeedback = true
            }
            viewModel.onEvent(HomeEvent.HideDeleteConfirmDialog)
        },
        onDismissDelete = {
            viewModel.onEvent(HomeEvent.HideDeleteConfirmDialog)
        },
    )
}
