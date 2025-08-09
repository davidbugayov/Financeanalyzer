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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.davidbugayov.financeanalyzer.ui.R as UiR

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
                title = stringResource(UiR.string.detailed_financial_statistics),
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
                        .padding(dimensionResource(UiR.dimen.financial_statistics_card_padding)),
            ) {
                // Период (верхняя карточка)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        shape = RoundedCornerShape(
                            dimensionResource(UiR.dimen.financial_statistics_card_corner_radius),
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
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
                                    text = state.period,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }

                // Табы
                item {
                    Spacer(Modifier.height(dimensionResource(UiR.dimen.financial_statistics_spacer_medium)))
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Text(
                                    stringResource(id = UiR.string.stat_by_category),
                                )
                            },
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    stringResource(id = UiR.string.stat_trends),
                                )
                            },
                        )
                    }
                }

                // Контент по табам
                item {
                    when (selectedTabIndex) {
                        0 -> {
                            // Категории: анализ расходов
                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                    title = stringResource(UiR.string.expense_analysis),
                                    icon = Icons.Default.Analytics,
                                    statistics = expenseAnalysis,
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        1 -> {
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
                                    title = stringResource(UiR.string.transaction_statistics),
                                    icon = Icons.Default.Receipt,
                                    statistics = transactionStats,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Spacer(Modifier.height(dimensionResource(UiR.dimen.financial_statistics_spacer_medium)))
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
                                            id = UiR.string.prediction_title,
                                        ),
                                    )
                                    Text(
                                        stringResource(
                                            id = UiR.string.prediction_next_month,
                                            predictedExpenses.amount.toString(),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                // Советы убраны — фокус на KPI и трендах

                // Инвестиционные советы убраны как вторичные
            }
        }
    }
}