package com.davidbugayov.financeanalyzer.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.remember
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
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackMessage
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackType
import com.davidbugayov.financeanalyzer.presentation.components.LoadingIndicatorWithMessage
import com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.isCompact
import com.davidbugayov.financeanalyzer.utils.rememberWindowSize
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
    val windowSize = rememberWindowSize()

    // Состояние для обратной связи
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(FeedbackType.INFO) }

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
                            onClick = {
                                viewModel.onEvent(HomeEvent.GenerateTestData)
                                feedbackMessage = "Тестовые данные сгенерированы"
                                feedbackType = FeedbackType.SUCCESS
                                showFeedback = true
                            }
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
            // Адаптивный макет в зависимости от размера экрана
            if (windowSize.isCompact()) {
                // Компактный макет для телефонов
                CompactLayout(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onShowGroupSummaryChange = { showGroupSummary = it },
                    onFilterSelected = { viewModel.onEvent(HomeEvent.SetFilter(it)) },
                    onNavigateToHistory = onNavigateToHistory
                )
            } else {
                // Расширенный макет для планшетов
                ExpandedLayout(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onShowGroupSummaryChange = { showGroupSummary = it },
                    onFilterSelected = { viewModel.onEvent(HomeEvent.SetFilter(it)) },
                    onNavigateToHistory = onNavigateToHistory
                )
            }

            // Кнопки навигации поверх контента с анимацией
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 0.dp,
                    tonalElevation = 4.dp
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
                                onClick = {
                                    onNavigateToChart()
                                    feedbackMessage = "Переход к графикам"
                                    feedbackType = FeedbackType.INFO
                                    showFeedback = true
                                },
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
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Кнопка Добавить
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledIconButton(
                                onClick = {
                                    onNavigateToAdd()
                                    feedbackMessage = "Добавление новой транзакции"
                                    feedbackType = FeedbackType.INFO
                                    showFeedback = true
                                },
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
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Кнопка История
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    onNavigateToHistory()
                                    feedbackMessage = "Переход к истории транзакций"
                                    feedbackType = FeedbackType.INFO
                                    showFeedback = true
                                },
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
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Индикатор загрузки с анимацией
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LoadingIndicatorWithMessage(
                    message = "Загрузка данных...",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Отображение уведомлений обратной связи
            FeedbackMessage(
                message = feedbackMessage,
                type = feedbackType,
                visible = showFeedback,
                onDismiss = { showFeedback = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * Компактный макет для телефонов
 */
@Composable
private fun CompactLayout(
    state: com.davidbugayov.financeanalyzer.presentation.home.state.HomeState,
    showGroupSummary: Boolean,
    onShowGroupSummaryChange: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    // Используем LazyColumn для основного контента
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 88.dp), // Добавляем отступ снизу для кнопок
        state = rememberLazyListState()
    ) {
        // Карточка с балансом
        item {
            BalanceCard(
                income = state.income,
                expense = state.expense,
                balance = state.balance
            )
        }

        // Сообщение об ошибке
        state.error?.let {
            item {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Фильтры транзакций
        item {
            FilterChips(
                currentFilter = state.currentFilter,
                onFilterSelected = onFilterSelected
            )
        }

        // Заголовок для транзакций с чекбоксом для GroupSummary
        item {
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
                            onCheckedChange = onShowGroupSummaryChange
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
        }

        // Отображение суммы для выбранного периода
        if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
            item {
                GroupSummary(transactions = state.filteredTransactions)
            }
        }

        // Список транзакций
        if (state.filteredTransactions.isEmpty() && !state.isLoading) {
            item {
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
            }
        } else {
            // Используем itemsIndexed для оптимизации списка транзакций
            itemsIndexed(
                items = state.filteredTransactions,
                key = { _, transaction -> transaction.id }
            ) { index, transaction ->
                TransactionItem(transaction = transaction)
                if (index < state.filteredTransactions.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Расширенный макет для планшетов
 */
@Composable
private fun ExpandedLayout(
    state: com.davidbugayov.financeanalyzer.presentation.home.state.HomeState,
    showGroupSummary: Boolean,
    onShowGroupSummaryChange: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 88.dp) // Добавляем отступ снизу для кнопок
    ) {
        // Левая панель с балансом и фильтрами
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            BalanceCard(
                income = state.income,
                expense = state.expense,
                balance = state.balance
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilterChips(
                currentFilter = state.currentFilter,
                onFilterSelected = onFilterSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Отображение суммы для выбранного периода
            if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
                GroupSummary(transactions = state.filteredTransactions)
            }
        }

        // Правая панель со списком транзакций
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
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
                            onCheckedChange = onShowGroupSummaryChange
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
                // Используем LazyColumn для списка транзакций
                LazyColumn(
                    state = rememberLazyListState()
                ) {
                    itemsIndexed(
                        items = state.filteredTransactions,
                        key = { _, transaction -> transaction.id }
                    ) { index, transaction ->
                        TransactionItem(transaction = transaction)
                        if (index < state.filteredTransactions.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
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
fun BalanceCard(income: Money, expense: Money, balance: Money) {
    // Получаем цвета из темы
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    
    // Кэшируем форматированные значения, чтобы избежать повторных вычислений при перерисовке
    val formattedBalance = remember(balance) { balance.formatForDisplay() }
    val formattedIncome = remember(income) { income.format(false) }
    val formattedExpense = remember(expense) { expense.format(false) }
    val balanceColor = remember(balance) { if (!balance.isNegative()) incomeColor else expenseColor }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.current_balance),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formattedBalance,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        text = stringResource(R.string.currency_format, formattedIncome),
                        fontSize = 14.sp,
                        color = incomeColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.currency_format, formattedExpense),
                        fontSize = 14.sp,
                        color = expenseColor,
                        fontWeight = FontWeight.Medium
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
    // Получаем цвета из темы
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    
    // Кэшируем форматированные значения, чтобы избежать повторных вычислений при перерисовке
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val formattedDate = remember(transaction.date) { dateFormat.format(transaction.date) }
    val formattedAmount = remember(transaction.amount, transaction.isExpense) {
        transaction.amount.format(false)
    }
    val amountColor = remember(transaction.isExpense) {
        if (transaction.isExpense) expenseColor else incomeColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.category_date_format, transaction.category, formattedDate),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            transaction.note?.let {
                if (it.isNotEmpty()) {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Text(
            text = if (transaction.isExpense)
                stringResource(R.string.expense_currency_format, formattedAmount)
            else
                stringResource(R.string.income_currency_format, formattedAmount),
            color = amountColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Компонент для отображения сводки по группе транзакций
 */
@Composable
fun GroupSummary(transactions: List<Transaction>) {
    // Получаем цвета из темы
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    
    // Кэшируем вычисления, чтобы избежать повторных вычислений при перерисовке
    val financialSummary = remember(transactions) {
        val income = transactions
            .filter { !it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val expense = transactions
            .filter { it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val balance = income - expense

        Triple(income, expense, balance)
    }

    val (income, expense, balance) = financialSummary

    // Кэшируем форматированные значения
    val formattedIncome = remember(income) { income.format(false) }
    val formattedExpense = remember(expense) { expense.format(false) }
    val formattedBalance = remember(balance) { balance.format(false) }
    val balanceColor = remember(balance) { if (balance >= Money.zero()) incomeColor else expenseColor }

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_format, formattedIncome),
                    fontSize = 14.sp,
                    color = incomeColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.expense),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_format, formattedExpense),
                    fontSize = 14.sp,
                    color = expenseColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.balance),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_format, formattedBalance),
                    fontSize = 14.sp,
                    color = balanceColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 