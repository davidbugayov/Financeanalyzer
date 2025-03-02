package com.davidbugayov.financeanalyzer.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLayoutDirection

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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История транзакций") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                modifier = Modifier.height(48.dp)
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
                
                if (error != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Произошла ошибка",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.loadTransactions() }) {
                            Text("Повторить")
                        }
                    }
                } else if (transactions.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет транзакций для отображения",
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
                            GroupingType.DAY -> groupTransactionsByDay(transactions)
                            GroupingType.WEEK -> groupTransactionsByWeek(transactions)
                            GroupingType.MONTH -> groupTransactionsByMonth(transactions)
                        }
                        
                        groupedTransactions.forEach { (period, transactionsInPeriod) ->
                            item {
                                Text(
                                    text = period,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
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
            label = { Text("По дням") }
        )
        
        FilterChip(
            selected = currentGrouping == GroupingType.WEEK,
            onClick = { onGroupingSelected(GroupingType.WEEK) },
            label = { Text("По неделям") }
        )
        
        FilterChip(
            selected = currentGrouping == GroupingType.MONTH,
            onClick = { onGroupingSelected(GroupingType.MONTH) },
            label = { Text("По месяцам") }
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
                text = "${transaction.category} • ${dateFormat.format(transaction.date)}",
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
            text = "${if (transaction.isExpense) "-" else "+"}₽ ${String.format("%.2f", transaction.amount)}",
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