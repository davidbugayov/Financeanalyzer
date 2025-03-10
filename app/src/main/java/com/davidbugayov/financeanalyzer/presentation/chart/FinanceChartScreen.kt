package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

    // Состояние для выбора периода
    var selectedPeriod by remember { mutableStateOf("Месяц") }
    val periodOptions = listOf("Неделя", "Месяц", "Квартал", "Год", "Все время")

    // Состояние для выбора типа данных
    var showExpenses by remember { mutableStateOf(true) }

    // Состояние для отображения меню
    var showMenu by remember { mutableStateOf(false) }

    // Форматирование дат
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    val currentDate = Date()
    val formattedDate = dateFormat.format(currentDate)

    // Фильтрация транзакций по выбранному периоду
    val filteredTransactions = remember(transactions, selectedPeriod) {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis

        when (selectedPeriod) {
            "Неделя" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                transactions.filter { it.date.time >= weekAgo }
            }
            "Месяц" -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.timeInMillis
                transactions.filter { it.date.time >= monthAgo }
            }
            "Квартал" -> {
                calendar.add(Calendar.MONTH, -3)
                val quarterAgo = calendar.timeInMillis
                transactions.filter { it.date.time >= quarterAgo }
            }
            "Год" -> {
                calendar.add(Calendar.YEAR, -1)
                val yearAgo = calendar.timeInMillis
                transactions.filter { it.date.time >= yearAgo }
            }
            else -> transactions // "Все время"
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
                        fontWeight = FontWeight.Medium
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
                    IconButton(onClick = { /* Открыть диалог выбора даты */ }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Выбрать период"
                        )
                    }
                    IconButton(onClick = { /* Открыть фильтры */ }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Фильтры"
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Дополнительные опции"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Экспорт данных") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Настройки графиков") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Сравнить периоды") },
                                onClick = { showMenu = false }
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
                        period = when (selectedPeriod) {
                            "Неделя" -> "За последнюю неделю"
                            "Месяц" -> "За последний месяц"
                            "Квартал" -> "За последний квартал"
                            "Год" -> "За последний год"
                            else -> "За все время"
                        }
                    )

                    // Добавляем прогресс-бар в стиле CoinKeeper
                    val totalAmount = filteredIncome.amount.toDouble() + filteredExpense.amount.toDouble()
                    val incomeRatio = if (totalAmount > 0) filteredIncome.amount.toDouble() / totalAmount else 0.0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF5D4037))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(incomeRatio.toFloat())
                                .background(Color(0xFF66BB6A))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1 - incomeRatio.toFloat())
                                .align(Alignment.TopEnd)
                                .background(Color(0xFFEF5350))
                        )
                    }

                    // Переключатель периода в стиле CoinKeeper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        periodOptions.forEach { period ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (period == selectedPeriod) Color(0xFF5D4037) else Color(0xFF1C1B1F).copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedPeriod = period }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = period,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (period == selectedPeriod) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Секция с круговой диаграммой
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF5D4037)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (showExpenses)
                                            "Структура расходов"
                                        else
                                            "Структура доходов",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (showExpenses)
                                            "Распределение ваших расходов по категориям"
                                        else
                                            "Распределение ваших доходов по источникам",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(
                                    onClick = { /* Показать информацию */ }
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Информация",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Переключатель доходы/расходы в стиле CoinKeeper
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Кнопка "Расходы"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (showExpenses) Color(0xFF8D6E63) else Color(0xFF5D4037).copy(alpha = 0.5f)
                                        )
                                        .clickable { showExpenses = true }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Расходы",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = if (showExpenses) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                // Кнопка "Доходы"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (!showExpenses) Color(0xFF8D6E63) else Color(0xFF5D4037).copy(alpha = 0.5f)
                                        )
                                        .clickable { showExpenses = false }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Доходы",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = if (!showExpenses) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF5D4037)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Динамика по месяцам",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Сравнение доходов и расходов за последние месяцы",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(
                                    onClick = { /* Показать информацию */ }
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Информация",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF5D4037)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Ежедневные траты",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "График ваших расходов по дням для анализа трат",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(
                                    onClick = { /* Показать информацию */ }
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Информация",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val expensesByDay = viewModel.getExpensesByDay(30, filteredTransactions)
                            if (expensesByDay.isNotEmpty()) {
                                DailyExpensesChart(
                                    data = expensesByDay,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(350.dp)
                                )
                            } else {
                                EmptyDataMessage(stringResource(R.string.no_expenses_recent_days))
                            }
                        }
                    }

                    // Секция со статистикой средних значений
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF5D4037)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Средние показатели",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Анализ ваших средних трат для планирования бюджета",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(
                                    onClick = { /* Показать информацию */ }
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Информация",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Расчет средних значений
                            val expensesByDay = viewModel.getExpensesByDay(30, filteredTransactions)
                            val totalExpenses = expensesByDay.values.sumOf { it.totalExpense.amount.toDouble() }
                            val daysCount = expensesByDay.size.coerceAtLeast(1)
                            val avgDailyExpense = totalExpenses / daysCount
                            val avgMonthlyExpense = avgDailyExpense * 30
                            val avgYearlyExpense = avgDailyExpense * 365

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.average_daily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Money(avgDailyExpense).format(false),
                                    color = LocalExpenseColor.current,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.average_monthly),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Money(avgMonthlyExpense).format(false),
                                    color = LocalExpenseColor.current,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.average_yearly),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Money(avgYearlyExpense).format(false),
                                    color = LocalExpenseColor.current,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Норма сбережений
                            val totalIncome = filteredTransactions
                                .filter { !it.isExpense }
                                .sumOf { it.amount.amount.toDouble() }

                            val totalExpense = filteredTransactions
                                .filter { it.isExpense }
                                .sumOf { it.amount.amount.toDouble() }

                            if (totalIncome > 0) {
                                val savingsRate = ((totalIncome - totalExpense) / totalIncome * 100)
                                    .coerceIn(0.0, 100.0)

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.savings_rate),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Процент дохода, который вы сохраняете",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format("%.1f%%", savingsRate),
                                            color = LocalIncomeColor.current,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = { showSavingsRateInfo = true }
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

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        progress = { (savingsRate / 100).toFloat() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = LocalIncomeColor.current,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                        .weight(incomeRatio.toFloat())
                        .fillMaxHeight()
                        .background(incomeColor)
                )
                Box(
                    modifier = Modifier
                        .weight((1 - incomeRatio).toFloat())
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