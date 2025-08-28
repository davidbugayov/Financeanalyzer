package com.davidbugayov.financeanalyzer.presentation.chart.statistic
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidbugayov.financeanalyzer.feature.statistics.dialogs.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.EnhancedCategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.EnhancedLineChart
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.FinancialHealthMetricsCard
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.LineChartTypeSelector
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartDisplayMode
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartEffect
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.viewmodel.EnhancedFinanceChartViewModel
import com.davidbugayov.financeanalyzer.shared.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.shared.usecase.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.ui.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.ui.components.ErrorContent
import com.davidbugayov.financeanalyzer.ui.components.tips.EnhancedTipCard
import com.davidbugayov.financeanalyzer.ui.components.tips.FinancialTipsManager
import com.davidbugayov.financeanalyzer.ui.components.tips.InvestmentTipsCard
import com.davidbugayov.financeanalyzer.ui.components.tips.RecommendationsPanel
import com.davidbugayov.financeanalyzer.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Улучшенный экран с финансовыми графиками.
 * Поддерживает свайп между разными типами визуализации и обновленный дизайн.
 *
 * @param viewModel ViewModel для управления состоянием экрана
 * @param onNavigateBack Колбэк для навигации назад
 * @param onNavigateToTransactions Опциональный колбэк для навигации к списку транзакций с фильтрами
 */
@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)
@Composable
fun FinancialStatisticsScreen(
    onNavigateBack: () -> Unit,
    periodType: PeriodType? = null,
    startDate: Date? = null,
    endDate: Date? = null,
    onAddTransaction: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToTransactions: ((String, Date, Date) -> Unit)? = null,
) {
    // Используем новую ViewModel
    val viewModel: EnhancedFinanceChartViewModel = koinViewModel()
    val koinComponent = remember { object : KoinComponent {} }
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var shouldScrollToSummaryCard by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 3 }, initialPage = 0)
    var lineChartDisplayMode by remember { mutableStateOf(LineChartDisplayMode.BOTH) }

    // Кнопка-лампочка раскрывает/скрывает рекомендации
    var showRecommendations by remember { mutableStateOf(false) }

    // Получаем персонализированные советы на основе данных пользователя
    val personalizedTips =
        remember(state.income, state.expense, state.savingsRate, state.expenseLineChartData) {
            // Рассчитываем рост расходов на основе данных линейного графика
            val expenseGrowth = calculateExpenseGrowth(state.expenseLineChartData)

            val totalIncome = state.income?.toMajorDouble() ?: 0.0
            val totalExpense = state.expense?.toMajorDouble() ?: 0.0

            FinancialTipsManager.getPersonalizedTips(
                savingsRate = state.savingsRate,
                monthsOfSavings = state.monthsOfSavings,
                hasRegularIncome = totalIncome > 0,
                expenseGrowth = expenseGrowth,
            )
        }

    // Логируем открытие экрана и загружаем данные с учетом выбранного периода
    LaunchedEffect(Unit) {
        // Триггер ачивки - просмотр статистики
        AchievementTrigger.onStatisticsViewed()

        // Если передан период с главного экрана, используем его
        if (periodType != null && startDate != null && endDate != null) {
            viewModel.handleIntent(
                EnhancedFinanceChartIntent.ChangePeriod(
                    periodType = periodType,
                    startDate = startDate,
                    endDate = endDate,
                ),
            )
        } else {
            viewModel.handleIntent(EnhancedFinanceChartIntent.LoadData)
        }
    }
    // Отложенный запрос оценки через 2 сек, отменяется при уходе со страницы (в других флейворах no-op)
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        val activity = (ctx as? android.app.Activity)
        if (activity != null) {
            delay(2000)
            if (!activity.isFinishing) {
                try {
                    com.davidbugayov.financeanalyzer.utils.RuStoreUtils
                        .requestReview(activity)
                    com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils.logEvent(
                        com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsConstants.Events.USER_RATING,
                        android.os.Bundle().apply {
                            putString(
                                com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsConstants.Params.SOURCE,
                                "rustore",
                            )
                            putString("request_location", "statistics_screen")
                        },
                    )
                } catch (e: Exception) {
                    timber.log.Timber.e(e, "Ошибка при запросе оценки на статистике")
                }
            }
        }
    }

    // Следим за изменениями периода
    LaunchedEffect(state.periodType, state.startDate, state.endDate) {
        // Можно добавить логику, если нужно
    }

    // Подписка на эффекты (side effects)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EnhancedFinanceChartEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is EnhancedFinanceChartEffect.ScrollToSummary -> {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(0)
                    }
                }
                is EnhancedFinanceChartEffect.NavigateToAddTransaction -> {
                    onAddTransaction()
                }
            }
        }
    }

    val backgroundGradient =
        Brush.verticalGradient(
            colors =
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant,
                ),
        )

    // Форматтер дат
    val dateFormat = remember { SimpleDateFormat("dd MMMM", Locale.forLanguageTag("ru-RU")) }

    // Локальное управление выбором периода (UI/UX как в подробной статистике)
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedPeriodType by remember { mutableStateOf(state.periodType ?: PeriodType.MONTH) }
    var currentStartDate by remember { mutableStateOf(state.startDate) }
    var currentEndDate by remember { mutableStateOf(state.endDate) }

    LaunchedEffect(state.periodType, state.startDate, state.endDate) {
        selectedPeriodType = state.periodType
        currentStartDate = state.startDate
        currentEndDate = state.endDate
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(UiR.string.analytics_title),
                showBackButton = true,
                onBackClick = {
                    onNavigateBack()
                },
                actions = {
                    IconButton(onClick = { showRecommendations = !showRecommendations }) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = stringResource(UiR.string.show_tip),
                            tint = if (showRecommendations) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(backgroundGradient),
        ) {
            if (state.isLoading) {
                CenteredLoadingIndicator(
                    message = stringResource(UiR.string.loading_data_default),
                )
            } else if (state.error != null) {
                ErrorContent(
                    error =
                        state.error
                            ?: stringResource(UiR.string.unknown_error_message),
                    onRetry = { viewModel.handleIntent(EnhancedFinanceChartIntent.LoadData) },
                )
            } else {
                // Основной контент с графиками и фильтрами всегда отображается
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                ) {
                    // Верхняя карточка периода — как в подробной статистике
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensionResource(UiR.dimen.finance_chart_screen_padding),
                                    vertical = 12.dp,
                                ).clickable { showPeriodDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(UiR.string.financial_statistics_period),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text = state.periodText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    // Диалоги выбора периода и дат
                    if (showPeriodDialog) {
                        PeriodSelectionDialog(
                            selectedPeriod = selectedPeriodType,
                            startDate = currentStartDate,
                            endDate = currentEndDate,
                            onPeriodSelected = { p ->
                                selectedPeriodType = p
                                if (p != PeriodType.CUSTOM) {
                                    val (newStart, newEnd) =
                                        DateUtils.updatePeriodDates(
                                            periodType = p,
                                            currentStartDate = currentStartDate,
                                            currentEndDate = currentEndDate,
                                        )
                                    currentStartDate = newStart
                                    currentEndDate = newEnd
                                    viewModel.handleIntent(
                                        EnhancedFinanceChartIntent.ChangePeriod(p, newStart, newEnd),
                                    )
                                    showPeriodDialog = false
                                }
                            },
                            onStartDateClick = { showStartDatePicker = true },
                            onEndDateClick = { showEndDatePicker = true },
                            onConfirm = {
                                showPeriodDialog = false
                                viewModel.handleIntent(
                                    EnhancedFinanceChartIntent.ChangePeriod(
                                        PeriodType.CUSTOM,
                                        currentStartDate,
                                        currentEndDate,
                                    ),
                                )
                            },
                            onDismiss = { showPeriodDialog = false },
                        )
                    }

                    if (showStartDatePicker) {
                        DatePickerDialog(
                            initialDate = currentStartDate,
                            maxDate = minOf(currentEndDate, Calendar.getInstance().time),
                            onDateSelected = { d ->
                                currentStartDate = d
                                showStartDatePicker = false
                            },
                            onDismiss = { showStartDatePicker = false },
                        )
                    }

                    if (showEndDatePicker) {
                        DatePickerDialog(
                            initialDate = currentEndDate,
                            minDate = currentStartDate,
                            maxDate = Calendar.getInstance().time,
                            onDateSelected = { d ->
                                currentEndDate = d
                                showEndDatePicker = false
                            },
                            onDismiss = { showEndDatePicker = false },
                        )
                    }

                    if (personalizedTips.isNotEmpty()) {
                        androidx.compose.animation.AnimatedVisibility(visible = showRecommendations) {
                            Column {
                                RecommendationsPanel(
                                    tips = personalizedTips,
                                    onActionClick = { tip ->
                                        when (tip.actionResId) {
                                            UiR.string.action_add_transaction -> onAddTransaction()
                                            UiR.string.action_view_categories ->
                                                onNavigateToTransactions?.invoke(
                                                    "",
                                                    state.startDate,
                                                    state.endDate,
                                                )
                                            UiR.string.action_start_saving, UiR.string.action_review_budget, UiR.string.action_continue_saving, UiR.string.action_create_plan -> onNavigateToBudget()
                                            UiR.string.action_study_statistics -> {
                                                coroutineScope.launch { pagerState.scrollToPage(1) }
                                            }
                                            UiR.string.action_analyze_spending -> {
                                                coroutineScope.launch { pagerState.scrollToPage(0) }
                                                viewModel.handleIntent(
                                                    EnhancedFinanceChartIntent.ToggleExpenseView(true),
                                                )
                                            }
                                        }
                                    },
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                    // --- Динамический типс: всегда разные, можно показать по кнопке ---
                    // Если нет транзакций, показываем кнопку "Добавить транзакцию" над графиками
                    if (state.transactions.isEmpty()) {
                        Card(
                            onClick = {
                                onAddTransaction()
                            },
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal =
                                            dimensionResource(
                                                UiR.dimen.finance_chart_screen_padding,
                                            ),
                                        vertical = 16.dp,
                                    ),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(UiR.string.add_first_transaction),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = stringResource(UiR.string.see_analytics_magic),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.alpha(0.8f),
                                    )
                                }
                            }
                        }
                    }
                    // Далее — все фильтры, табы, графики и т.д. (основной UI)
                    // Убрали карточку на экране статистики по запросу — баланс теперь только на Home

                    // KPI финансового здоровья
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal =
                                        dimensionResource(
                                            UiR.dimen.finance_chart_screen_padding,
                                        ),
                                    vertical =
                                        dimensionResource(
                                            UiR.dimen.finance_chart_screen_vertical_spacing,
                                        ),
                                ),
                    ) {
                        FinancialHealthMetricsCard(
                            savingsRate = state.savingsRate,
                            averageDailyExpense = state.averageDailyExpense,
                            monthsOfSavings = state.monthsOfSavings,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Табы для переключения между типами графиков
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                            },
                            text = { Text(stringResource(UiR.string.categories)) },
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            text = { Text(stringResource(UiR.string.dynamics)) },
                        )
                        Tab(
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            text = { Text(stringResource(UiR.string.analysis)) },
                        )
                    }

                    // Горизонтальный пейджер для свайпа между графиками
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Пирографик по категориям
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                dimensionResource(
                                                    UiR.dimen.finance_chart_screen_padding,
                                                ),
                                            ),
                                ) {
                                    // Получаем данные категорий в зависимости от выбранного режима
                                    val categoryData =
                                        if (state.showExpenses) {
                                            state.expensesByCategory
                                        } else {
                                            state.incomeByCategory
                                        }

                                    // Проверяем, нет ли данных для отображения
                                    if (categoryData.isEmpty()) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(
                                                        dimensionResource(
                                                            UiR.dimen.finance_chart_screen_piechart_height,
                                                        ),
                                                    ).padding(
                                                        top =
                                                            dimensionResource(
                                                                UiR.dimen.finance_chart_screen_padding,
                                                            ),
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text =
                                                    stringResource(
                                                        UiR.string.no_data,
                                                    ),
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                        }
                                    } else {
                                        // Улучшенный пирограф категорий (данные готовит ViewModel)
                                        EnhancedCategoryPieChart(
                                            items = state.pieChartData,
                                            selectedIndex = null,
                                            onSectorClick = { _ ->
                                                // отключаем переход в историю по клику по сектору
                                            },
                                            modifier =
                                                Modifier.padding(
                                                    top =
                                                        dimensionResource(
                                                            UiR.dimen.finance_chart_screen_padding,
                                                        ),
                                                ),
                                            showExpenses = state.showExpenses,
                                            onShowExpensesChange = { showExpenses ->
                                                viewModel.handleIntent(
                                                    EnhancedFinanceChartIntent.ToggleExpenseView(
                                                        showExpenses,
                                                    ),
                                                )
                                            },
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // Линейный график динамики
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                dimensionResource(
                                                    UiR.dimen.finance_chart_screen_padding,
                                                ),
                                            ),
                                ) {
                                    // Линейный график
                                    LineChartTypeSelector(
                                        selectedMode = lineChartDisplayMode,
                                        onModeSelected = { lineChartDisplayMode = it },
                                    )

                                    val periodText = "${dateFormat.format(state.startDate)} – ${
                                        dateFormat.format(
                                            state.endDate,
                                        )
                                    }"

                                    EnhancedLineChart(
                                        incomeData = state.incomeLineChartData,
                                        expenseData = state.expenseLineChartData,
                                        showIncome = lineChartDisplayMode.showIncome,
                                        showExpense = lineChartDisplayMode.showExpense,
                                        title = stringResource(UiR.string.dynamics),
                                        period = periodText,
                                    )
                                }
                            }

                            2 -> {
                                // Аналитика и инсайты
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                top = 24.dp,
                                                bottom = 4.dp,
                                                start =
                                                    dimensionResource(
                                                        UiR.dimen.finance_chart_screen_padding,
                                                    ),
                                                end =
                                                    dimensionResource(
                                                        UiR.dimen.finance_chart_screen_padding,
                                                    ),
                                            ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Карточка "Полная статистика"
                                    Card(
                                        onClick = {
                                            // Переход на экран подробной статистики
                                            viewModel.navigateToDetailedStatistics()
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    bottom =
                                                        dimensionResource(
                                                            UiR.dimen.finance_chart_screen_card_bottom_padding,
                                                        ),
                                                ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation =
                                            CardDefaults.cardElevation(
                                                defaultElevation = 6.dp,
                                            ),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            ),
                                        border =
                                            BorderStroke(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            ),
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        dimensionResource(
                                                            UiR.dimen.finance_chart_screen_card_content_padding,
                                                        ),
                                                    ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Column {
                                                Text(
                                                    text =
                                                        stringResource(
                                                            UiR.string.detailed_financial_statistics,
                                                        ),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                )

                                                Text(
                                                    text = stringResource(UiR.string.study_financial_indicators),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color =
                                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                            alpha = 0.8f,
                                                        ),
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Filled.Analytics,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp),
                                            )
                                        }
                                    }

                                    // Оставляем остальной контент этой вкладки
                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                dimensionResource(
                                                    UiR.dimen.finance_chart_screen_vertical_spacing,
                                                ),
                                            ),
                                    )

                                    // Удаляем отдельные блоки советов, оставляем один
                                    // Дополнительные персонализированные советы (если есть)
                                    if (personalizedTips.size > 1 && showRecommendations) {
                                        EnhancedTipCard(
                                            tips = personalizedTips.drop(1).take(2),
                                            onDismiss = {},
                                            onActionClick = { tip ->
                                                when (tip.actionResId) {
                                                    UiR.string.action_add_transaction -> onAddTransaction()
                                                    UiR.string.action_start_saving, UiR.string.action_continue_saving -> onNavigateToBudget()
                                                    UiR.string.action_study_statistics ->
                                                        coroutineScope.launch {
                                                            pagerState.scrollToPage(
                                                                1,
                                                            )
                                                        }
                                                    UiR.string.action_analyze_spending ->
                                                        coroutineScope.launch {
                                                            pagerState.scrollToPage(
                                                                0,
                                                            )
                                                        }
                                                    UiR.string.action_view_categories ->
                                                        onNavigateToTransactions?.invoke(
                                                            "",
                                                            state.startDate,
                                                            state.endDate,
                                                        )
                                                }
                                            },
                                        )
                                    }

                                    // Оставляем предсказания
                                    val predictExpensesUseCase = koinComponent.get<PredictFutureExpensesUseCase>()
                                    val predictedExpenses =
                                        remember(
                                            state.transactions,
                                        ) { predictExpensesUseCase(state.transactions) }

                                    // В UI, добавляем карточку предсказаний (тональная)
                                    Card(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.25f,
                                                    ),
                                            ),
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Analytics,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = stringResource(id = UiR.string.prediction_title),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    )
                                                    Text(
                                                        text = stringResource(id = UiR.string.prediction_subtitle),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                                alpha = 0.8f,
                                                            ),
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(6.dp))

                                            // Форматируем сумму с валютой
                                            // Форматируем сумму через Money.formatForDisplay (единый формат приложения)
                                            val amountText = predictedExpenses.formatForDisplay(showCurrency = true, useMinimalDecimals = true)

                                            Text(
                                                text = stringResource(id = UiR.string.prediction_next_month, amountText),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                        }
                                    }

                                    // Инвестиционные советы в виде структурированной карточки
                                    InvestmentTipsCard(
                                        tipsRes =
                                            listOf(
                                                UiR.string.investment_tip_bonds,
                                                UiR.string.investment_tip_diversification,
                                                UiR.string.investment_tip_stocks,
                                            ),
                                    )
                                }
                            }
                        }
                    }

                    // (удалено) второй экземпляр KPI

                    // Если нужно прокручивать к карточке
                    if (shouldScrollToSummaryCard) {
                        LaunchedEffect(true) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                                shouldScrollToSummaryCard = false
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Рассчитывает рост расходов на основе данных линейного графика
 *
 * @param expenseData Данные расходов по периодам
 * @return Коэффициент роста расходов (-1 до 1, где 0.1 = 10% рост, -0.1 = 10% снижение)
 */
private fun calculateExpenseGrowth(
    expenseData: List<com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint>,
): Double {
    if (expenseData.size < 2) return 0.0

    // Берем последние точки для анализа тренда
    val recentPoints = expenseData.takeLast(minOf(7, expenseData.size)) // Последние 7 точек или все доступные

    if (recentPoints.size < 2) return 0.0

    // Рассчитываем простой линейный тренд
    val firstValue =
        recentPoints
            .first()
            .value.toMajorDouble()
    val lastValue =
        recentPoints
            .last()
            .value.toMajorDouble()

    // Избегаем деления на ноль
    if (firstValue <= 0.0) return 0.0

    // Рассчитываем относительное изменение
    val growth = (lastValue - firstValue) / firstValue

    // Ограничиваем значение в разумных пределах (-1 до 1)
    return growth.coerceIn(-1.0, 1.0)
}
