package com.davidbugayov.financeanalyzer.presentation.chart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.davidbugayov.financeanalyzer.presentation.chart.components.CategoryPieChart
import com.davidbugayov.financeanalyzer.presentation.chart.components.MonthlyBarChart
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * Экран с финансовой аналитикой и графиками.
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Финансовая аналитика") },
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
                        .padding(16.dp),
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
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных для отображения",
                        color = Color.Gray
                    )
                }
            } else if (!isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Секция расходов по категориям
                    Text(
                        text = "Расходы по категориям",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val expensesByCategory = viewModel.getExpensesByCategory()
                    if (expensesByCategory.isNotEmpty()) {
                        CategoryPieChart(
                            data = expensesByCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет данных о расходах",
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Секция доходов по категориям
                    Text(
                        text = "Доходы по категориям",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val incomeByCategory = viewModel.getIncomeByCategory()
                    if (incomeByCategory.isNotEmpty()) {
                        CategoryPieChart(
                            data = incomeByCategory,
                            isIncome = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет данных о доходах",
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Секция баланса по месяцам
                    Text(
                        text = "Баланс по месяцам",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val transactionsByMonth = viewModel.getTransactionsByMonth()
                    if (transactionsByMonth.isNotEmpty()) {
                        MonthlyBarChart(
                            data = transactionsByMonth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Недостаточно данных для отображения",
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Секция расходов по дням
                    Text(
                        text = "Расходы за последние 7 дней",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val expensesByDay = viewModel.getExpensesByDay()
                    if (expensesByDay.isNotEmpty()) {
                        MonthlyBarChart(
                            data = expensesByDay,
                            isExpense = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет данных о расходах за последние дни",
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
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