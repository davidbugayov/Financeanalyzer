package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.components.DailyExpensesChart
import com.davidbugayov.financeanalyzer.presentation.chart.components.PieChartSection
import com.davidbugayov.financeanalyzer.presentation.chart.components.SavingsRateDialog
import com.davidbugayov.financeanalyzer.presentation.chart.components.StatisticsSection
import com.davidbugayov.financeanalyzer.presentation.chart.components.SummarySection
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartIntent
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.presentation.components.LoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceChartScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Логируем открытие экрана графиков
    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "finance_chart",
            screenClass = "FinanceChartScreen"
        )
    }

    // Логируем изменение периода
    LaunchedEffect(state.periodType, state.startDate, state.endDate) {
        AnalyticsUtils.logChartViewed(
            chartType = "finance_chart",
            periodType = state.periodType.name.lowercase()
        )
    }

    // UI state
    var showSavingsRateInfo by remember { mutableStateOf(false) }

    // Format dates for display
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val periodText = when (state.periodType) {
        PeriodType.ALL -> stringResource(R.string.period_all)
        PeriodType.DAY -> dateFormat.format(state.startDate)
        PeriodType.WEEK, PeriodType.MONTH, PeriodType.QUARTER, PeriodType.YEAR, PeriodType.CUSTOM ->
            "${dateFormat.format(state.startDate)} - ${dateFormat.format(state.endDate)}"
    }
    
    // Filter transactions based on selected date range
    val filteredTransactions = state.transactions.filter { transaction ->
        val isInRange = transaction.date >= state.startDate && transaction.date <= state.endDate
        Timber.tag("FinanceChart").d("Transaction ${transaction.id}: date=${dateFormat.format(transaction.date)}, isInRange=$isInRange")
        isInRange
    }

    Timber.tag("FinanceChart").d("Total transactions: ${state.transactions.size}, Filtered: ${filteredTransactions.size}")
    Timber.tag("FinanceChart").d("Date range: ${dateFormat.format(state.startDate)} - ${dateFormat.format(state.endDate)}")

    // Calculate totals
    val totalIncome = filteredTransactions
        .filter { !it.isExpense }
        .map { it.amount }
        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

    val totalExpense = filteredTransactions
        .filter { it.isExpense }
        .map { it.amount }
        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

    Timber.tag("FinanceChart").d("Total income: $totalIncome, Total expense: $totalExpense")

    // Calculate statistics
    val daysInPeriod = max(1, (state.endDate.time - state.startDate.time) / (24 * 60 * 60 * 1000))
    val avgDailyExpense = totalExpense.amount.toDouble() / daysInPeriod
    val avgMonthlyExpense = avgDailyExpense * 30
    val avgYearlyExpense = avgDailyExpense * 365

    // Calculate savings rate
    val savingsRate = if (totalIncome.amount.toDouble() > 0) {
        ((totalIncome.amount.toDouble() - totalExpense.amount.toDouble()) / totalIncome.amount.toDouble()) * 100
    } else {
        0.0
    }

    // Show dialogs if needed
    if (showSavingsRateInfo) {
        SavingsRateDialog(onDismiss = { showSavingsRateInfo = false })
    }

    if (state.showPeriodDialog) {
        PeriodSelectionDialog(
            selectedPeriod = state.periodType,
            startDate = state.startDate,
            endDate = state.endDate,
            onPeriodSelected = { periodType ->
                viewModel.handleIntent(ChartIntent.SetPeriodType(periodType))
                if (periodType != PeriodType.CUSTOM) {
                    viewModel.handleIntent(ChartIntent.HidePeriodDialog)
                }
            },
            onStartDateClick = {
                viewModel.handleIntent(ChartIntent.ShowStartDatePicker)
            },
            onEndDateClick = {
                viewModel.handleIntent(ChartIntent.ShowEndDatePicker)
            },
            onDismiss = {
                viewModel.handleIntent(ChartIntent.HidePeriodDialog)
            }
        )
    }

    if (state.showStartDatePicker) {
        DatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { date ->
                viewModel.handleIntent(ChartIntent.UpdateStartDate(date))
                viewModel.handleIntent(ChartIntent.HideStartDatePicker)
            },
            onDismiss = {
                viewModel.handleIntent(ChartIntent.HideStartDatePicker)
            }
        )
    }

    if (state.showEndDatePicker) {
        DatePickerDialog(
            initialDate = state.endDate,
            onDateSelected = { date ->
                viewModel.handleIntent(ChartIntent.UpdateEndDate(date))
                viewModel.handleIntent(ChartIntent.HideEndDatePicker)
            },
            onDismiss = {
                viewModel.handleIntent(ChartIntent.HideEndDatePicker)
            }
        )
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
                            text = stringResource(R.string.charts_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Сбрасываем даты перед выходом с экрана
                        viewModel.handleIntent(ChartIntent.ResetDateFilter)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Оставляем только кнопку выбора периода, убираем кнопку фильтра
                    IconButton(onClick = { viewModel.handleIntent(ChartIntent.ShowPeriodDialog) }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = stringResource(R.string.select_period)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading indicator if data is loading
            if (state.isLoading) {
                Timber.tag("FinanceChart").d("Loading state")
                LoadingIndicator()
            } else if (state.error != null) {
                Timber.tag("FinanceChart").d("Error state: ${state.error}")
                // Show error message if there's an error
                ErrorContent(
                    message = state.error ?: stringResource(R.string.unknown_error),
                    onRetry = { viewModel.handleIntent(ChartIntent.LoadTransactions) }
                )
            } else if (filteredTransactions.isEmpty()) {
                Timber.tag("FinanceChart").d("Empty state")
                // Show empty state if there are no transactions
                EmptyContent()
            } else {
                Timber.tag("FinanceChart").d("Content state")
                // Show content if there are transactions
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = dimensionResource(R.dimen.spacing_large))
                ) {
                    // Summary section
                    SummarySection(
                        income = totalIncome,
                        expense = totalExpense,
                        period = periodText,
                        onPeriodClick = { viewModel.handleIntent(ChartIntent.ShowPeriodDialog) }
                    )

                    // Pie chart section
                    PieChartSection(
                        showExpenses = state.showExpenses,
                        onShowExpensesChange = { showExpenses ->
                            viewModel.handleIntent(ChartIntent.ToggleExpenseView(showExpenses))
                        },
                        filteredTransactions = filteredTransactions,
                        viewModel = viewModel
                    )

                    // Daily expenses section
                    DailyExpensesChart(
                        dailyExpenses = state.dailyExpenses,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Statistics section
                    StatisticsSection(
                        avgDailyExpense = avgDailyExpense,
                        avgMonthlyExpense = avgMonthlyExpense,
                        avgYearlyExpense = avgYearlyExpense,
                        savingsRate = savingsRate,
                        onSavingsRateInfoClick = { showSavingsRateInfo = true }
                    )
                }
            }
        }
    }
}
