package com.davidbugayov.financeanalyzer.presentation.home

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.BuildConfig
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Главный экран приложения.
 * Отображает текущий баланс и последние транзакции.
 * Следует принципам MVI и Clean Architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToChart: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Загружаем сохраненное состояние видимости GroupSummary
    val sharedPreferences = context.getSharedPreferences("finance_analyzer_prefs", 0)
    var showGroupSummary by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean("show_group_summary", true))
    }

    // Загружаем транзакции при первом запуске
    LaunchedEffect(key1 = Unit) {
        viewModel.onEvent(HomeEvent.LoadTransactions)
        // Инициализируем состояние showGroupSummary в ViewModel из SharedPreferences
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(showGroupSummary))
    }

    // Сохраняем настройку при изменении
    LaunchedEffect(showGroupSummary) {
        sharedPreferences.edit().putBoolean("show_group_summary", showGroupSummary).apply()
        // Обновляем состояние в ViewModel
        viewModel.onEvent(HomeEvent.SetShowGroupSummary(showGroupSummary))
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
                actions = {
                    // Кнопка для генерации тестовых данных
                    if (BuildConfig.DEBUG) {
                        IconButton(
                            onClick = { viewModel.onEvent(HomeEvent.GenerateTestData) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Сгенерировать тестовые данные"
                            )
                        }
                    }
                },
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
                    .padding(bottom = 88.dp) // Добавляем отступ снизу для кнопок
                    .verticalScroll(rememberScrollState())
            ) {
                // Карточка с балансом
                BalanceCard(
                    income = state.income,
                    expense = state.expense,
                    balance = state.balance
                )

                // Сообщение об ошибке
                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Фильтры транзакций
                FilterChips(
                    currentFilter = state.currentFilter,
                    onFilterSelected = { viewModel.onEvent(HomeEvent.SetFilter(it)) }
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
                        text = when (state.currentFilter) {
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
                if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
                    GroupSummary(transactions = state.filteredTransactions)
                }

                // Список транзакций
                if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (state.currentFilter) {
                                TransactionFilter.TODAY -> stringResource(R.string.no_transactions_today)
                                TransactionFilter.WEEK -> stringResource(R.string.no_transactions_week)
                                TransactionFilter.MONTH -> stringResource(R.string.no_transactions_month)
                            },
                            color = Color.Gray
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        state.filteredTransactions.forEach { transaction ->
                            TransactionItem(transaction = transaction)
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Кнопки навигации поверх контента
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
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
                                imageVector = Icons.Default.Summarize,
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
                            onClick = onNavigateToAdd,
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
            if (state.isLoading) {
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