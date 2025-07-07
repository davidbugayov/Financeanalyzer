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
import com.davidbugayov.financeanalyzer.feature.home.BuildConfig
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.ErrorTracker
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.presentation.categories.AppCategoriesViewModel
import com.davidbugayov.financeanalyzer.ui.components.AnimatedBottomNavigationBar
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.ui.components.FeedbackMessage
import com.davidbugayov.financeanalyzer.ui.components.FeedbackType
import com.davidbugayov.financeanalyzer.ui.components.TransactionActionsDialog
import com.davidbugayov.financeanalyzer.presentation.home.components.CompactLayout
import com.davidbugayov.financeanalyzer.presentation.home.components.ExpandedLayout
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.feature.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber
import com.davidbugayov.financeanalyzer.ui.components.DeleteTransactionDialog
import android.os.SystemClock
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem

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
                    onClick = {
                        // Отслеживаем действие пользователя
                        UserEventTracker.trackUserAction(
                            "generate_test_data",
                            mapOf(
                                "source" to "home_screen",
                            ),
                        )
                        onGenerateTestData()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.generate_test_data),
                    )
                }
            }
            IconButton(
                onClick = {
                    // Отслеживаем действие пользователя
                    UserEventTracker.trackUserAction(
                        "navigate_to_profile",
                        mapOf(
                            "source" to "home_screen",
                        ),
                    )
                    onNavigateToProfile()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile),
                )
            }
        },
    )
}

@Composable
private fun HomeBottomBar(onNavigateToChart: () -> Unit, onNavigateToHistory: () -> Unit, onNavigateToAdd: () -> Unit) {
    AnimatedBottomNavigationBar(
        visible = true,
        onChartClick = {
            // Отслеживаем действие пользователя
            UserEventTracker.trackUserAction(
                "navigate_to_chart",
                mapOf(
                    "source" to "home_screen",
                ),
            )
            // Триггер ачивки - посещение раздела
            Timber.d("🏆 Триггер ачивки: Пользователь посетил раздел 'Статистика'")
            onNavigateToChart()
        },
        onHistoryClick = {
            // Отслеживаем действие пользователя
            UserEventTracker.trackUserAction(
                "navigate_to_history",
                mapOf(
                    "source" to "home_screen",
                ),
            )
            // Триггер ачивки - посещение раздела
            Timber.d("🏆 Триггер ачивки: Пользователь посетил раздел 'История'")
            onNavigateToHistory()
        },
        onAddClick = {
            // Отслеживаем действие пользователя
            UserEventTracker.trackUserAction(
                "navigate_to_add",
                mapOf(
                    "source" to "home_screen",
                ),
            )
            // Отслеживаем использование функции
            UserEventTracker.trackFeatureUsage("add_transaction")
            // Триггер ачивки - добавление транзакции
            Timber.d("🏆 Триггер ачивки: Пользователь добавляет транзакцию")
            onNavigateToAdd()
        },
    )
}

@Composable
private fun HomeMainContent(
    windowSizeIsCompact: Boolean,
    state: HomeState,
    categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.AppCategoriesViewModel,
    pagingItems: androidx.paging.compose.LazyPagingItems<TransactionListItem>,
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
            pagingItems = pagingItems,
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
            pagingItems = pagingItems,
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
    categoriesViewModel: com.davidbugayov.financeanalyzer.presentation.categories.AppCategoriesViewModel =
        koinViewModel(),
    editViewModel: EditTransactionViewModel = koinViewModel(),
    updateWidgetsUseCase: UpdateWidgetsUseCase = koinInject(),
    userEventTracker: UserEventTracker = koinInject(),
    errorTracker: ErrorTracker = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val windowSize = rememberWindowSize()
    val pagingItems = viewModel.pagedUiModels.collectAsLazyPagingItems()

    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(FeedbackType.INFO) }
    var selectedTransactionForActions by remember { mutableStateOf<Transaction?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)

    // Отслеживаем время загрузки экрана
    val screenLoadStartTime = remember { SystemClock.elapsedRealtime() }

    val testDataGeneratedMsg = stringResource(R.string.test_data_generated)
    val transactionDeletedMsg = stringResource(R.string.transaction_deleted)
    val emptyTransactionIdErrorMsg = stringResource(R.string.empty_transaction_id_error)

    // Отслеживаем открытие экрана
    LaunchedEffect(Unit) {
        // Отмечаем начало загрузки экрана
        PerformanceMetrics.startScreenLoadTiming(PerformanceMetrics.Screens.HOME)

        // Логируем просмотр экрана
        AnalyticsUtils.logScreenView(
            screenName = "home",
            screenClass = "HomeScreen",
        )

        // Отслеживаем открытие экрана для аналитики пользовательских событий
        userEventTracker.trackScreenOpen(PerformanceMetrics.Screens.HOME)

        // Логируем использование функции
        userEventTracker.trackFeatureUsage("home_view")

        try {
            // Загружаем данные для экрана
            viewModel.onEvent(HomeEvent.LoadTransactions)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке данных для главного экрана")

            // Отслеживаем ошибку
            errorTracker.trackException(
                e,
                isFatal = false,
                mapOf(
                    "location" to "home_screen",
                    "action" to "load_data",
                ),
            )
        }

        // Завершаем отслеживание загрузки экрана
        PerformanceMetrics.endScreenLoadTiming(PerformanceMetrics.Screens.HOME)

        // Дополнительно отслеживаем время загрузки экрана
        val loadTime = SystemClock.elapsedRealtime() - screenLoadStartTime
        AnalyticsUtils.logEvent(
            AnalyticsConstants.Events.SCREEN_LOAD,
            android.os.Bundle().apply {
                putString(AnalyticsConstants.Params.SCREEN_NAME, "home")
                putLong(AnalyticsConstants.Params.DURATION_MS, loadTime)
            },
        )
    }

    // Отслеживаем закрытие экрана
    DisposableEffect(Unit) {
        onDispose {
            // Отслеживаем закрытие экрана
            userEventTracker.trackScreenClose(PerformanceMetrics.Screens.HOME)
        }
    }

    LaunchedEffect(Unit) {
        // Load persisted group summary toggle
        val savedShowSummary = sharedPreferences.getBoolean("show_group_summary", false)
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(savedShowSummary))

        // Load persisted transaction filter (if any) so it survives navigation away and back
        sharedPreferences.getString("current_filter", null)?.let { savedFilterName ->
            kotlin.runCatching { TransactionFilter.valueOf(savedFilterName) }.getOrNull()?.let { savedFilter ->
                viewModel.onEvent(HomeEvent.SetFilter(savedFilter))
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var lastRefreshTime = 0L
        val observer = LifecycleEventObserver { _, event ->
            Timber.d("HOME: Событие жизненного цикла: $event")
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentTime = System.currentTimeMillis()
                Timber.d("HOME: ON_RESUME - время с последнего обновления: ${currentTime - lastRefreshTime}ms")
                if (currentTime - lastRefreshTime > 2000) {
                    Timber.d("HOME: ON_RESUME - проверяем необходимость обновления данных")
                    // Вместо полной перезагрузки просто проверяем, нужно ли обновить данные
                    // Плавное обновление уже происходит через subscribeToRepositoryChanges
                    lastRefreshTime = currentTime
                } else {
                    Timber.d("HOME: ON_RESUME - пропускаем обновление (прошло менее 2 секунд)")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val onTransactionClick = { transaction: Transaction ->
        userEventTracker.trackUserAction(
            "transaction_click",
            mapOf(
                "transaction_id" to transaction.id,
                "transaction_amount" to transaction.amount.amount.toString(),
            ),
        )
        viewModel.onEvent(HomeEvent.EditTransaction(transaction))
    }
    val onTransactionLongClick = { transaction: Transaction ->
        userEventTracker.trackUserAction(
            "transaction_long_click",
            mapOf(
                "transaction_id" to transaction.id,
            ),
        )
        selectedTransactionForActions = transaction
        showActionsDialog = true
    }
    val onToggleGroupSummary = remember<(Boolean) -> Unit> {
        { newValue ->
            viewModel.onEvent(HomeEvent.SetShowGroupSummary(newValue))
            sharedPreferences.edit { putBoolean("show_group_summary", newValue) }
        }
    }
    val onFilterSelected = { filter: TransactionFilter ->
        userEventTracker.trackUserAction(
            "filter_selected",
            mapOf(
                "filter" to filter.toString(),
            ),
        )
        userEventTracker.trackFeatureUsage("transaction_filter")
        // Persist selected filter so it can be restored later
        sharedPreferences.edit { putString("current_filter", filter.name) }
        viewModel.onEvent(HomeEvent.SetFilter(filter))
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
                    pagingItems = pagingItems,
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
            userEventTracker.trackUserAction(
                "delete_transaction",
                mapOf(
                    "transaction_id" to transaction.id,
                    "transaction_amount" to transaction.amount.amount.toString(),
                ),
            )
            // Показываем диалог подтверждения вместо прямого удаления
            viewModel.onEvent(HomeEvent.ShowDeleteConfirmDialog(transaction))
            showActionsDialog = false
            selectedTransactionForActions = null
        },
        onEditTransaction = { transaction ->
            userEventTracker.trackUserAction(
                "edit_transaction",
                mapOf(
                    "transaction_id" to transaction.id,
                ),
            )
            userEventTracker.trackFeatureUsage("edit_transaction")
            viewModel.onEvent(HomeEvent.EditTransaction(transaction))
            showActionsDialog = false
            selectedTransactionForActions = null
        },
        transactionToDelete = state.transactionToDelete,
        onConfirmDelete = {
            state.transactionToDelete?.let { transactionToDelete ->
                userEventTracker.trackUserAction(
                    "delete_transaction",
                    mapOf(
                        "transaction_id" to transactionToDelete.id,
                        "transaction_amount" to transactionToDelete.amount.amount.toString(),
                    ),
                )
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
