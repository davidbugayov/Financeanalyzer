package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.components.DailyExpensesSection
import com.davidbugayov.financeanalyzer.presentation.chart.components.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.chart.components.PeriodSelector
import com.davidbugayov.financeanalyzer.presentation.chart.components.PieChartSection
import com.davidbugayov.financeanalyzer.presentation.chart.components.SavingsRateDialog
import com.davidbugayov.financeanalyzer.presentation.chart.components.StatisticsSection
import com.davidbugayov.financeanalyzer.presentation.chart.components.SummarySection
import com.davidbugayov.financeanalyzer.presentation.chart.state.ChartIntent
import com.davidbugayov.financeanalyzer.presentation.components.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.presentation.components.LoadingIndicator
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
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

    // UI state
    var showSavingsRateInfo by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showPeriodSelection by remember { mutableStateOf(false) }

    // Track the currently selected period
    val defaultPeriod = stringResource(R.string.month)
    var selectedPeriod by remember { mutableStateOf(defaultPeriod) }

    // Format dates for display
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val periodText = "${dateFormat.format(state.startDate)} - ${dateFormat.format(state.endDate)}"

    // Define period options
    val periodOptions = listOf(
        stringResource(R.string.week),
        stringResource(R.string.month),
        stringResource(R.string.period_quarter),
        stringResource(R.string.year),
        stringResource(R.string.all_time)
    )

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

    if (showPeriodSelection) {
        PeriodSelectionDialog(
            startDate = state.startDate,
            endDate = state.endDate,
            onStartDateClick = { showStartDatePicker = true },
            onEndDateClick = { showEndDatePicker = true },
            onDismiss = { showPeriodSelection = false }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { date ->
                viewModel.handleIntent(ChartIntent.UpdateStartDate(date))
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = state.endDate,
            onDateSelected = { date ->
                viewModel.handleIntent(ChartIntent.UpdateEndDate(date))
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.charts_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showPeriodSelection = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
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
                    // Period selector
                    PeriodSelector(
                        periodOptions = periodOptions,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { period, startDate, endDate ->
                            selectedPeriod = period
                            val startDateJava = Date.from(startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
                            val endDateJava = Date.from(endDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
                            viewModel.handleIntent(ChartIntent.UpdateDateRange(startDateJava, endDateJava))
                        },
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_large))
                    )

                    // Summary section
                    SummarySection(
                        income = totalIncome,
                        expense = totalExpense,
                        period = periodText
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
                    DailyExpensesSection(
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
