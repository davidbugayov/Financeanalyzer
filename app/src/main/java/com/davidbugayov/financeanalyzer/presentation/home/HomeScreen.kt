package com.davidbugayov.financeanalyzer.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
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
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize
import timber.log.Timber

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    editTransactionViewModel: EditTransactionViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToChart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToWallets: () -> Unit = onNavigateToBudget,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val windowSize = rememberWindowSize()

    // Состояние для обратной связи
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(FeedbackType.INFO) }

    // Состояние для диалога действий с транзакцией
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }

    // Загружаем сохраненное состояние видимости GroupSummary
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)

    // Логируем открытие главного экрана и загружаем данные только при первом входе
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "home",
            screenClass = "HomeScreen"
        )
    }
    
    // Инициализируем состояние из SharedPreferences при первом запуске
    LaunchedEffect(Unit) {
        val savedShowSummary = sharedPreferences.getBoolean("show_group_summary", false)
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(savedShowSummary))
    }

    // Обновляем транзакции при возвращении на экран, но с управлением частоты
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        Timber.d("HomeScreen: регистрируем обновление при навигации")
        
        // Время последнего обновления
        var lastRefreshTime = 0L
        
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Проверяем, прошло ли достаточно времени с последнего обновления
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRefreshTime > 2000) { // Не обновляем чаще чем раз в 2 секунды
                    Timber.d("HomeScreen: Запрашиваем обновление данных после навигации (ON_RESUME)")
                    
                    // Запускаем стандартную загрузку данных. Дебаунсинг внутри ViewModel предотвратит лишние вызовы.
                    viewModel.onEvent(HomeEvent.LoadTransactions)
                    
                    lastRefreshTime = currentTime
                } else {
                    Timber.d("HomeScreen: пропускаем обновление - прошло менее 2 секунд с последнего")
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Оптимизируем обработчики событий TransactionClick и TransactionLongClick, чтобы они не пересоздавались при каждой рекомпозиции
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

    // Оптимизируем обработчик изменения showGroupSummary
    val onShowGroupSummaryChange = remember<(Boolean) -> Unit> {
        { newValue ->
            // Обновляем состояние только в ViewModel
            viewModel.onEvent(HomeEvent.SetShowGroupSummary(newValue))
            // Обновляем SharedPreferences
            sharedPreferences.edit {
                putBoolean("show_group_summary", newValue)
            }
        }
    }

    // Оптимизируем обработчик выбора фильтра
    val onFilterSelected = remember<(TransactionFilter) -> Unit> {
        { filter ->
            viewModel.onEvent(HomeEvent.SetFilter(filter))
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_title),
                navigationIcon = {
                    // Кнопка для генерации тестовых данных
                    if (BuildConfig.DEBUG) {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(HomeEvent.GenerateTestData)
                                feedbackMessage = "Тестовые данные сгенерированы"
                                feedbackType = FeedbackType.SUCCESS
                                showFeedback = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Сгенерировать тестовые данные"
                            )
                        }
                    }
                },
                actions = {
                    // Кнопка профиля
                    IconButton(
                        onClick = {
                            onNavigateToProfile()
                            feedbackMessage = "Переход к профилю"
                            feedbackType = FeedbackType.INFO
                            showFeedback = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile)
                        )
                    }
                },
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
            )
        },
        bottomBar = {
            // Используем анимированную нижнюю навигацию 
            AnimatedBottomNavigationBar(
                visible = true,
                onChartClick = {
                    onNavigateToChart()
                    feedbackMessage = "Переход к графикам"
                    feedbackType = FeedbackType.INFO
                    showFeedback = true
                },
                onHistoryClick = {
                    onNavigateToHistory()
                    feedbackMessage = "Переход к истории транзакций"
                    feedbackType = FeedbackType.INFO
                    showFeedback = true
                },
                onBudgetClick = {
                    onNavigateToWallets()
                    feedbackMessage = "Переход к кошелькам"
                    feedbackType = FeedbackType.INFO
                    showFeedback = true
                },
            )
        },
        floatingActionButton = {
            // Floating Action Button для добавления транзакции
            FloatingActionButton(
                onClick = {
                    // Просто навигация на экран добавления транзакции
                    onNavigateToAdd()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_button),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Применяем явно только внутренние отступы, полученные от Scaffold
                .padding(paddingValues)
        ) {
            // Адаптивный макет в зависимости от размера экрана
            if (windowSize.isCompact()) {
                // Компактный макет для телефонов
                CompactLayout(
                    state = state,
                    showGroupSummary = state.showGroupSummary,
                    onShowGroupSummaryChange = onShowGroupSummaryChange,
                    onFilterSelected = onFilterSelected,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick
                )
            } else {
                // Расширенный макет для планшетов
                ExpandedLayout(
                    state = state,
                    showGroupSummary = state.showGroupSummary,
                    onShowGroupSummaryChange = onShowGroupSummaryChange,
                    onFilterSelected = onFilterSelected,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick
                )
            }
            
            // Индикатор загрузки с анимацией
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CenteredLoadingIndicator(
                    message = stringResource(R.string.loading_data),
                    modifier = Modifier
                )
            }

            // Диалог действий с транзакцией (удаление/редактирование)
            if (showActionsDialog && selectedTransaction != null) {
                TransactionActionsDialog(
                    transaction = selectedTransaction!!,
                    onDismiss = { showActionsDialog = false },
                    onDelete = { transaction ->
                        showActionsDialog = false
                        viewModel.onEvent(HomeEvent.ShowDeleteConfirmDialog(transaction))
                    },
                    onEdit = { transaction ->
                        showActionsDialog = false
                        editTransactionViewModel.loadTransactionForEdit(transaction)
                        if (transaction.id.isNotBlank()) {
                            onNavigateToEdit(transaction.id)
                        } else {
                            Timber.e("Попытка навигации на экран редактирования с пустым transactionId!")
                            // Можно показать Snackbar или Toast
                        }
                    }
                )
            }

            // Диалог подтверждения удаления транзакции
            state.transactionToDelete?.let { transaction ->
                DeleteTransactionDialog(
                    transaction = transaction,
                    onConfirm = {
                        viewModel.onEvent(HomeEvent.DeleteTransaction(transaction))
                        feedbackMessage = "Транзакция удалена"
                        feedbackType = FeedbackType.SUCCESS
                        showFeedback = true
                    },
                    onDismiss = {
                        viewModel.onEvent(HomeEvent.HideDeleteConfirmDialog)
                    }
                )
            }

            // Отображение уведомлений обратной связи
            FeedbackMessage(
                message = feedbackMessage,
                type = feedbackType,
                visible = showFeedback,
                onDismiss = { showFeedback = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp) // Оставляем небольшой отступ сверху
            )
        }
    }
}