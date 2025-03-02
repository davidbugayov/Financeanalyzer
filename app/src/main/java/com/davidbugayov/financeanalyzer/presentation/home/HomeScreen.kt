package com.davidbugayov.financeanalyzer.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToChart: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    
    // Загружаем сохраненное состояние видимости GroupSummary
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)
    var showGroupSummary by rememberSaveable { 
        mutableStateOf(sharedPreferences.getBoolean("show_group_summary", true)) 
    }
    
    // Загружаем транзакции при первом запуске
    LaunchedEffect(key1 = Unit) {
        viewModel.loadTransactions()
    }
    
    // Сохраняем настройку при изменении
    LaunchedEffect(showGroupSummary) {
        sharedPreferences.edit().putBoolean("show_group_summary", showGroupSummary).apply()
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
                            text = stringResource(R.string.app_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                // Уменьшаем отступы в TopAppBar
                modifier = Modifier.height(56.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Применяем все отступы, включая верхний
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Карточка с балансом
                BalanceCard(
                    income = viewModel.getTotalIncome(),
                    expense = viewModel.getTotalExpense(),
                    balance = viewModel.getCurrentBalance()
                )

                // Сообщение об ошибке
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Фильтры транзакций
                FilterChips(
                    currentFilter = currentFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )

                // Заголовок для транзакций с чекбоксом для GroupSummary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when(currentFilter) {
                            TransactionFilter.TODAY -> stringResource(R.string.transactions_today)
                            TransactionFilter.WEEK -> stringResource(R.string.transactions_week)
                            TransactionFilter.MONTH -> stringResource(R.string.transactions_month)
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Иконка с чекбоксом для управления видимостью GroupSummary
                        Row(
                            verticalAlignment = CenterVertically,
                            modifier = Modifier
                                .height(36.dp)
                        ) {
                            Checkbox(
                                checked = showGroupSummary,
                                onCheckedChange = { showGroupSummary = it }
                            )
                            Icon(
                                imageVector = Icons.Default.Summarize,
                                contentDescription = stringResource(R.string.show_summary),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        TextButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(stringResource(R.string.all))
                        }
                    }
                }
                
                // Отображение суммы для выбранного периода
                val filteredTransactions = viewModel.getFilteredTransactions()
                if (filteredTransactions.isNotEmpty() && showGroupSummary) {
                    GroupSummary(transactions = filteredTransactions)
                }

                // Список транзакций
                if (filteredTransactions.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when(currentFilter) {
                                TransactionFilter.TODAY -> stringResource(R.string.no_transactions_today)
                                TransactionFilter.WEEK -> stringResource(R.string.no_transactions_week)
                                TransactionFilter.MONTH -> stringResource(R.string.no_transactions_month)
                            },
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredTransactions) { transaction ->
                            TransactionItem(transaction = transaction)
                            HorizontalDivider()
                        }
                    }
                }

                // Кнопки навигации с новой организацией
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Кнопка Графики
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        FilledTonalIconButton(
                            onClick = onNavigateToChart,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = stringResource(R.string.charts)
                            )
                        }
                        Text(
                            text = stringResource(R.string.charts),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Кнопка Добавить
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        FilledIconButton(
                            onClick = onNavigateToAddTransaction,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add)
                            )
                        }
                        Text(
                            text = stringResource(R.string.add),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Кнопка История
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        FilledTonalIconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(R.string.history)
                            )
                        }
                        Text(
                            text = stringResource(R.string.history),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Индикатор загрузки
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Компонент с фильтрами транзакций
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == TransactionFilter.TODAY,
            onClick = { onFilterSelected(TransactionFilter.TODAY) },
            label = { Text(stringResource(R.string.filter_today)) }
        )
        
        FilterChip(
            selected = currentFilter == TransactionFilter.WEEK,
            onClick = { onFilterSelected(TransactionFilter.WEEK) },
            label = { Text(stringResource(R.string.filter_week)) }
        )
        
        FilterChip(
            selected = currentFilter == TransactionFilter.MONTH,
            onClick = { onFilterSelected(TransactionFilter.MONTH) },
            label = { Text(stringResource(R.string.filter_month)) }
        )
    }
}

/**
 * Карточка с информацией о балансе.
 */
@Composable
fun BalanceCard(income: Double, expense: Double, balance: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Уменьшаем вертикальный отступ
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp) // Уменьшаем внутренний отступ
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.current_balance),
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Text(
                text = stringResource(R.string.currency_format, String.format("%.2f", balance)),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(12.dp)) // Уменьшаем отступ
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", income)),
                        fontSize = 16.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", expense)),
                        fontSize = 16.sp,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

/**
 * Элемент списка транзакций.
 */
@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Уменьшаем вертикальный отступ
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
            // Добавляем отображение примечания, если оно есть
            transaction.note?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
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
 * Компонент для отображения сводки по группе транзакций
 */
@Composable
fun GroupSummary(transactions: List<Transaction>) {
    val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val balance = income - expense
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(R.string.income),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.currency_format, String.format("%.2f", income)),
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.expense),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.currency_format, String.format("%.2f", expense)),
                    fontSize = 14.sp,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.balance),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.currency_format, String.format("%.2f", balance)),
                    fontSize = 14.sp,
                    color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 