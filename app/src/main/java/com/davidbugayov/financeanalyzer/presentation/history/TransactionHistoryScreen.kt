package com.davidbugayov.financeanalyzer.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
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

/**
 * Экран истории транзакций.
 * Отображает список всех транзакций, сгруппированных по выбранному периоду.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    
    // Состояние для выбранного типа группировки
    var groupingType by remember { mutableStateOf(GroupingType.MONTH) }
    
    // Состояние для выбора даты
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }
    
    // Состояние для фильтрованных транзакций
    val filteredTransactions = remember(transactions, selectedDate) {
        filterTransactionsByDate(transactions, selectedDate)
    }
    
    // Диалог выбора даты
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
                    // Кнопка выбора даты
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.select_date))
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
                
                // Отображение выбранной даты
                Text(
                    text = stringResource(
                        R.string.date_format,
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(selectedDate)
                    ),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
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
@OptIn(ExperimentalMaterial3Api::class)
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
 * Фильтрует транзакции по выбранной дате
 */
private fun filterTransactionsByDate(transactions: List<Transaction>, selectedDate: Date): List<Transaction> {
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    
    // Получаем год и месяц выбранной даты
    val selectedYear = calendar.get(Calendar.YEAR)
    val selectedMonth = calendar.get(Calendar.MONTH)
    
    return transactions.filter {
        calendar.time = it.date
        val transactionYear = calendar.get(Calendar.YEAR)
        val transactionMonth = calendar.get(Calendar.MONTH)
        
        // Фильтруем по году и месяцу
        transactionYear == selectedYear && transactionMonth == selectedMonth
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