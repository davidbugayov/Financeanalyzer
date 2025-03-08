package com.davidbugayov.financeanalyzer.presentation.history

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.presentation.components.LoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.components.PeriodFilterChips
import com.davidbugayov.financeanalyzer.presentation.history.components.GroupingChips
import com.davidbugayov.financeanalyzer.presentation.history.components.TransactionHistory
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.AddCategoryDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.CategorySelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.history.event.TransactionHistoryEvent
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Логируем открытие экрана истории
    LaunchedEffect(Unit) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "transaction_history")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "TransactionHistoryScreen")
        }
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Состояние диалогов
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    // Получаем список всех категорий
    val categories = remember(state.transactions) {
        state.transactions.map { it.category }.distinct().sorted()
    }

    // Диалог выбора периода
    if (state.showPeriodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog) },
            title = { Text(stringResource(R.string.select_period)) },
            text = {
                PeriodFilterChips(
                    currentFilter = state.periodType,
                    onFilterSelected = {
                        viewModel.onEvent(TransactionHistoryEvent.SetPeriodType(it))
                        if (it != PeriodType.CUSTOM) {
                            viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog)
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog) }) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    // Диалог выбора категории
    if (state.showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategory = state.selectedCategory,
            categories = categories,
            onCategorySelected = { category ->
                viewModel.onEvent(TransactionHistoryEvent.SetCategory(category))
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
            },
            onAddCategory = {
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
                showAddCategoryDialog = true
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
            }
        )
    }

    // Диалог добавления новой категории
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            categoryText = newCategoryText,
            onCategoryTextChange = { newCategoryText = it },
            onConfirm = {
                if (newCategoryText.isNotBlank()) {
                    viewModel.onEvent(TransactionHistoryEvent.SetCategory(newCategoryText))
                    newCategoryText = ""
                }
                showAddCategoryDialog = false
                showCategoryDialog = true
            },
            onDismiss = {
                newCategoryText = ""
                showAddCategoryDialog = false
                showCategoryDialog = true
            }
        )
    }

    // Диалог выбора начальной даты
    if (state.showStartDatePicker) {
        DatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { date ->
                viewModel.onEvent(TransactionHistoryEvent.SetStartDate(date))
                viewModel.onEvent(TransactionHistoryEvent.HideStartDatePicker)
                viewModel.onEvent(TransactionHistoryEvent.ShowEndDatePicker)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideStartDatePicker)
                viewModel.onEvent(TransactionHistoryEvent.ShowPeriodDialog)
            }
        )
    }

    // Диалог выбора конечной даты
    if (state.showEndDatePicker) {
        DatePickerDialog(
            initialDate = state.endDate,
            onDateSelected = { date ->
                viewModel.onEvent(TransactionHistoryEvent.SetEndDate(date))
                viewModel.onEvent(TransactionHistoryEvent.HideEndDatePicker)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideEndDatePicker)
                viewModel.onEvent(TransactionHistoryEvent.ShowPeriodDialog)
            }
        )
    }

    // Диалог подтверждения удаления транзакции
    state.transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TransactionHistoryEvent.HideDeleteConfirmDialog) },
            title = { Text(stringResource(R.string.delete_transaction)) },
            text = {
                Text(
                    stringResource(
                        R.string.delete_transaction_confirmation,
                        transaction.title,
                        transaction.amount.format()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(TransactionHistoryEvent.DeleteTransaction(transaction))
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(TransactionHistoryEvent.HideDeleteConfirmDialog) }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowCategoryDialog) }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = stringResource(R.string.select_category),
                            tint = if (state.selectedCategory != null)
                                MaterialTheme.colorScheme.primary
                            else
                                LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowPeriodDialog) }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.select_period)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GroupingChips(
                        currentGrouping = state.groupingType,
                        onGroupingSelected = { viewModel.onEvent(TransactionHistoryEvent.SetGroupingType(it)) }
                    )
                }

                // Отображение выбранного периода
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            when (state.periodType) {
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

                    if (state.periodType == PeriodType.CUSTOM) {
                        Text(
                            text = " (${
                                SimpleDateFormat(
                                    "dd.MM.yyyy",
                                    Locale.getDefault()
                                ).format(state.startDate)
                            } - ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(state.endDate)})",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Показываем статистику по категории
                state.categoryStats?.let { (currentTotal, previousTotal, percentChange) ->
                    CategoryStatsCard(
                        category = state.selectedCategory!!,
                        currentTotal = currentTotal,
                        previousTotal = previousTotal,
                        percentChange = percentChange
                    )
                }

                if (state.error != null) {
                    ErrorContent(
                        error = state.error,
                        onRetry = { viewModel.onEvent(TransactionHistoryEvent.ReloadTransactions) }
                    )
                } else if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                    EmptyContent()
                } else if (!state.isLoading) {
                    // Отображение сгруппированных транзакций
                    val groupedTransactions = viewModel.getGroupedTransactions()
                    val transactionGroups = remember(groupedTransactions) {
                        groupedTransactions.map { (period, transactions) ->
                            // Вычисляем баланс для группы транзакций
                            val income = transactions
                                .filter { !it.isExpense }
                                .map { it.amount }
                                .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                            // Для расходов берем абсолютное значение (без минуса)
                            val expense = transactions
                                .filter { it.isExpense }
                                .map { it.amount }
                                .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                            // Для баланса: доходы - расходы
                            val balance = income - expense

                            TransactionGroup(
                                date = period,
                                transactions = transactions,
                                balance = balance
                            )
                        }
                    }

                    TransactionHistory(
                        transactionGroups = transactionGroups,
                        onTransactionClick = { /* Пока ничего не делаем при клике */ },
                        onTransactionLongClick = { transaction ->
                            viewModel.onEvent(TransactionHistoryEvent.ShowDeleteConfirmDialog(transaction))
                        }
                    )
                }

                if (state.isLoading) {
                    LoadingIndicator()
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
    var isExpanded by remember { mutableStateOf(true) }
    val income = transactions
        .filter { !it.isExpense }
        .map { it.amount }
        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

    val expense = transactions
        .filter { it.isExpense }
        .map { it.amount }
        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
    val balance = income - expense

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .shadow(elevation = 1.dp)
            .clickable { isExpanded = !isExpanded },
        color = MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = period,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.currency_format, income.format(false)),
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = stringResource(R.string.expense_currency_format, expense.abs().format(false)),
                        fontSize = 14.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = stringResource(R.string.currency_format, balance.format(false)),
                        fontSize = 14.sp,
                        color = if (balance >= Money.zero()) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Элемент списка транзакций в истории.
 */
@Composable
fun TransactionHistoryItem(transaction: Transaction) {
    // Используем локаль устройства для форматирования даты
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val formattedDate = remember(transaction.date) { dateFormat.format(transaction.date) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    text = stringResource(
                        R.string.category_date_format,
                        transaction.category,
                        formattedDate
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                transaction.note?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = if (transaction.isExpense)
                    stringResource(R.string.expense_currency_format, transaction.amount.format(false))
                else
                    stringResource(R.string.income_currency_format, transaction.amount.format(false)),
                color = if (transaction.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
