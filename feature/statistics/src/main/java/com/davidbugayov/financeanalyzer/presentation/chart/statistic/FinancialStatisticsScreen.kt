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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger
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
import com.davidbugayov.financeanalyzer.ui.components.card.AdviceCard
import com.davidbugayov.financeanalyzer.ui.components.card.AdvicePriority
import com.davidbugayov.financeanalyzer.ui.components.card.SmartRecommendationCard
import com.davidbugayov.financeanalyzer.ui.components.card.SmartRecommendationGenerator
import com.davidbugayov.financeanalyzer.ui.components.card.SmartCardStyle
import com.davidbugayov.financeanalyzer.utils.DateUtils
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random
import androidx.compose.material.icons.outlined.Lightbulb

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

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("finance_analyzer_prefs", Context.MODE_PRIVATE) }
    var showTip by remember { mutableStateOf(prefs.getBoolean("show_statistics_tip", true)) }

    // Показываем типс только один раз автоматически
    LaunchedEffect(Unit) {
        if (showTip) {
            prefs.edit().putBoolean("show_statistics_tip", false).apply()
        }
    }

    // Список динамических советов
    val tips = listOf(
        Pair(stringResource(R.string.statistics_tip_title), stringResource(R.string.statistics_tip_text)),
        Pair("Сравнивайте периоды", "Смотрите, как меняются ваши расходы и доходы от месяца к месяцу — ищите тренды!"),
        Pair("Категории под контролем", "Анализируйте, на что уходит больше всего денег, и оптимизируйте свои траты."),
        Pair("Планируйте бюджет", "Используйте аналитику для постановки финансовых целей и отслеживания прогресса."),
        Pair("Следите за сбережениями", "Проверьте, сколько месяцев вы сможете прожить на свои накопления — это ваш финансовый буфер.")
    )
    var tipIndex by remember { mutableStateOf(Random.nextInt(tips.size)) }
    val currentTip = tips[tipIndex]
    var tipRequestedFromTopBar by remember { mutableStateOf(false) }

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
                title = stringResource(R.string.charts_title),
                showBackButton = true,
                onBackClick = {
                    onNavigateBack()
                },
                actions = {
                    if (!showTip) {
                        IconButton(onClick = {
                            var newIndex: Int
                            do {
                                newIndex = Random.nextInt(tips.size)
                            } while (newIndex == tipIndex && tips.size > 1)
                            tipIndex = newIndex
                            showTip = true
                            tipRequestedFromTopBar = true
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = "Показать совет",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
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
                CenteredLoadingIndicator(message = stringResource(R.string.loading_data))
            } else if (state.error != null) {
                ErrorContent(
                    error = state.error ?: stringResource(R.string.unknown_error),
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
                    if (showTip) {
                        // Используем новую систему рекомендаций для статистики
                        val statisticsTips = SmartRecommendationGenerator.generateStatisticsTips()
                        val randomTip = statisticsTips.random()

                        SmartRecommendationCard(
                            recommendations = listOf(randomTip),
                            title = currentTip.first,
                            subtitle = currentTip.second,
                            style = SmartCardStyle.MINIMAL,
                            showPriorityIndicator = false,
                            onDismiss = {
                                showTip = false
                                tipRequestedFromTopBar = false
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    // --- Динамический типс: всегда разные, можно показать по кнопке ---
                    // Если нет транзакций, показываем кнопку "Добавить транзакцию" над графиками
                    if (state.transactions.isEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
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
                        periodType = state.periodType,
                        viewModel = viewModel,
                        modifier =
                            Modifier
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
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal =
                                        dimensionResource(
                                            R.dimen.finance_chart_screen_tab_row_padding,
                                        ),
                                ),
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Пирографик по категориям
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .fillMaxSize()
                                            .padding(
                                                vertical =
                                                    dimensionResource(
                                                        R.dimen.finance_chart_screen_padding,
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
                                                    stringResource(
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
                                            .padding(
                                                vertical =
                                                    dimensionResource(
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
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp, bottom = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Карточка "Полная статистика"
                                    Card(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    bottom =
                                                        dimensionResource(
                                                            R.dimen.finance_chart_screen_card_bottom_padding,
                                                        ),
                                                )
                                                .clickable {
                                                    // Переход на экран подробной статистики
                                                    viewModel.navigateToDetailedStatistics()
                                                },
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
                                                        stringResource(
                                                            R.string.detailed_financial_statistics,
                                                        ),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                )

                                                Text(
                                                    text =
                                                        stringResource(
                                                            R.string.explore_your_financial_metrics,
                                                        ),
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

                                    // Блок критических финансовых рекомендаций
                                    if (state.recommendations.isNotEmpty()) {
                                        // Генерируем умные рекомендации на основе реальных данных
                                        val smartRecommendations = SmartRecommendationGenerator.generateCriticalFinancialRecommendations(
                                            savingsRate = state.savingsRate.toFloat(),
                                            monthsOfEmergencyFund = state.monthsOfSavings.toFloat(),
                                            topExpenseCategory = state.expensesByCategory.maxByOrNull { it.value.amount }?.key ?: "",
                                            topCategoryPercentage = (state.expensesByCategory.maxByOrNull { it.value.amount }?.value?.amount?.toFloat() ?: 0f) /
                                                (state.expense?.amount?.toFloat() ?: 1f) * 100f,
                                            totalTransactions = state.transactions.size,
                                            unusualSpendingDetected = false
                                        )

                                        SmartRecommendationCard(
                                            recommendations = smartRecommendations,
                                            title = "🎯 Персональные советы",
                                            subtitle = "На основе анализа ваших финансов",
                                            style = SmartCardStyle.COMPACT,
                                            showPriorityIndicator = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
                                        )
                                    }

                                    // Блок профессиональных бюджетных советов
                                    val budgetTips = SmartRecommendationGenerator.generateTopBudgetingTips()
                                    SmartRecommendationCard(
                                        recommendations = budgetTips,
                                        title = "💰 Золотые правила бюджета",
                                        subtitle = "Проверенные принципы финансового планирования",
                                        style = SmartCardStyle.ENHANCED,
                                        showPriorityIndicator = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Вторая карточка - метрики финансового здоровья
                    FinancialHealthMetricsCard(
                        savingsRate = state.savingsRate,
                        averageDailyExpense = state.averageDailyExpense ?: Money.zero(),
                        monthsOfSavings = state.monthsOfSavings,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensionResource(R.dimen.finance_chart_screen_padding),
                                    vertical =
                                        dimensionResource(
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
