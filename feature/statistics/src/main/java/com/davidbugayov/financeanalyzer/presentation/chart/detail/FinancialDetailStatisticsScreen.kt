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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.presentation.chart.detail.state.FinancialDetailStatisticsContract
import com.davidbugayov.financeanalyzer.presentation.chart.detail.viewmodel.FinancialDetailStatisticsViewModel
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.components.card.FinancialDataMapper
import com.davidbugayov.financeanalyzer.ui.components.card.PremiumStatisticsCard
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
    viewModel.state.collectAsState().value
    val metrics = viewModel.metrics.collectAsState().value
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }

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
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        shape =
                            RoundedCornerShape(
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
                                // Используем общий компонент PeriodFilterBar для локализованного периода
                                com.davidbugayov.financeanalyzer.presentation.chart.statistic.components
                                    .PeriodFilterBar(
                                        periodType = com.davidbugayov.financeanalyzer.navigation.model.PeriodType.CUSTOM,
                                        startDate = java.util.Date(startDate),
                                        endDate = java.util.Date(endDate),
                                        onChangePeriod = { _, _, _ -> },
                                        modifier = Modifier,
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
                                        averageDailyExpense = metrics.averageDailyExpense.formatForDisplay(true),
                                        averageMonthlyExpense = metrics.averageMonthlyExpense.formatForDisplay(true),
                                        topIncomeCategory =
                                            com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryLocalization
                                                .displayName(
                                                    context,
                                                    metrics.topIncomeCategory,
                                                ),
                                        topExpenseCategory =
                                            com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryLocalization
                                                .displayName(
                                                    context,
                                                    metrics.topExpenseCategory,
                                                ),
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
                                        averageIncomePerTransaction =
                                            metrics.averageIncomePerTransaction.formatForDisplay(
                                                true,
                                            ),
                                        averageExpensePerTransaction =
                                            metrics.averageExpensePerTransaction.formatForDisplay(
                                                true,
                                            ),
                                        maxIncome = metrics.maxIncome.formatForDisplay(true),
                                        maxExpense = metrics.maxExpense.formatForDisplay(true),
                                        savingsRate = metrics.savingsRate,
                                        monthsOfSavings = metrics.monthsOfSavings,
                                    )
                                PremiumStatisticsCard(
                                    title = stringResource(UiR.string.transaction_statistics),
                                    icon = Icons.Default.Receipt,
                                    statistics = transactionStats,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                // Блок предсказания расходов удалён по требованию
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
