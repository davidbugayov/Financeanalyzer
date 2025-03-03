package com.davidbugayov.financeanalyzer.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import android.os.Bundle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit
) {
    val analytics = Firebase.analytics
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val layoutDirection = LocalLayoutDirection.current

    // Состояние для выбранного типа группировки
    var groupingType by remember { mutableStateOf(GroupingType.MONTH) }

    // Состояние для выбора периода
    var periodType by remember { mutableStateOf(PeriodType.MONTH) }
    var showPeriodDialog by remember { mutableStateOf(false) }

    // Состояние для выбора дат
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(
        Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time
    ) }
    var endDate by remember { mutableStateOf(Date()) }

    // Состояние для фильтрованных транзакций
    val filteredTransactions = remember(transactions, periodType, startDate, endDate) {
        filterTransactionsByPeriod(transactions, periodType, startDate, endDate)
    }

    // Добавляем логирование при изменении периода
    LaunchedEffect(periodType) {
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_NAME, "period_type")
            param(FirebaseAnalytics.Param.ITEM_ID, periodType.name)
        }
    }

    // Добавляем логирование при изменении группировки
    LaunchedEffect(groupingType) {
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_NAME, "grouping_type")
            param(FirebaseAnalytics.Param.ITEM_ID, groupingType.name)
        }
    }

    // Диалог выбора периода
    if (showPeriodDialog) {
        AlertDialog(
            onDismissRequest = { showPeriodDialog = false },
            title = { Text(stringResource(R.string.select_period)) },
            text = {
                Column {
                    PeriodRadioButton(
                        text = stringResource(R.string.period_all),
                        selected = periodType == PeriodType.ALL,
                        onClick = {
                            periodType = PeriodType.ALL
                            showPeriodDialog = false
                        }
                    )
                    PeriodRadioButton(
                        text = stringResource(R.string.period_month),
                        selected = periodType == PeriodType.MONTH,
                        onClick = {
                            periodType = PeriodType.MONTH
                            showPeriodDialog = false
                        }
                    )
                    PeriodRadioButton(
                        text = stringResource(R.string.period_quarter),
                        selected = periodType == PeriodType.QUARTER,
                        onClick = {
                            periodType = PeriodType.QUARTER
                            showPeriodDialog = false
                        }
                    )
                    PeriodRadioButton(
                        text = stringResource(R.string.period_half_year),
                        selected = periodType == PeriodType.HALF_YEAR,
                        onClick = {
                            periodType = PeriodType.HALF_YEAR
                            showPeriodDialog = false
                        }
                    )
                    PeriodRadioButton(
                        text = stringResource(R.string.period_year),
                        selected = periodType == PeriodType.YEAR,
                        onClick = {
                            periodType = PeriodType.YEAR
                            showPeriodDialog = false
                        }
                    )
                    PeriodRadioButton(
                        text = stringResource(R.string.period_custom),
                        selected = periodType == PeriodType.CUSTOM,
                        onClick = {
                            periodType = PeriodType.CUSTOM
                        }
                    )

                    if (periodType == PeriodType.CUSTOM) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Код для строки с начальной датой
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { showStartDatePicker = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.from_date).split(":")[0],
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(startDate),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = stringResource(R.string.select_start_date)
                                )
                            }
                        }

                        // Код для строки с конечной датой
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { showEndDatePicker = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.to_date).split(":")[0],
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endDate),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = stringResource(R.string.select_end_date)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPeriodDialog = false }) {
                    Text(stringResource(if (periodType == PeriodType.CUSTOM) R.string.apply else R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPeriodDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Диалог выбора начальной даты
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Date(it)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Диалог выбора конечной даты
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = Date(it)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                            text = stringResource(R.string.history_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // Кнопка выбора периода
                    IconButton(onClick = { showPeriodDialog = true }) {
                        Icon(Icons.Default.FilterAlt, contentDescription = stringResource(R.string.select_period))
                    }
                },
                modifier = Modifier.height(56.dp)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Фильтры группировки
                GroupingChips(
                    currentGrouping = groupingType,
                    onGroupingSelected = { groupingType = it }
                )

                // Отображение выбранного периода
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            when (periodType) {
                                PeriodType.ALL -> R.string.period_all
                                PeriodType.MONTH -> R.string.period_month
                                PeriodType.QUARTER -> R.string.period_quarter
                                PeriodType.HALF_YEAR -> R.string.period_half_year
                                PeriodType.YEAR -> R.string.period_year
                                PeriodType.CUSTOM -> R.string.period_custom
                            }
                        ),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    if (periodType == PeriodType.CUSTOM) {
                        Text(
                            text = " (${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(startDate)} - ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endDate)})",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (error != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: stringResource(R.string.error_occurred),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.loadTransactions() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                } else if (filteredTransactions.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_transactions),
                            color = Color.Gray
                        )
                    }
                } else if (!isLoading) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        // Группируем транзакции в зависимости от выбранного типа группировки
                        val groupedTransactions = when (groupingType) {
                            GroupingType.DAY -> groupTransactionsByDay(filteredTransactions)
                            GroupingType.WEEK -> groupTransactionsByWeek(filteredTransactions)
                            GroupingType.MONTH -> groupTransactionsByMonth(filteredTransactions)
                        }

                        groupedTransactions.forEach { (period, transactionsInPeriod) ->
                            item {
                                GroupHeader(
                                    period = period,
                                    transactions = transactionsInPeriod
                                )
                            }

                            items(transactionsInPeriod) { transaction ->
                                TransactionHistoryItem(transaction = transaction)
                                HorizontalDivider()
                            }
                        }
                    }
                }

                // Индикатор загрузки
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения радиокнопки выбора периода
 */
@Composable
fun PeriodRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Заголовок группы транзакций с суммой
 */
@Composable
fun GroupHeader(period: String, transactions: List<Transaction>) {
    val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val balance = income - expense

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = period,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    R.string.income_label,
                    stringResource(R.string.currency_format, String.format("%.2f", income))
                ),
                fontSize = 12.sp,
                color = Color(0xFF4CAF50)
            )

            Text(
                text = stringResource(
                    R.string.expense_label,
                    stringResource(R.string.currency_format, String.format("%.2f", expense))
                ),
                fontSize = 12.sp,
                color = Color(0xFFF44336)
            )

            Text(
                text = stringResource(
                    R.string.balance_label,
                    stringResource(R.string.currency_format, String.format("%.2f", balance))
                ),
                fontSize = 12.sp,
                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Компонент с фильтрами группировки транзакций
 */
@Composable
fun GroupingChips(
    currentGrouping: GroupingType,
    onGroupingSelected: (GroupingType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentGrouping == GroupingType.DAY,
            onClick = { onGroupingSelected(GroupingType.DAY) },
            label = { Text(stringResource(R.string.group_by_day)) }
        )

        FilterChip(
            selected = currentGrouping == GroupingType.WEEK,
            onClick = { onGroupingSelected(GroupingType.WEEK) },
            label = { Text(stringResource(R.string.group_by_week)) }
        )

        FilterChip(
            selected = currentGrouping == GroupingType.MONTH,
            onClick = { onGroupingSelected(GroupingType.MONTH) },
            label = { Text(stringResource(R.string.group_by_month)) }
        )
    }
}

/**
 * Элемент списка транзакций в истории.
 */
@Composable
fun TransactionHistoryItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(
                    R.string.category_date_format,
                    transaction.category,
                    dateFormat.format(transaction.date)
                ),
                fontSize = 12.sp,
                color = Color.Gray
            )
            transaction.note?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Text(
            text = if (transaction.isExpense)
                stringResource(R.string.expense_currency_format, String.format("%.2f", transaction.amount))
            else
                stringResource(R.string.income_currency_format, String.format("%.2f", transaction.amount)),
            color = if (transaction.isExpense) Color(0xFFF44336) else Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Перечисление для типов группировки транзакций
 */
enum class GroupingType {
    DAY, WEEK, MONTH
}

/**
 * Перечисление для типов периодов
 */
enum class PeriodType {
    ALL, MONTH, QUARTER, HALF_YEAR, YEAR, CUSTOM
}

/**
 * Фильтрует транзакции по выбранному периоду
 */
private fun filterTransactionsByPeriod(
    transactions: List<Transaction>,
    periodType: PeriodType,
    startDate: Date? = null,
    endDate: Date? = null
): List<Transaction> {
    val calendar = Calendar.getInstance()
    val currentDate = calendar.time

    return when (periodType) {
        PeriodType.ALL -> transactions
        PeriodType.MONTH -> {
            calendar.add(Calendar.MONTH, -1)
            val monthAgo = calendar.time
            transactions.filter { it.date.after(monthAgo) || it.date == monthAgo }
        }
        PeriodType.QUARTER -> {
            calendar.add(Calendar.MONTH, -3)
            val quarterAgo = calendar.time
            transactions.filter { it.date.after(quarterAgo) || it.date == quarterAgo }
        }
        PeriodType.HALF_YEAR -> {
            calendar.add(Calendar.MONTH, -6)
            val halfYearAgo = calendar.time
            transactions.filter { it.date.after(halfYearAgo) || it.date == halfYearAgo }
        }
        PeriodType.YEAR -> {
            calendar.add(Calendar.YEAR, -1)
            val yearAgo = calendar.time
            transactions.filter { it.date.after(yearAgo) || it.date == yearAgo }
        }
        PeriodType.CUSTOM -> {
            if (startDate != null && endDate != null) {
                // Устанавливаем конец дня для конечной даты
                val endCalendar = Calendar.getInstance()
                endCalendar.time = endDate
                endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                endCalendar.set(Calendar.MINUTE, 59)
                endCalendar.set(Calendar.SECOND, 59)

                transactions.filter {
                    (it.date.after(startDate) || it.date == startDate) &&
                            (it.date.before(endCalendar.time) || it.date == endCalendar.time)
                }
            } else {
                transactions
            }
        }
    }
}

/**
 * Группирует транзакции по дням
 */
private fun groupTransactionsByDay(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    return transactions
        .sortedByDescending { it.date }
        .groupBy { dateFormat.format(it.date).replaceFirstChar { it.uppercase() } }
}

/**
 * Группирует транзакции по неделям
 */
private fun groupTransactionsByWeek(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val calendar = Calendar.getInstance()
    val result = mutableMapOf<String, MutableList<Transaction>>()

    // Сортируем транзакции по дате (от новых к старым)
    val sortedTransactions = transactions.sortedByDescending { it.date }

    for (transaction in sortedTransactions) {
        calendar.time = transaction.date

        // Получаем номер недели и год
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        // Определяем первый и последний день недели
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val firstDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val lastDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)

        // Формируем ключ для группировки
        val weekKey = "$firstDay - $lastDay $year"

        // Добавляем транзакцию в соответствующую группу
        if (!result.containsKey(weekKey)) {
            result[weekKey] = mutableListOf()
        }
        result[weekKey]?.add(transaction)
    }

    return result
}

/**
 * Группирует транзакции по месяцам
 */
private fun groupTransactionsByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val format = SimpleDateFormat("MMMM yyyy", Locale("ru"))
    return transactions
        .sortedByDescending { it.date }
        .groupBy { format.format(it.date).replaceFirstChar { it.uppercase() } }
}