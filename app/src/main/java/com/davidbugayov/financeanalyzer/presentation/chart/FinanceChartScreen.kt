package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.chart.components.CategoryList
import com.davidbugayov.financeanalyzer.presentation.chart.components.CategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.components.DailyExpensesChart
import com.davidbugayov.financeanalyzer.presentation.components.LoadingIndicator
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Улучшенный экран с финансовой аналитикой и графиками.
 * Отображает различные графики на основе транзакций пользователя.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceChartScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Convert dimension resource to sp
    val textSizeLarge = dimensionResource(id = R.dimen.text_size_large).value.sp

    // Состояние для диалога с информацией о норме сбережений
    var showSavingsRateInfo by remember { mutableStateOf(false) }

    // Состояние для диалогов выбора даты
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Состояние для диалога выбора периода
    var showPeriodDialog by remember { mutableStateOf(false) }

    // Форматирование дат
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val periodText = remember(state.startDate, state.endDate) {
        "${dateFormat.format(state.startDate)} - ${dateFormat.format(state.endDate)}"
    }

    val weekString = stringResource(R.string.week)
    val monthString = stringResource(R.string.month)
    val yearString = stringResource(R.string.year)
    val allTimeString = stringResource(R.string.all_time)

    // Фильтрация транзакций по выбранному периоду
    val filteredTransactions = remember(state.transactions, state.startDate, state.endDate) {
        // Устанавливаем начало дня для startDate
        val startCalendar = Calendar.getInstance()
        startCalendar.time = state.startDate
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        val start = startCalendar.time

        // Устанавливаем конец дня для endDate
        val endCalendar = Calendar.getInstance()
        endCalendar.time = state.endDate
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)
        val end = endCalendar.time

        state.transactions.filter {
            (it.date.after(start) || it.date == start) &&
                    (it.date.before(end) || it.date == end)
        }
    }

    // Расчет сумм для отфильтрованных транзакций
    val filteredIncome = filteredTransactions
        .filter { !it.isExpense }
        .map { it.amount }
        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

    val filteredExpense = filteredTransactions
        .filter { it.isExpense }
        .map { it.amount }
        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

    val periodOptions = listOf(
        weekString,
        monthString,
        yearString,
        allTimeString
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.charts_title),
                        fontSize = textSizeLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showPeriodDialog = true }
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.cd_select_period)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = dimensionResource(id = R.dimen.spacing_normal),
                    end = dimensionResource(id = R.dimen.spacing_normal),
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.spacing_large)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_transactions, state.error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_large)),
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.handleIntent(ChartIntent.LoadTransactions) }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else if (state.transactions.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.spacing_large)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_data_to_display),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (!state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Секция с общей информацией
                    SummarySection(
                        income = filteredIncome,
                        expense = filteredExpense,
                        period = periodText
                    )

                    // Улучшенный переключатель периода
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.spacing_large), vertical = dimensionResource(R.dimen.spacing_medium)),
                        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.spacing_medium)),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            var selectedPeriod by remember { mutableStateOf(periodOptions[0]) }

                            periodOptions.forEach { period ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (period == selectedPeriod)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            selectedPeriod = period
                                            // Обновляем даты в зависимости от выбранного периода
                                            when (period) {
                                                weekString -> {
                                                    val endDate = Calendar.getInstance().time
                                                    val startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.DAY_OF_YEAR, -7)
                                                    }.time
                                                    viewModel.handleIntent(ChartIntent.UpdateDateRange(startDate, endDate))
                                                }
                                                monthString -> {
                                                    val endDate = Calendar.getInstance().time
                                                    val startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.MONTH, -1)
                                                    }.time
                                                    viewModel.handleIntent(ChartIntent.UpdateDateRange(startDate, endDate))
                                                }
                                                yearString -> {
                                                    val endDate = Calendar.getInstance().time
                                                    val startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.YEAR, -1)
                                                    }.time
                                                    viewModel.handleIntent(ChartIntent.UpdateDateRange(startDate, endDate))
                                                }
                                                allTimeString -> {
                                                    val endDate = Calendar.getInstance().time
                                                    val startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.YEAR, -10)
                                                    }.time
                                                    viewModel.handleIntent(ChartIntent.UpdateDateRange(startDate, endDate))
                                                }
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = period,
                                        color = if (period == selectedPeriod)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = if (period == selectedPeriod) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Секция с круговой диаграммой
                    PieChartSection(
                        showExpenses = state.showExpenses,
                        onShowExpensesChange = { viewModel.handleIntent(ChartIntent.ToggleExpenseView(it)) },
                        filteredTransactions = filteredTransactions,
                        viewModel = viewModel
                    )

                    // Секция с графиком расходов по дням
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.spacing_large)),
                        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.spacing_large))
                        ) {
                            Text(
                                text = stringResource(R.string.daily_expenses),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                            Text(
                                text = stringResource(R.string.tap_bar_for_details),
                                fontSize = textSizeLarge,
                                fontWeight = FontWeight.Bold,
                                color = LocalExpenseColor.current
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                            val expensesByDay = viewModel.getExpensesByDay(30, filteredTransactions)
                            if (expensesByDay.isNotEmpty()) {
                                // Добавляем горизонтальную прокрутку
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    DailyExpensesChart(
                                        data = expensesByDay,
                                        modifier = Modifier
                                            .width(max(expensesByDay.size * 40, 350).dp)
                                            .height(dimensionResource(R.dimen.chart_height_large))
                                    )
                                }
                            } else {
                                EmptyDataMessage(stringResource(R.string.no_expenses_recent_days))
                            }
                        }
                    }

                    // Секция со статистикой средних значений
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.spacing_large)),
                        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.spacing_large))
                        ) {
                            Text(
                                text = stringResource(R.string.average_values),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                            Text(
                                text = stringResource(R.string.analyze_expenses_for_budget),
                                fontSize = textSizeLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                            // Расчет средних значений
                            val expensesByDay = viewModel.getExpensesByDay(30, filteredTransactions)
                            val totalExpenses = expensesByDay.values.sumOf { it.totalExpense.amount.toDouble() }
                            val daysCount = expensesByDay.size.coerceAtLeast(1)
                            val avgDailyExpense = totalExpenses / daysCount
                            val avgMonthlyExpense = avgDailyExpense * 30
                            val avgYearlyExpense = avgDailyExpense * 365

                            // Расчет нормы сбережений
                            val totalIncome = filteredTransactions
                                .filter { !it.isExpense }
                                .sumOf { it.amount.amount.toDouble() }

                            val totalExpense = filteredTransactions
                                .filter { it.isExpense }
                                .sumOf { it.amount.amount.toDouble() }

                            val savingsRate = if (totalIncome > 0) {
                                ((totalIncome - totalExpense) / totalIncome * 100)
                                    .coerceIn(0.0, 100.0)
                            } else 0.0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.average_daily),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Money(avgDailyExpense).format(false),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LocalExpenseColor.current
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = stringResource(R.string.average_monthly),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Money(avgMonthlyExpense).format(false),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LocalExpenseColor.current
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.average_yearly),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Money(avgYearlyExpense).format(false),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LocalExpenseColor.current
                                    )
                                }

                                // Норма сбережений
                                if (totalIncome > 0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.savings_rate),
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.1f%%", savingsRate),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = LocalIncomeColor.current
                                            )
                                            IconButton(
                                                onClick = { showSavingsRateInfo = true },
                                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                                            ) {
                                                Icon(
                                                    Icons.Default.Info,
                                                    contentDescription = "Информация о норме сбережений",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Добавляем визуальное представление нормы сбережений
                            if (totalIncome > 0) {
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                                Text(
                                    text = stringResource(R.string.savings_percentage),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight((savingsRate / 100).coerceAtLeast(0.01).toFloat())
                                            .fillMaxHeight()
                                            .background(LocalIncomeColor.current)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight((1 - (savingsRate / 100)).coerceAtLeast(0.01).toFloat())
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }

                                // Добавляем подсказки для интерпретации
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when {
                                            savingsRate < 10 -> stringResource(R.string.low)
                                            savingsRate < 20 -> stringResource(R.string.medium)
                                            else -> stringResource(R.string.good)
                                        },
                                        fontSize = 12.sp,
                                        color = when {
                                            savingsRate < 10 -> MaterialTheme.colorScheme.error
                                            savingsRate < 20 -> MaterialTheme.colorScheme.tertiary
                                            else -> LocalIncomeColor.current
                                        }
                                    )
                                    Text(
                                        text = stringResource(R.string.recommended_savings_rate),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                }
            }

            // Индикатор загрузки
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            // Диалог с информацией о норме сбережений
            if (showSavingsRateInfo) {
                AlertDialog(
                    onDismissRequest = { showSavingsRateInfo = false },
                    title = {
                        Text(
                            text = "Норма сбережений",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.savings_rate_description),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.how_calculated),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.savings_rate_formula),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.recommended_values),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.recommended_values_list),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.importance),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.importance_list),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.advice),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.advice_text),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.understood),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSavingsRateInfo = false }) {
                            Text(stringResource(R.string.understood))
                        }
                    }
                )
            }

            // Диалог выбора периода
            if (showPeriodDialog) {
                AlertDialog(
                    onDismissRequest = { showPeriodDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.select_period),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Строка выбора начальной даты
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showPeriodDialog = false
                                        showStartDatePicker = true
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.from),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.width(dimensionResource(R.dimen.width_large))
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimensionResource(R.dimen.height_small)),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = dimensionResource(R.dimen.padding_small)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(state.startDate),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // Строка выбора конечной даты
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showPeriodDialog = false
                                        showEndDatePicker = true
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.to),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.width(dimensionResource(R.dimen.width_large))
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimensionResource(R.dimen.height_small)),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = dimensionResource(R.dimen.padding_small)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(state.endDate),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPeriodDialog = false }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }

            // Диалог выбора начальной даты
            if (showStartDatePicker) {
                DatePickerDialog(
                    initialDate = state.startDate,
                    onDateSelected = { date ->
                        viewModel.handleIntent(ChartIntent.UpdateStartDate(date))
                        showStartDatePicker = false
                        if (state.endDate.before(date)) {
                            showEndDatePicker = true
                        }
                    },
                    onDismissRequest = { showStartDatePicker = false }
                )
            }

            // Диалог выбора конечной даты
            if (showEndDatePicker) {
                DatePickerDialog(
                    initialDate = state.endDate,
                    onDateSelected = { date ->
                        if (date.before(state.startDate)) {
                            // Если конечная дата раньше начальной, меняем их местами
                            viewModel.handleIntent(ChartIntent.UpdateDateRange(date, state.startDate))
                        } else {
                            viewModel.handleIntent(ChartIntent.UpdateEndDate(date))
                        }
                        showEndDatePicker = false
                    },
                    onDismissRequest = { showEndDatePicker = false }
                )
            }
        }
    }
}

/**
 * Секция с общей информацией о доходах и расходах
 */
@Composable
private fun SummarySection(
    income: Money,
    expense: Money,
    period: String
) {
    val balance = income - expense
    val incomeColor = LocalIncomeColor.current  // Зеленый цвет для доходов
    val expenseColor = LocalExpenseColor.current  // Красный цвет для расходов
    val balanceColor = if (balance.isNegative()) expenseColor else incomeColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_large)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = period,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = stringResource(R.string.currency_format, balance.format(false)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.income_currency_format, income.format(false)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = incomeColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.expense_currency_format, expense.format(false)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = expenseColor
                    )
                }
            }

            // Добавляем визуальное представление соотношения доходов и расходов
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            val totalAmount = income.amount.toDouble() + expense.amount.toDouble()
            val incomeRatio = if (totalAmount > 0) income.amount.toDouble() / totalAmount else 0.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
            ) {
                Box(
                    modifier = Modifier
                        .weight(incomeRatio.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(incomeColor)
                )
                Box(
                    modifier = Modifier
                        .weight((1 - incomeRatio).toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(expenseColor)
                )
            }
        }
    }
}

/**
 * Сообщение об отсутствии данных
 */
@Composable
private fun EmptyDataMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Диалог выбора даты.
 * Использует стандартный DatePicker из Material3.
 *
 * @param initialDate Начальная дата для отображения в календаре
 * @param onDateSelected Callback, вызываемый при выборе даты
 * @param onDismiss Callback для закрытия диалога
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismissRequest: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Date(it))
                    }
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Секция с круговой диаграммой, отображающая распределение доходов или расходов по категориям
 */
@Composable
private fun PieChartSection(
    showExpenses: Boolean,
    onShowExpensesChange: (Boolean) -> Unit,
    filteredTransactions: List<Transaction>,
    viewModel: ChartViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_large)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = if (showExpenses)
                    stringResource(R.string.chart_expenses)
                else
                    stringResource(R.string.chart_income),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (showExpenses) LocalExpenseColor.current else LocalIncomeColor.current
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = if (showExpenses)
                    stringResource(R.string.expense_type)
                else
                    stringResource(R.string.income_type),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (showExpenses) LocalExpenseColor.current else LocalIncomeColor.current
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Переключатель доходы/расходы в стиле CoinKeeper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.show_expenses),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (showExpenses) LocalExpenseColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onShowExpensesChange(true) }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.show_income),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (!showExpenses) LocalIncomeColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onShowExpensesChange(false) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Визуальное представление выбора
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
            ) {
                Box(
                    modifier = Modifier
                        .weight(if (showExpenses) 0.7f else 0.3f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                )
                Box(
                    modifier = Modifier
                        .weight(if (!showExpenses) 0.7f else 0.3f)
                        .fillMaxHeight()
                        .background(LocalExpenseColor.current)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Отображение соответствующей диаграммы
            if (showExpenses) {
                val expensesByCategory = viewModel.getExpensesByCategory(filteredTransactions)
                if (expensesByCategory.isNotEmpty()) {
                    // Отображаем круговую диаграмму
                    CategoryPieChart(
                        data = expensesByCategory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.chart_height_large))
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    // Отображаем список категорий
                    CategoryList(
                        data = expensesByCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EmptyDataMessage(stringResource(R.string.no_expense_data))
                }
            } else {
                val incomeByCategory = viewModel.getIncomeByCategory(filteredTransactions)
                if (incomeByCategory.isNotEmpty()) {
                    // Отображаем круговую диаграмму
                    CategoryPieChart(
                        data = incomeByCategory,
                        isIncome = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.chart_height_large))
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    // Отображаем список категорий
                    CategoryList(
                        data = incomeByCategory,
                        isIncome = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EmptyDataMessage(stringResource(R.string.no_income_data))
                }
            }
        }
    }
} 