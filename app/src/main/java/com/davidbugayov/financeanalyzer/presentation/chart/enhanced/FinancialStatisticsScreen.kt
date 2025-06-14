package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.BudgetTip
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.EnhancedCategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.EnhancedLineChart
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.EnhancedSummaryCard
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.FinancialHealthMetricsCard
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.LineChartTypeSelector
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model.LineChartDisplayMode
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartEffect
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartIntent
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.viewmodel.EnhancedFinanceChartViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor
import com.davidbugayov.financeanalyzer.utils.DateUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.math.BigDecimal

/**
 * Улучшенный экран с финансовыми графиками.
 * Поддерживает свайп между разными типами визуализации и обновленный дизайн.
 *
 * @param viewModel ViewModel для управления состоянием экрана
 * @param onNavigateBack Колбэк для навигации назад
 * @param onNavigateToTransactions Опциональный колбэк для навигации к списку транзакций с фильтрами
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)
@Composable
fun FinancialStatisticsScreen(
    onNavigateBack: () -> Unit,
) {
    // Используем новую ViewModel
    val viewModel: EnhancedFinanceChartViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var shouldScrollToSummaryCard by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val pagerState = rememberPagerState(pageCount = { 3 }, initialPage = 0)
    var lineChartDisplayMode by remember { mutableStateOf(LineChartDisplayMode.BOTH) }

    // Логируем открытие экрана и загружаем данные
    LaunchedEffect(Unit) {
        viewModel.handleIntent(EnhancedFinanceChartIntent.LoadData)
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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant,
        ),
    )

    // Форматтер дат
    val dateFormat = remember { SimpleDateFormat("dd MMMM", Locale.forLanguageTag("ru-RU")) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.charts_title),
                showBackButton = true,
                onBackClick = {
                    onNavigateBack()
                },
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGradient),
        ) {
            if (state.isLoading) {
                CenteredLoadingIndicator(message = stringResource(R.string.loading_data))
            } else if (state.error != null) {
                ErrorContent(
                    error = state.error ?: stringResource(R.string.unknown_error),
                    onRetry = { viewModel.handleIntent(EnhancedFinanceChartIntent.LoadData) },
                )
            } else {
                // Основной контент с графиками и фильтрами всегда отображается
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                ) {
                    // Если нет транзакций, показываем кнопку "Добавить транзакцию" над графиками
                    if (state.transactions.isEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensionResource(
                                        R.dimen.finance_chart_screen_padding,
                                    ),
                                    vertical = 16.dp,
                                )
                                .clickable {
                                    viewModel.handleIntent(
                                        EnhancedFinanceChartIntent.AddTransactionClicked,
                                    )
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
                                        text = stringResource(R.string.add_first_transaction),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = stringResource(R.string.analytics_magic_hint),
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
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.finance_chart_screen_padding)),
                    )

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
                            text = { Text(stringResource(R.string.tab_categories)) },
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            text = { Text(stringResource(R.string.tab_dynamics)) },
                        )
                        Tab(
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            text = { Text(stringResource(R.string.tab_analysis)) },
                        )
                    }

                    // Горизонтальный пейджер для свайпа между графиками
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(
                                    R.dimen.finance_chart_screen_tab_row_padding,
                                ),
                            ),
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Пирографик по категориям
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxSize()
                                        .padding(
                                            vertical = dimensionResource(
                                                R.dimen.finance_chart_screen_padding,
                                            ),
                                        ),
                                ) {
                                    // Получаем данные категорий в зависимости от выбранного режима
                                    val categoryData = if (state.showExpenses) {
                                        Timber.d(
                                            "Данные по расходам за период ${DateUtils.formatDate(
                                                state.startDate,
                                            )} – ${DateUtils.formatDate(
                                                state.endDate,
                                            )}: ${state.expensesByCategory.size} категорий, сумма: ${state.expensesByCategory.values.fold(
                                                BigDecimal.ZERO,
                                            ) { acc, money -> acc.add(money.amount) }}",
                                        )
                                        Timber.d(
                                            "Список категорий расходов: ${state.expensesByCategory.keys.joinToString()}",
                                        )
                                        state.expensesByCategory
                                    } else {
                                        Timber.d(
                                            "Данные по доходам за период ${DateUtils.formatDate(
                                                state.startDate,
                                            )} – ${DateUtils.formatDate(
                                                state.endDate,
                                            )}: ${state.incomeByCategory.size} категорий, сумма: ${state.incomeByCategory.values.fold(
                                                BigDecimal.ZERO,
                                            ) { acc, money -> acc.add(money.amount) }}",
                                        )
                                        Timber.d(
                                            "Список категорий доходов: ${state.incomeByCategory.keys.joinToString()}",
                                        )
                                        state.incomeByCategory
                                    }

                                    // Проверяем, нет ли данных для отображения
                                    if (categoryData.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(
                                                    dimensionResource(
                                                        R.dimen.finance_chart_screen_piechart_height,
                                                    ),
                                                )
                                                .padding(
                                                    top = dimensionResource(
                                                        R.dimen.finance_chart_screen_padding,
                                                    ),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    R.string.enhanced_chart_no_data,
                                                ),
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                        }
                                    } else {
                                        // Улучшенный пирограф категорий (данные готовит ViewModel)
                                        EnhancedCategoryPieChart(
                                            items = state.pieChartData,
                                            selectedIndex = null,
                                            onSectorClick = { item ->
                                                if (item != null) {
                                                    selectedCategory = item.original?.name
                                                    item.original?.name?.let { categoryName ->
                                                        // onNavigateToTransactions?.invoke(categoryName, state.startDate, state.endDate)
                                                    }
                                                } else {
                                                    selectedCategory = null
                                                }
                                            },
                                            modifier = Modifier.padding(
                                                top = dimensionResource(
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = dimensionResource(
                                                R.dimen.finance_chart_screen_padding,
                                            ),
                                        ),
                                ) {
                                    // Линейный график
                                    LineChartTypeSelector(
                                        selectedMode = lineChartDisplayMode,
                                        onModeSelected = { lineChartDisplayMode = it },
                                    )

                                    val periodText = "${dateFormat.format(state.startDate)} – ${dateFormat.format(
                                        state.endDate,
                                    )}"

                                    EnhancedLineChart(
                                        incomeData = state.incomeLineChartData,
                                        expenseData = state.expenseLineChartData,
                                        showIncome = lineChartDisplayMode.showIncome,
                                        showExpense = lineChartDisplayMode.showExpense,
                                        title = stringResource(id = R.string.chart_title_dynamics),
                                        period = periodText,
                                    )
                                }
                            }

                            2 -> {
                                // Аналитика и инсайты
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Карточка "Полная статистика"
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                bottom = dimensionResource(
                                                    R.dimen.finance_chart_screen_card_bottom_padding,
                                                ),
                                            )
                                            .clickable {
                                                // Переход на экран подробной статистики через navController
                                                // navController.navigate(Screen.FinancialStatistics.createRoute(state.startDate.time, state.endDate.time))
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 4.dp,
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = LocalFriendlyCardBackgroundColor.current,
                                        ),
                                    ) {
                                        Row(
                                            modifier = Modifier
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
                                                    text = stringResource(
                                                        R.string.detailed_financial_statistics,
                                                    ),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                )

                                                Text(
                                                    text = stringResource(
                                                        R.string.explore_your_financial_metrics,
                                                    ),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Filled.Analytics,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }

                                    // Оставляем остальной контент этой вкладки
                                    Spacer(
                                        modifier = Modifier.height(
                                            dimensionResource(
                                                R.dimen.finance_chart_screen_vertical_spacing,
                                            ),
                                        ),
                                    )

                                    // Блок рекомендаций по бюджету
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.budget_tips_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 8.dp),
                                        )
                                        BudgetTip(
                                            icon = Icons.Filled.AccountBalanceWallet,
                                            title = stringResource(
                                                R.string.budget_tip_save_10_title,
                                            ),
                                            description = stringResource(
                                                R.string.budget_tip_save_10_desc,
                                            ),
                                        )
                                        BudgetTip(
                                            icon = Icons.Filled.BarChart,
                                            title = stringResource(
                                                R.string.budget_tip_control_categories_title,
                                            ),
                                            description = stringResource(
                                                R.string.budget_tip_control_categories_desc,
                                            ),
                                        )
                                        BudgetTip(
                                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                                            title = stringResource(
                                                R.string.budget_tip_set_goals_title,
                                            ),
                                            description = stringResource(
                                                R.string.budget_tip_set_goals_desc,
                                            ),
                                        )
                                        BudgetTip(
                                            icon = Icons.Filled.Check,
                                            title = stringResource(
                                                R.string.budget_tip_check_weekly_title,
                                            ),
                                            description = stringResource(
                                                R.string.budget_tip_check_weekly_desc,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Вторая карточка - метрики финансового здоровья
                    FinancialHealthMetricsCard(
                        savingsRate = state.savingsRate,
                        averageDailyExpense = state.averageDailyExpense ?: Money.zero(),
                        monthsOfSavings = state.monthsOfSavings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(R.dimen.finance_chart_screen_padding),
                                vertical = dimensionResource(
                                    R.dimen.finance_chart_screen_vertical_spacing,
                                ),
                            ),
                    )

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
