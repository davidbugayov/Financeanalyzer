package com.davidbugayov.financeanalyzer.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.DeleteTransactionDialog
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackMessage
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackType
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.home.components.CompactLayout
import com.davidbugayov.financeanalyzer.presentation.home.components.ExpandedLayout
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToChart: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val windowSize = rememberWindowSize()

    // Логируем открытие главного экрана
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

    // Загружаем сохраненное состояние видимости GroupSummary
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)
    var showGroupSummary by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean("show_group_summary", true))
    }

    // Загружаем транзакции при первом запуске
    LaunchedEffect(key1 = Unit) {
        viewModel.onEvent(HomeEvent.LoadTransactions)
        // Инициализируем состояние showGroupSummary в ViewModel из SharedPreferences
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(showGroupSummary))
    }

    // Сохраняем настройку при изменении
    LaunchedEffect(showGroupSummary) {
        sharedPreferences.edit {
            putBoolean("show_group_summary", showGroupSummary)
        }
        // Обновляем состояние в ViewModel
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(showGroupSummary))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.app_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
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
                modifier = Modifier.height(56.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Применяем все отступы, включая верхний
                .padding(paddingValues)
        ) {
            // Адаптивный макет в зависимости от размера экрана
            if (windowSize.isCompact()) {
                // Компактный макет для телефонов
                CompactLayout(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onShowGroupSummaryChange = { showGroupSummary = it },
                    onFilterSelected = { viewModel.onEvent(HomeEvent.SetFilter(it)) },
                    onNavigateToHistory = onNavigateToHistory,
                    onTransactionClick = { /* Пока ничего не делаем */ },
                    onTransactionLongClick = { transaction ->
                        viewModel.onEvent(HomeEvent.ShowDeleteConfirmDialog(transaction))
                    }
                )
            } else {
                // Расширенный макет для планшетов
                ExpandedLayout(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onShowGroupSummaryChange = { showGroupSummary = it },
                    onFilterSelected = { viewModel.onEvent(HomeEvent.SetFilter(it)) },
                    onNavigateToHistory = onNavigateToHistory,
                    onTransactionClick = { /* Пока ничего не делаем */ },
                    onTransactionLongClick = { transaction ->
                        viewModel.onEvent(HomeEvent.ShowDeleteConfirmDialog(transaction))
                    }
                )
            }

            // Кнопки навигации поверх контента с анимацией
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 0.dp,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = CenterVertically
                    ) {
                        // Кнопка Графики
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    onNavigateToChart()
                                    feedbackMessage = "Переход к графикам"
                                    feedbackType = FeedbackType.INFO
                                    showFeedback = true
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = stringResource(R.string.charts)
                                )
                            }
                            Text(
                                text = stringResource(R.string.charts),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Кнопка Добавить
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledIconButton(
                                onClick = {
                                    onNavigateToAdd()
                                    feedbackMessage = "Добавление новой транзакции"
                                    feedbackType = FeedbackType.INFO
                                    showFeedback = true
                                },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_button)
                                )
                            }
                            Text(
                                text = stringResource(R.string.add_button),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Кнопка История
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    onNavigateToHistory()
                                    feedbackMessage = "Переход к истории транзакций"
                                    feedbackType = FeedbackType.INFO
                                    showFeedback = true
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = stringResource(R.string.history)
                                )
                            }
                            Text(
                                text = stringResource(R.string.history),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

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