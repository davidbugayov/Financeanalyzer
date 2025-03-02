package com.davidbugayov.financeanalyzer.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
 * Отображает список всех транзакций, сгруппированных по месяцам.
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
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val groupedTransactions = transactions.sortedByDescending { it.date }
                        .groupBy { formatDateToMonthYear(it.date) }
                    
                    groupedTransactions.forEach { (month, transactionsInMonth) ->
                        item {
                            Text(
                                text = month,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                        
                        items(transactionsInMonth) { transaction ->
                            TransactionHistoryItem(transaction = transaction)
                            HorizontalDivider()
                        }
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
 * Форматирует дату в строку месяца и года.
 */
private fun formatDateToMonthYear(date: Date): String {
    val format = SimpleDateFormat("MMMM yyyy", Locale("ru"))
    return format.format(date).replaceFirstChar { it.uppercase() }
} 