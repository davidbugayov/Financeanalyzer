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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.components.LineChartTypeSelector
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.davidbugayov.financeanalyzer.utils.DateUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.viewmodel.EnhancedFinanceChartViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartIntent
import androidx.compose.ui.res.dimensionResource
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.flow.collectLatest
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.state.EnhancedFinanceChartEffect

/**
 * Улучшенный экран с финансовыми графиками.
 * Поддерживает свайп между разными типами визуализации и обновленный дизайн.
 *
 * @param viewModel ViewModel для управления состоянием экрана
 * @param onNavigateBack Колбэк для навигации назад
 * @param onNavigateToTransactions Опциональный колбэк для навигации к списку транзакций с фильтрами
 * @param onNavigateToStatistics Опциональный колбэк для навигации к экрану финансовой статистики
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun EnhancedFinanceChartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: ((String, Date, Date) -> Unit)? = null,
    onNavigateToStatistics: ((List<Transaction>, Money, Money, String) -> Unit)? = null
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
                else -> {}
            }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.charts_title),
                showBackButton = true,
                onBackClick = {
                    onNavigateBack()
                }
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGradient)
        ) {
            if (state.isLoading) {
                CenteredLoadingIndicator(message = stringResource(R.string.loading_data))
            } else if (state.error != null) {
                ErrorContent(
                    error = state.error ?: stringResource(R.string.unknown_error),
                    onRetry = { viewModel.handleIntent(EnhancedFinanceChartIntent.LoadData) }
                )
            } else if (state.transactions.isEmpty()) {
                EmptyContent(
                    message = stringResource(R.string.enhanced_chart_no_data),
                    onActionClick = { viewModel.handleIntent(EnhancedFinanceChartIntent.LoadData) }
                )
            } else {
                // Основной контент с графиками
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Карточка с общим балансом и периодом
                    EnhancedSummaryCard(
                        income = state.income ?: Money.zero(),
                        expense = (state.expense ?: Money.zero()).abs(),
                        startDate = state.startDate,
                        endDate = state.endDate,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.finance_chart_screen_padding))
                    )

                    // Табы для переключения между типами графиков
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(0)
                                }
                            },
                            text = { Text(stringResource(R.string.tab_categories)) }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(1)
                                }
                            },
                            text = { Text(stringResource(R.string.tab_dynamics)) }
                        )
                        Tab(
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(2)
                                }
                            },
                            text = { Text(stringResource(R.string.tab_analysis)) }
                        )
                    }

                    // Горизонтальный пейджер для свайпа между графиками
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.finance_chart_screen_tab_row_padding))
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Пирографик по категориям
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxSize()
                                        .padding(vertical = dimensionResource(R.dimen.finance_chart_screen_padding))
                                ) {
                                    // Получаем данные категорий в зависимости от выбранного режима
                                    val categoryData = if (state.showExpenses) {
                                        Timber.d("Данные по расходам за период ${DateUtils.formatDate(state.startDate)} - ${DateUtils.formatDate(state.endDate)}: ${state.expensesByCategory.size} категорий, сумма: ${state.expensesByCategory.values.sumOf { it.amount.toDouble() }}")
                                        Timber.d("Список категорий расходов: ${state.expensesByCategory.keys.joinToString()}")
                                        state.expensesByCategory
                                    } else {
                                        Timber.d("Данные по доходам за период ${DateUtils.formatDate(state.startDate)} - ${DateUtils.formatDate(state.endDate)}: ${state.incomeByCategory.size} категорий, сумма: ${state.incomeByCategory.values.sumOf { it.amount.toDouble() }}")
                                        Timber.d("Список категорий доходов: ${state.incomeByCategory.keys.joinToString()}")
                                        state.incomeByCategory
                                    }

                                    // Проверяем, нет ли данных для отображения
                                    if (categoryData.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(dimensionResource(R.dimen.finance_chart_screen_piechart_height))
                                                .padding(top = dimensionResource(R.dimen.finance_chart_screen_padding)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(R.string.enhanced_chart_no_data),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    } else {
                                        // Улучшенный пирограф категорий (данные готовит ViewModel)
                                        EnhancedCategoryPieChart(
                                            items = state.pieChartData,
                                            selectedIndex = null,
                                            onSectorClick = { item ->
                                                if (item != null) {
                                                    selectedCategory = item.category?.name
                                                    item.category?.name?.let { categoryName ->
                                                        onNavigateToTransactions?.invoke(categoryName, state.startDate, state.endDate)
                                                    }
                                                } else {
                                                    selectedCategory = null
                                                }
                                            },
                                            modifier = Modifier.padding(top = dimensionResource(R.dimen.finance_chart_screen_padding)),
                                            showExpenses = state.showExpenses,
                                            onShowExpensesChange = { showExpenses ->
                                                viewModel.handleIntent(EnhancedFinanceChartIntent.ToggleExpenseView(showExpenses))
                                            }
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // Линейный график динамики
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = dimensionResource(R.dimen.finance_chart_screen_padding))
                                ) {
                                    // Линейный график
                                    LineChartTypeSelector(
                                        selectedMode = lineChartDisplayMode,
                                        onModeSelected = { lineChartDisplayMode = it }
                                    )

                                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                                    val periodText = "${dateFormat.format(state.startDate)} - ${dateFormat.format(state.endDate)}"

                                    EnhancedLineChart(
                                        incomeData = state.incomeLineChartData,
                                        expenseData = state.expenseLineChartData,
                                        showIncome = lineChartDisplayMode.showIncome,
                                        showExpense = lineChartDisplayMode.showExpense,
                                        title = stringResource(id = R.string.chart_title_dynamics),
                                        period = periodText
                                    )
                                }
                            }

                            2 -> {
                                // Аналитика и инсайты
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = dimensionResource(R.dimen.finance_chart_screen_padding))
                                ) {
                                    // Карточка "Полная статистика"
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = dimensionResource(R.dimen.finance_chart_screen_card_bottom_padding))
                                            .clickable {
                                                // Переход на экран подробной статистики
                                                onNavigateToStatistics?.invoke(
                                                    state.transactions,
                                                    state.income ?: Money.zero(),
                                                    state.expense ?: Money.zero(),
                                                    state.periodText
                                                )
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(dimensionResource(R.dimen.finance_chart_screen_card_content_padding)),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.detailed_financial_statistics),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Text(
                                                    text = stringResource(R.string.explore_your_financial_metrics),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Filled.Analytics,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    // Оставляем остальной контент этой вкладки
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.finance_chart_screen_vertical_spacing)))

                                    Text(
                                        text = stringResource(R.string.analytics_coming_soon),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.finance_chart_screen_analytics_text_padding)),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Карточка с метриками финансового здоровья
                    FinancialHealthMetricsCard(
                        savingsRate = state.savingsRate,
                        averageDailyExpense = state.averageDailyExpense,
                        monthsOfSavings = state.monthsOfSavings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(R.dimen.finance_chart_screen_padding),
                                vertical = dimensionResource(R.dimen.finance_chart_screen_vertical_spacing)
                            )
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