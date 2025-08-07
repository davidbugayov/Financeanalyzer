package com.davidbugayov.financeanalyzer.presentation.chart.statistic

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.feature.statistics.R
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.EnhancedCategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.EnhancedLineChart
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.EnhancedSummaryCard
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.FinancialHealthMetricsCard
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.components.LineChartTypeSelector
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartDisplayMode
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartEffect
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.statistic.viewmodel.EnhancedFinanceChartViewModel
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.ui.components.ErrorContent
import com.davidbugayov.financeanalyzer.ui.components.tips.EnhancedTipCard
import com.davidbugayov.financeanalyzer.ui.components.tips.FinancialTipsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
    periodType: com.davidbugayov.financeanalyzer.navigation.model.PeriodType? = null,
    startDate: Date? = null,
    endDate: Date? = null,
    onAddTransaction: () -> Unit,
    onNavigateToTransactions: ((String, Date, Date) -> Unit)? = null,
) {
    // Используем новую ViewModel
    val viewModel: EnhancedFinanceChartViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var shouldScrollToSummaryCard by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 3 }, initialPage = 0)
    var lineChartDisplayMode by remember { mutableStateOf(LineChartDisplayMode.BOTH) }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("finance_analyzer_prefs", Context.MODE_PRIVATE) }
    var showTip by remember { mutableStateOf(prefs.getBoolean("show_statistics_tip", true)) }

    // Показываем типс только один раз автоматически
    LaunchedEffect(Unit) {
        if (showTip) {
            prefs.edit { putBoolean("show_statistics_tip", false) }
        }
    }

    // Получаем персонализированные советы на основе данных пользователя
    val personalizedTips = remember(state.income, state.expense, state.savingsRate, state.expenseLineChartData) {
        // Рассчитываем рост расходов на основе данных линейного графика
        val expenseGrowth = calculateExpenseGrowth(state.expenseLineChartData)

        val totalIncome = state.income?.amount?.toDouble() ?: 0.0
        state.expense?.amount?.toDouble() ?: 0.0

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
                    // onNavigateToTransactions?.invoke("", state.startDate, state.endDate)
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Графики",
                showBackButton = true,
                onBackClick = {
                    onNavigateBack()
                },
                actions = {
                    if (!showTip) {
                        IconButton(
                            onClick = {
                                showTip = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = "Показать совет",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
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
                CenteredLoadingIndicator(message = "Загрузка данных...")
            } else if (state.error != null) {
                ErrorContent(
                    error = state.error ?: "Неизвестная ошибка",
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
                    if (showTip && personalizedTips.isNotEmpty()) {
                        // Показываем только лучшие 3 совета
                        EnhancedTipCard(
                            tips = personalizedTips.take(3),
                            onDismiss = {
                                showTip = false
                            },
                            onActionClick = { tip ->
                                // Обработка действий по советам
                                when (tip.actionResId) {
                                    com.davidbugayov.financeanalyzer.ui.R.string.action_add_transaction -> onAddTransaction()
                                    com.davidbugayov.financeanalyzer.ui.R.string.action_start_saving, com.davidbugayov.financeanalyzer.ui.R.string.action_continue_saving -> {
                                        // TODO: Навигация к разделу сбережений
                                    }
                                    com.davidbugayov.financeanalyzer.ui.R.string.action_study_statistics, com.davidbugayov.financeanalyzer.ui.R.string.action_analyze_spending -> {
                                        // Закрываем совет и фокусируемся на графиках
                                        showTip = false
                                    }
                                }
                            },
                        )
                    }
                    // --- Динамический типс: всегда разные, можно показать по кнопке ---
                    // Если нет транзакций, показываем кнопку "Добавить транзакцию" над графиками
                    if (state.transactions.isEmpty()) {
                        Card(
                            onClick = {
                                viewModel.handleIntent(
                                    EnhancedFinanceChartIntent.AddTransactionClicked,
                                )
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
                                                R.dimen.finance_chart_screen_padding,
                                            ),
                                        vertical = 16.dp,
                                    )
                                    .clickable {
                                        onAddTransaction()
                                    },
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
                                        text = "Добавьте первую транзакцию",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = "И увидите магию аналитики!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.alpha(0.8f),
                                    )
                                }
                            }
                        }
                    }
                    // Далее — все фильтры, табы, графики и т.д. (основной UI)
                    // Карточка с общим балансом и периодом
                    EnhancedSummaryCard(
                        income = state.income ?: Money.zero(),
                        expense = (state.expense ?: Money.zero()).abs(),
                        startDate = state.startDate,
                        endDate = state.endDate,
                        periodType = state.periodType,
                        viewModel = viewModel,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.finance_chart_screen_padding)),
                    )

                    // KPI финансового здоровья
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(R.dimen.finance_chart_screen_padding),
                                vertical = dimensionResource(R.dimen.finance_chart_screen_vertical_spacing),
                            ),
                    ) {
                        Text(
                            text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.insight_financial_health),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
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
                            text = { Text("Категории") },
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            text = { Text("Динамика") },
                        )
                        Tab(
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            text = { Text("Анализ") },
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
                                            .padding(dimensionResource(R.dimen.finance_chart_screen_padding)),
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
                                                            R.dimen.finance_chart_screen_piechart_height,
                                                        ),
                                                    )
                                                    .padding(
                                                        top =
                                                            dimensionResource(
                                                                R.dimen.finance_chart_screen_padding,
                                                            ),
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text =
                                                    "Нет данных",
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                        }
                                    } else {
                                        // Улучшенный пирограф категорий (данные готовит ViewModel)
                                        EnhancedCategoryPieChart(
                                            items = state.pieChartData,
                                            selectedIndex = null,
                                            onSectorClick = { item ->
                                                item?.original?.name?.let { categoryName ->
                                                    onNavigateToTransactions?.invoke(
                                                        categoryName,
                                                        state.startDate,
                                                        state.endDate,
                                                    )
                                                }
                                            },
                                            modifier =
                                                Modifier.padding(
                                                    top =
                                                        dimensionResource(
                                                            R.dimen.finance_chart_screen_padding,
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
                                            .padding(dimensionResource(R.dimen.finance_chart_screen_padding)),
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
                                        title = "Динамика",
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
                                                start = dimensionResource(R.dimen.finance_chart_screen_padding),
                                                end = dimensionResource(R.dimen.finance_chart_screen_padding),
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
                                                            R.dimen.finance_chart_screen_card_bottom_padding,
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
                                                            R.dimen.finance_chart_screen_card_content_padding,
                                                        ),
                                                    ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Column {
                                                Text(
                                                    text =
                                                        "Детальная финансовая статистика",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                )

                                                Text(
                                                    text =
                                                        "Изучите свои финансовые показатели",
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
                                                    R.dimen.finance_chart_screen_vertical_spacing,
                                                ),
                                            ),
                                    )

                                    // Удаляем отдельные блоки советов, оставляем один
                                    // Дополнительные персонализированные советы (если есть)
                                    if (personalizedTips.size > 1) {
                                        EnhancedTipCard(
                                            tips = personalizedTips.drop(1).take(2), // Только 2 доп.совета
                                            onDismiss = { /* Не показываем кнопку закрытия для дополнительных советов */ },
                                            onActionClick = { tip ->
                                                when (tip.actionResId) {
                                                    com.davidbugayov.financeanalyzer.ui.R.string.action_add_transaction -> onAddTransaction()
                                                    com.davidbugayov.financeanalyzer.ui.R.string.action_start_saving, com.davidbugayov.financeanalyzer.ui.R.string.action_continue_saving -> {
                                                        // TODO: Навигация к разделу сбережений
                                                    }
                                                    com.davidbugayov.financeanalyzer.ui.R.string.action_study_statistics, com.davidbugayov.financeanalyzer.ui.R.string.action_analyze_spending -> {
                                                        // Фокус на графиках
                                                    }
                                                }
                                            },
                                        )
                                    }

                                    // Оставляем предсказания
                                    val predictExpensesUseCase =
                                        remember {
                                            org.koin.core.context.GlobalContext.get()
                                                .get<PredictFutureExpensesUseCase>()
                                        }
                                    val predictedExpenses = remember { predictExpensesUseCase(state.transactions) }

                                    // В UI, добавляем карточку предсказаний (тональная)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.4f,
                                            ),
                                        ),
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.prediction_title),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = stringResource(
                                                    id = com.davidbugayov.financeanalyzer.ui.R.string.prediction_next_month,
                                                    predictedExpenses.amount.toString(),
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    // Ключевые инвестиционные советы без дублей
                                    val keyInvestmentTips = listOf(
                                        stringResource(com.davidbugayov.financeanalyzer.ui.R.string.investment_tip_bonds),
                                        stringResource(com.davidbugayov.financeanalyzer.ui.R.string.investment_tip_diversification),
                                        stringResource(com.davidbugayov.financeanalyzer.ui.R.string.investment_tip_stocks),
                                    )
                                    Column {
                                        Text("Важные инвестиционные советы")
                                        keyInvestmentTips.forEach { tip ->
                                            Text(tip)
                                        }
                                    }
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
    val firstValue = recentPoints.first().value.amount.toDouble()
    val lastValue = recentPoints.last().value.amount.toDouble()

    // Избегаем деления на ноль
    if (firstValue <= 0.0) return 0.0

    // Рассчитываем относительное изменение
    val growth = (lastValue - firstValue) / firstValue

    // Ограничиваем значение в разумных пределах (-1 до 1)
    return growth.coerceIn(-1.0, 1.0)
}
