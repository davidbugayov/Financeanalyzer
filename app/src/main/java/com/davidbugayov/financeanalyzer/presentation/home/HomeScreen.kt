package com.davidbugayov.financeanalyzer.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

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
    val layoutDirection = LocalLayoutDirection.current
    
    // Загружаем транзакции при первом запуске
    LaunchedEffect(key1 = Unit) {
        viewModel.loadTransactions()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Финансовый анализатор") },
                // Уменьшаем отступы в TopAppBar
                modifier = Modifier.height(48.dp)
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

                // Последние транзакции
                Text(
                    text = "Последние транзакции",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (transactions.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет транзакций. Добавьте новую!",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(viewModel.getRecentTransactions()) { transaction ->
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
                                contentDescription = "Графики"
                            )
                        }
                        Text(
                            text = "Графики",
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
                                contentDescription = "Добавить"
                            )
                        }
                        Text(
                            text = "Добавить",
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
                                contentDescription = "История"
                            )
                        }
                        Text(
                            text = "История",
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
                text = "Текущий баланс",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Text(
                text = "₽ ${String.format("%.2f", balance)}",
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
                        text = "Доходы",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₽ ${String.format("%.2f", income)}",
                        fontSize = 16.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = "Расходы",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₽ ${String.format("%.2f", expense)}",
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
                text = "${transaction.category} • ${dateFormat.format(transaction.date)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        Text(
            text = "${if (transaction.isExpense) "-" else "+"}₽ ${String.format("%.2f", transaction.amount)}",
            color = if (transaction.isExpense) Color(0xFFF44336) else Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold
        )
    }
} 