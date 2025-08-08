package com.davidbugayov.financeanalyzer.presentation.chart.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.usecase.analytics.PredictFutureExpensesUseCase
import com.davidbugayov.financeanalyzer.presentation.chart.detail.components.KeyMetricsCard
import com.davidbugayov.financeanalyzer.presentation.chart.detail.state.FinancialDetailStatisticsContract
import com.davidbugayov.financeanalyzer.presentation.chart.detail.viewmodel.FinancialDetailStatisticsViewModel
import com.davidbugayov.financeanalyzer.ui.R
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.card.FinancialDataMapper
import com.davidbugayov.financeanalyzer.ui.components.card.PremiumInsightsCard
import com.davidbugayov.financeanalyzer.ui.components.card.PremiumStatisticsCard
import com.davidbugayov.financeanalyzer.ui.components.card.SmartRecommendationCard
import com.davidbugayov.financeanalyzer.ui.components.card.SmartRecommendationGenerator
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

/**
 * Экран подробной финансовой статистики
 *
 * @param startDate Начало периода
 * @param endDate Конец периода
 * @param onNavigateBack Обработчик нажатия на кнопку "Назад"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialDetailStatisticsScreen(
    startDate: Long,
    endDate: Long,
    onNavigateBack: () -> Unit,
) {
    val viewModel: FinancialDetailStatisticsViewModel = koinViewModel { parametersOf(startDate, endDate) }
    val state = viewModel.state.collectAsState().value
    val metrics = viewModel.metrics.collectAsState().value

    var selectedTabIndex by remember { mutableStateOf(0) }
    var includeTransfers by remember { mutableStateOf(false) }
    var includeRefunds by remember { mutableStateOf(false) }

    // Временный лог для диагностики
    Timber.d(
        "FinancialDetailStatisticsScreen: metrics.savingsRate=${metrics.savingsRate}, metrics.monthsOfSavings=${metrics.monthsOfSavings}",
    )

    rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Запускаем загрузку данных при первом запуске
    LaunchedEffect(Unit) {
        viewModel.handleIntent(FinancialDetailStatisticsContract.Intent.LoadData)
    }

    // Обработка эффектов (например, показ ошибок)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FinancialDetailStatisticsContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val backgroundGradient =
        Brush.verticalGradient(
            colors =
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
        )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.detailed_financial_statistics),
                showBackButton = true,
                onBackClick = onNavigateBack,
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
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.financial_statistics_card_padding)),
            ) {
                // Панель фильтров
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.overview),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_small)))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.4f,
                                        ),
                                ),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.financial_statistics_period),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = state.period,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    val chips =
                                        listOf(
                                            com.davidbugayov.financeanalyzer.ui.R.string.all_time,
                                            com.davidbugayov.financeanalyzer.ui.R.string.year,
                                            com.davidbugayov.financeanalyzer.ui.R.string.month,
                                            com.davidbugayov.financeanalyzer.ui.R.string.week,
                                            com.davidbugayov.financeanalyzer.ui.R.string.day,
                                        )
                                    chips.forEachIndexed { index, resId ->
                                        AssistChip(
                                            onClick = { /* TODO: hook to VM */ },
                                            label = { Text(stringResource(id = resId)) },
                                            colors =
                                                AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surface,
                                                ),
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = includeTransfers, onCheckedChange = { includeTransfers = it })
                                    Text(
                                        stringResource(
                                            id = com.davidbugayov.financeanalyzer.ui.R.string.include_transfers,
                                        ),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Checkbox(checked = includeRefunds, onCheckedChange = { includeRefunds = it })
                                    Text(
                                        stringResource(
                                            id = com.davidbugayov.financeanalyzer.ui.R.string.include_refunds,
                                        ),
                                    )
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        onClick = {
                                            includeTransfers = false
                                            includeRefunds = false
                                        },
                                    ) {
                                        Text(
                                            stringResource(
                                                id = com.davidbugayov.financeanalyzer.ui.R.string.reset_filters,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Табы
                item {
                    Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Text(
                                    stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.stat_by_category),
                                )
                            },
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.stat_by_merchant),
                                )
                            },
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = {
                                Text(
                                    stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.stat_by_account),
                                )
                            },
                        )
                        Tab(
                            selected = selectedTabIndex == 3,
                            onClick = { selectedTabIndex = 3 },
                            text = {
                                Text(
                                    stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.stat_calendar),
                                )
                            },
                        )
                        Tab(
                            selected = selectedTabIndex == 4,
                            onClick = { selectedTabIndex = 4 },
                            text = {
                                Text(
                                    stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.stat_trends),
                                )
                            },
                        )
                    }
                }

                // Контент по табам
                item {
                    when (selectedTabIndex) {
                        0 -> {
                            // Категории: ключевые метрики + анализ расходов
                            Column(modifier = Modifier.fillMaxWidth()) {
                                KeyMetricsCard(
                                    income = state.income,
                                    expense = state.expense,
                                    savingsRate = metrics.savingsRate,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
                                val expenseAnalysis =
                                    FinancialDataMapper.createExpenseAnalysis(
                                        averageDailyExpense = metrics.averageDailyExpense.format(true),
                                        averageMonthlyExpense = metrics.averageMonthlyExpense.format(true),
                                        topIncomeCategory = metrics.topIncomeCategory,
                                        topExpenseCategory = metrics.topExpenseCategory,
                                        topExpenseCategories =
                                            metrics.topExpenseCategories.map {
                                                it.first to
                                                    it.second.format(
                                                        true,
                                                    )
                                            },
                                        mostFrequentExpenseDay = metrics.mostFrequentExpenseDay,
                                    )
                                PremiumStatisticsCard(
                                    title = stringResource(R.string.expense_analysis),
                                    icon = Icons.Default.Analytics,
                                    statistics = expenseAnalysis,
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        1 -> {
                            // Контрагенты: паттерны и инсайты
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val spendingPatterns =
                                    FinancialDataMapper.createSpendingPatternInsights(
                                        mostFrequentExpenseDay = metrics.mostFrequentExpenseDay,
                                        expenseTransactionsCount = metrics.expenseTransactionsCount,
                                        averageExpensePerTransaction = metrics.averageExpensePerTransaction.amount.toFloat(),
                                    )
                                PremiumInsightsCard(
                                    title = stringResource(R.string.spending_patterns),
                                    insights = spendingPatterns,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        2 -> {
                            // Счета: заглушка
                            Text(
                                text =
                                    stringResource(
                                        id = com.davidbugayov.financeanalyzer.ui.R.string.no_data_to_display,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        3 -> {
                            // Календарь: заглушка
                            Text(
                                text =
                                    stringResource(
                                        id = com.davidbugayov.financeanalyzer.ui.R.string.no_data_to_display,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        4 -> {
                            // Тренды: статистика транзакций + предсказания
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val transactionStats =
                                    FinancialDataMapper.createTransactionStatistics(
                                        totalTransactions = metrics.totalTransactions,
                                        incomeTransactionsCount = metrics.incomeTransactionsCount,
                                        expenseTransactionsCount = metrics.expenseTransactionsCount,
                                        averageIncomePerTransaction = metrics.averageIncomePerTransaction.format(true),
                                        averageExpensePerTransaction =
                                            metrics.averageExpensePerTransaction.format(
                                                true,
                                            ),
                                        maxIncome = metrics.maxIncome.format(true),
                                        maxExpense = metrics.maxExpense.format(true),
                                        savingsRate = metrics.savingsRate,
                                        monthsOfSavings = metrics.monthsOfSavings,
                                    )
                                PremiumStatisticsCard(
                                    title = stringResource(R.string.transaction_statistics),
                                    icon = Icons.Default.Receipt,
                                    statistics = transactionStats,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
                                val predictExpensesUseCase =
                                    remember {
                                        org.koin.core.context.GlobalContext.get()
                                            .get<PredictFutureExpensesUseCase>()
                                    }
                                val predictedExpenses =
                                    remember { predictExpensesUseCase(transactions = state.transactions) }
                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                ) {
                                    Text(
                                        stringResource(
                                            id = com.davidbugayov.financeanalyzer.ui.R.string.prediction_title,
                                        ),
                                    )
                                    Text(
                                        stringResource(
                                            id = com.davidbugayov.financeanalyzer.ui.R.string.prediction_next_month,
                                            predictedExpenses.amount.toString(),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                // Советы (общая секция в конце)
                item {
                    Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tips),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    val healthRecommendations =
                        SmartRecommendationGenerator.generateCriticalFinancialRecommendations(
                            savingsRate = metrics.savingsRate,
                            monthsOfEmergencyFund = metrics.monthsOfSavings,
                        )
                    SmartRecommendationCard(
                        recommendations = healthRecommendations,
                        title = stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.key_recommendations),
                        subtitle =
                            stringResource(
                                id = com.davidbugayov.financeanalyzer.ui.R.string.for_financial_health,
                            ),
                    )
                }

                // Инвестиционные советы
                item {
                    Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.investment_tips),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    val keyInvestmentTips =
                        listOf(
                            stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.investment_tip_bonds),
                            stringResource(
                                id = com.davidbugayov.financeanalyzer.ui.R.string.investment_tip_diversification,
                            ),
                            stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.investment_tip_stocks),
                        )
                    Column {
                        Text(
                            stringResource(id = com.davidbugayov.financeanalyzer.ui.R.string.investment_tips),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        keyInvestmentTips.forEach { tip -> Text(tip) }
                    }
                    Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))
                }
            }
        }
    }
}
