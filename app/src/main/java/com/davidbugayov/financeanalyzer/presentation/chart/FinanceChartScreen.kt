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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.presentation.chart.components.CategoryList
import com.davidbugayov.financeanalyzer.presentation.chart.components.CategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.components.DailyExpensesChart
import com.davidbugayov.financeanalyzer.presentation.chart.components.MonthlyComparisonChart
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
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scrollState = rememberScrollState()
    val layoutDirection = LocalLayoutDirection.current

    // Состояние для диалога с информацией о норме сбережений
    var showSavingsRateInfo by remember { mutableStateOf(false) }

    // Состояние для диалогов выбора даты
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Состояние для дат
    var startDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time) }
    var endDate by remember { mutableStateOf(Calendar.getInstance().time) }

    // Состояние для фильтров
    var showIncomeTransactions by remember { mutableStateOf(true) }
    var showExpenseTransactions by remember { mutableStateOf(true) }

    // Состояние для категорий
    val allCategories = remember {
        listOf("Продукты", "Транспорт", "Развлечения", "Здоровье", "Одежда", "Жильё", "Связь", "Прочее")
    }
    var selectedCategories by remember { mutableStateOf(allCategories.toSet()) }
    var selectAllCategories by remember { mutableStateOf(true) }

    // Состояние для выбора типа данных
    var showExpenses by remember { mutableStateOf(true) }

    // Состояние для отображения меню
    var showMenu by remember { mutableStateOf(false) }

    // Состояние для диалога выбора периода
    var showPeriodDialog by remember { mutableStateOf(false) }

    // Форматирование дат
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val periodText = remember(startDate, endDate) {
        "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
    }

    // Фильтрация транзакций по выбранному периоду
    val filteredTransactions = remember(transactions, startDate, endDate) {
        // Устанавливаем начало дня для startDate
        val startCalendar = Calendar.getInstance()
        startCalendar.time = startDate
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        val start = startCalendar.time

        // Устанавливаем конец дня для endDate
        val endCalendar = Calendar.getInstance()
        endCalendar.time = endDate
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)
        val end = endCalendar.time

        transactions.filter {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.charts_title),
                        fontSize = 18.sp,
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
                                contentDescription = "Выбрать период"
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
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_transactions, error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.loadTransactions() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else if (transactions.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_data_to_display),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (!isLoading) {
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val periodOptions = listOf("Неделя", "Месяц", "Год", "Все")
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
                                                "Неделя" -> {
                                                    endDate = Calendar.getInstance().time
                                                    startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.DAY_OF_YEAR, -7)
                                                    }.time
                                                }
                                                "Месяц" -> {
                                                    endDate = Calendar.getInstance().time
                                                    startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.MONTH, -1)
                                                    }.time
                                                }
                                                "Год" -> {
                                                    endDate = Calendar.getInstance().time
                                                    startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.YEAR, -1)
                                                    }.time
                                                }
                                                "Все" -> {
                                                    endDate = Calendar.getInstance().time
                                                    startDate = Calendar.getInstance().apply {
                                                        time = endDate
                                                        add(Calendar.YEAR, -10) // Условно "все время" - 10 лет
                                                    }.time
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (showExpenses)
                                    "Структура расходов"
                                else
                                    "Структура доходов",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (showExpenses)
                                    "Распределение ваших расходов по категориям"
                                else
                                    "Распределение ваших доходов по источникам",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showExpenses) LocalExpenseColor.current else LocalIncomeColor.current
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Переключатель доходы/расходы в стиле CoinKeeper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Расходы",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Показать расходы",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (showExpenses) LocalExpenseColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.clickable { showExpenses = true }
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Доходы",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Показать доходы",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (!showExpenses) LocalIncomeColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.clickable { showExpenses = false }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Визуальное представление выбора
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(if (showExpenses) 0.7f else 0.3f)
                                        .fillMaxHeight()
                                        .background(LocalExpenseColor.current)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(if (!showExpenses) 0.7f else 0.3f)
                                        .fillMaxHeight()
                                        .background(LocalIncomeColor.current)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Отображение соответствующей диаграммы
                            if (showExpenses) {
                                val expensesByCategory = viewModel.getExpensesByCategory(filteredTransactions)
                                if (expensesByCategory.isNotEmpty()) {
                                    // Отображаем круговую диаграмму
                                    CategoryPieChart(
                                        data = expensesByCategory,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

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
                                            .height(280.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

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

                    // Секция с улучшенным графиком баланса по месяцам
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Динамика по месяцам",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Сравнение доходов и расходов за последние месяцы",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val transactionsByMonth = viewModel.getTransactionsByMonth(filteredTransactions)
                            if (transactionsByMonth.isNotEmpty()) {
                                MonthlyComparisonChart(
                                    data = transactionsByMonth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(350.dp)
                                )
                            } else {
                                EmptyDataMessage(stringResource(R.string.insufficient_data))
                            }
                        }
                    }

                    // Секция с графиком расходов по дням
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Ежедневные траты",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "График ваших расходов по дням для анализа трат",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalExpenseColor.current
                            )

                            Spacer(modifier = Modifier.height(16.dp))

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
                                            .height(350.dp)
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
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Средние показатели",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Анализ ваших средних трат для планирования бюджета",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

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

                            Spacer(modifier = Modifier.height(16.dp))

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
                                                text = String.format("%.1f%%", savingsRate),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = LocalIncomeColor.current
                                            )
                                            IconButton(
                                                onClick = { showSavingsRateInfo = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Info,
                                                    contentDescription = "Информация о норме сбережений",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Добавляем визуальное представление нормы сбережений
                            if (totalIncome > 0) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Процент дохода, который вы сохраняете",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight((savingsRate / 100).toFloat().coerceAtLeast(0.01f))
                                            .fillMaxHeight()
                                            .background(LocalIncomeColor.current)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight((1 - (savingsRate / 100)).toFloat().coerceAtLeast(0.01f))
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }

                                // Добавляем подсказки для интерпретации
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when {
                                            savingsRate < 10 -> "Низкая"
                                            savingsRate < 20 -> "Средняя"
                                            else -> "Хорошая"
                                        },
                                        fontSize = 12.sp,
                                        color = when {
                                            savingsRate < 10 -> Color(0xFFEF5350)
                                            savingsRate < 20 -> Color(0xFFFFA726)
                                            else -> Color(0xFF66BB6A)
                                        }
                                    )
                                    Text(
                                        text = "Рекомендуется: 20% и выше",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Индикатор загрузки
            AnimatedVisibility(
                visible = isLoading,
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
                                text = "Норма сбережений - это важный финансовый показатель, который показывает, какую часть вашего дохода вы откладываете.",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Как рассчитывается:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Норма сбережений = ((Доходы - Расходы) / Доходы) × 100%",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Рекомендуемые значения:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "• Низкая: менее 10%\n• Средняя: 10-20%\n• Хорошая: 20% и выше",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Почему это важно:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "• Помогает создать финансовую подушку безопасности\n" +
                                        "• Позволяет достигать долгосрочных финансовых целей\n" +
                                        "• Обеспечивает финансовую стабильность\n" +
                                        "• Защищает от непредвиденных расходов",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Совет:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Старайтесь поддерживать норму сбережений на уровне 20% и выше. Это поможет вам создать надежный финансовый фундамент и достичь ваших финансовых целей.",
                                fontSize = 14.sp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSavingsRateInfo = false }) {
                            Text("Понятно")
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
                            text = "Выберите период",
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
                                    text = "От",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.width(40.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(startDate),
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
                                    text = "До",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.width(40.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(endDate),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPeriodDialog = false }) {
                            Text("Закрыть")
                        }
                    }
                )
            }

            // Диалог выбора начальной даты
            if (showStartDatePicker) {
                DatePickerDialog(
                    initialDate = startDate,
                    onDateSelected = { date ->
                        startDate = date
                        showStartDatePicker = false
                        if (endDate.before(startDate)) {
                            showEndDatePicker = true
                        }
                    },
                    onDismiss = {
                        showStartDatePicker = false
                    }
                )
            }

            // Диалог выбора конечной даты
            if (showEndDatePicker) {
                DatePickerDialog(
                    initialDate = endDate,
                    onDateSelected = { date ->
                        if (date.before(startDate)) {
                            // Если конечная дата раньше начальной, меняем их местами
                            endDate = startDate
                            startDate = date
                        } else {
                            endDate = date
                        }
                        showEndDatePicker = false
                    },
                    onDismiss = {
                        showEndDatePicker = false
                    }
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
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = period,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.currency_format, balance.format(false)),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance.isNegative()) expenseColor else incomeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(16.dp))

            val totalAmount = income.amount.toDouble() + expense.amount.toDouble()
            val incomeRatio = if (totalAmount > 0) income.amount.toDouble() / totalAmount else 0.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
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
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Date(it))
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
} 