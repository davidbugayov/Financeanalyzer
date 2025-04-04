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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.dimensionResource
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
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    addTransactionViewModel: com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToChart: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val windowSize = rememberWindowSize()
    
    // Переменная для отслеживания первоначальной загрузки
    val initialLoadDone = remember { mutableStateOf(false) }

    // Логируем открытие главного экрана и загружаем данные только при первом входе
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "home",
            screenClass = "HomeScreen"
        )
    }

    // Состояние для обратной связи
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(FeedbackType.INFO) }

    // Состояние для диалога действий с транзакцией
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }

    // Загружаем сохраненное состояние видимости GroupSummary
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)
    var showGroupSummary by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean("show_group_summary", false))
    }

    // Обновляем состояние showGroupSummary в ViewModel при его изменении
    LaunchedEffect(showGroupSummary) {
        sharedPreferences.edit {
            putBoolean("show_group_summary", showGroupSummary)
        }
        // Обновляем состояние в ViewModel
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(showGroupSummary))
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
            showGroupSummary = newValue
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Применяем явно только верхний отступ, чтобы избежать двойных отступов
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Адаптивный макет в зависимости от размера экрана
            if (windowSize.isCompact()) {
                // Компактный макет для телефонов
                CompactLayout(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onShowGroupSummaryChange = onShowGroupSummaryChange,
                    onFilterSelected = onFilterSelected,
                    onNavigateToHistory = onNavigateToHistory,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick
                )
            } else {
                // Расширенный макет для планшетов
                ExpandedLayout(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onShowGroupSummaryChange = onShowGroupSummaryChange,
                    onFilterSelected = onFilterSelected,
                    onNavigateToHistory = onNavigateToHistory,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick
                )
            }

            // Используем анимированную нижнюю навигацию 
            AnimatedBottomNavigationBar(
                visible = true,
                onAddClick = {
                    onNavigateToAdd()
                    feedbackMessage = "Добавление новой транзакции"
                    feedbackType = FeedbackType.INFO
                    showFeedback = true
                },
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
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Индикатор загрузки с анимацией
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
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
                        // Загружаем транзакцию в ViewModel для редактирования
                        addTransactionViewModel.loadTransactionForEditing(transaction)
                        // Переходим на экран добавления/редактирования
                        onNavigateToAdd()
                        feedbackMessage = "Редактирование транзакции"
                        feedbackType = FeedbackType.INFO
                        showFeedback = true
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
                    .padding(top = 8.dp)
            )
        }
    }
}