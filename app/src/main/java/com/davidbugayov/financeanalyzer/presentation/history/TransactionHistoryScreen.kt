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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    
    // Логируем открытие экрана истории
    LaunchedEffect(Unit) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "transaction_history")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "TransactionHistoryScreen")
        }
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Состояние для выбранного типа группировки
    var groupingType by remember { mutableStateOf(GroupingType.MONTH) }

    // Состояние для выбора периода
    var periodType by remember { mutableStateOf(PeriodType.MONTH) }
    var showPeriodDialog by remember { mutableStateOf(false) }

    // Состояние для фильтрации по категориям
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    // Получаем список всех категорий
    val categories = remember(transactions) {
        transactions.map { it.category }.distinct().sorted()
    }

    // Состояние для выбора дат
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(
        Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time
    ) }
    var endDate by remember { mutableStateOf(Date()) }

    // Состояние для фильтрованных транзакций
    val filteredTransactions = remember(transactions, periodType, startDate, endDate, selectedCategory) {
        filterTransactionsByPeriod(transactions, periodType, startDate, endDate)
            .filter { transaction -> 
                selectedCategory == null || transaction.category == selectedCategory
            }
    }

    // Вычисляем статистику для выбранной категории
    val categoryStats = remember(filteredTransactions, selectedCategory) {
        if (selectedCategory != null) {
            val currentPeriodTransactions = filteredTransactions.filter { it.category == selectedCategory }
            val currentPeriodTotal = currentPeriodTransactions.sumOf { it.amount }
            
            // Вычисляем даты для предыдущего периода
            val periodDuration = endDate.time - startDate.time
            val previousStartDate = Date(startDate.time - periodDuration)
            val previousEndDate = Date(endDate.time - periodDuration)
            
            // Получаем транзакции за предыдущий период
            val previousPeriodTransactions = filterTransactionsByPeriod(
                transactions.filter { it.category == selectedCategory },
                PeriodType.CUSTOM,
                previousStartDate,
                previousEndDate
            )
            val previousPeriodTotal = previousPeriodTransactions.sumOf { it.amount }
            
            // Вычисляем разницу в процентах
            val percentChange = if (previousPeriodTotal != 0.0) {
                ((currentPeriodTotal - previousPeriodTotal) / kotlin.math.abs(previousPeriodTotal) * 100).toInt()
            } else null
            
            Triple(currentPeriodTotal, previousPeriodTotal, percentChange)
        } else null
    }

    // Добавляем логирование при изменении периода
    LaunchedEffect(periodType) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "period_type")
            putString(FirebaseAnalytics.Param.ITEM_NAME, periodType.name)
        }
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params)
    }

    // Добавляем логирование при изменении группировки
    LaunchedEffect(groupingType) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "grouping_type")
            putString(FirebaseAnalytics.Param.ITEM_NAME, groupingType.name)
        }
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params)
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

    // Диалог добавления новой категории
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddCategoryDialog = false
                newCategoryText = ""
            },
            title = { Text("Добавить категорию") },
            text = {
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text("Название категории") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryText.isNotBlank()) {
                            selectedCategory = newCategoryText
                            showAddCategoryDialog = false
                            showCategoryDialog = false
                            newCategoryText = ""
                        }
                    },
                    enabled = newCategoryText.isNotBlank()
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddCategoryDialog = false
                        newCategoryText = ""
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог выбора категории
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text(stringResource(R.string.select_category)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Кнопка добавления новой категории
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(56.dp)
                    ) {
                        Text("Добавить категорию")
                    }

                    // Опция "Все категории"
                    CategoryButton(
                        text = stringResource(R.string.all),
                        selected = selectedCategory == null,
                        onClick = {
                            selectedCategory = null
                            showCategoryDialog = false
                        }
                    )

                    // Список категорий
                    categories.forEach { category ->
                        CategoryButton(
                            text = category,
                            selected = category == selectedCategory,
                            onClick = {
                                selectedCategory = category
                                showCategoryDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    selectedCategory = null
                    showCategoryDialog = false 
                }) {
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
                            .height(56.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.history_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Кнопка фильтра по категориям
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = stringResource(R.string.select_category),
                            tint = if (selectedCategory != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    // Кнопка фильтра по периоду
                    IconButton(onClick = { showPeriodDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.select_period)
                        )
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

                // Показываем статистику по категории, если она выбрана
                val category = selectedCategory // сохраняем значение в локальную переменную
                if (category != null && categoryStats != null) {
                    val (currentTotal, previousTotal, percentChange) = categoryStats
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Текущий период",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = String.format("%.2f ₽", currentTotal),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Прошлый период",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = String.format("%.2f ₽", previousTotal),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (percentChange != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val currentPercentChange = percentChange // сохраняем в локальную переменную
                                val changeText = when {
                                    currentPercentChange.compareTo(0) > 0 -> "На $currentPercentChange% больше чем в прошлом периоде"
                                    currentPercentChange.compareTo(0) < 0 -> "На ${kotlin.math.abs(currentPercentChange)}% меньше чем в прошлом периоде"
                                    else -> "Без изменений по сравнению с прошлым периодом"
                                }
                                val changeColor = when {
                                    currentPercentChange.compareTo(0) > 0 -> Color(0xFFE57373) // Красный для увеличения расходов
                                    currentPercentChange.compareTo(0) < 0 -> Color(0xFF81C784) // Зеленый для уменьшения расходов
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Text(
                                    text = changeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = changeColor
                                )
                            }
                        }
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
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            items(transactionsInPeriod) { transaction ->
                                TransactionHistoryItem(transaction = transaction)
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
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
            .height(48.dp)
            .clickable { onClick() },
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
 * Форматирует число в сокращенный вид (1.2K, 1.3M и т.д.)
 */
private fun formatAbbreviatedNumber(number: Double): String {
    return when {
        kotlin.math.abs(number) >= 1_000_000 -> {
            String.format("%.1fM", number / 1_000_000)
        }
        kotlin.math.abs(number) >= 1_000 -> {
            String.format("%.1fK", number / 1_000)
        }
        else -> {
            String.format("%.0f", number)
        }
    }
}

/**
 * Заголовок группы транзакций с суммой
 */
@Composable
fun GroupHeader(period: String, transactions: List<Transaction>) {
    var isExpanded by remember { mutableStateOf(true) }
    val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
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
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
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
                        text = stringResource(
                            R.string.income_label,
                            stringResource(R.string.currency_format, formatAbbreviatedNumber(income))
                        ),
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )

                    Text(
                        text = stringResource(
                            R.string.expense_label,
                            stringResource(R.string.currency_format, formatAbbreviatedNumber(expense))
                        ),
                        fontSize = 12.sp,
                        color = Color(0xFFF44336)
                    )

                    Text(
                        text = stringResource(
                            R.string.balance_label,
                            stringResource(R.string.currency_format, formatAbbreviatedNumber(balance))
                        ),
                        fontSize = 12.sp,
                        color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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

        // Определяем первый и последний день недели
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val firstDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val lastDay = SimpleDateFormat("dd.MM", Locale("ru")).format(calendar.time)
        val year = calendar.get(Calendar.YEAR)

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

/**
 * Кнопка выбора категории
 */
@Composable
private fun CategoryButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}