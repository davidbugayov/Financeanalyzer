package com.davidbugayov.financeanalyzer.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.AdaptiveAppBar
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import com.davidbugayov.financeanalyzer.presentation.components.DeleteTransactionDialog
import com.davidbugayov.financeanalyzer.presentation.components.EmptyContent
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.presentation.history.components.CategoryStatsCard
import com.davidbugayov.financeanalyzer.presentation.history.components.GroupingChips
import com.davidbugayov.financeanalyzer.presentation.history.components.TransactionHistory
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.CategorySelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DatePickerDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.DeleteCategoryConfirmDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.PeriodSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.dialogs.SourceSelectionDialog
import com.davidbugayov.financeanalyzer.presentation.history.event.TransactionHistoryEvent
import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.ColorUtils
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
        AnalyticsUtils.logScreenView(
            screenName = "transaction_history",
            screenClass = "TransactionHistoryScreen"
        )
    }

    // Получаем список всех категорий из CategoriesViewModel
    val expenseCategories by viewModel.categoriesViewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.categoriesViewModel.incomeCategories.collectAsState()

    // Преобразуем категории в списки строк для отображения
    val expenseCategoryNames = remember(expenseCategories) {
        expenseCategories.map { it.name }.filter { it != "Другое" }.sorted()
    }

    val incomeCategoryNames = remember(incomeCategories) {
        incomeCategories.map { it.name }.filter { it != "Другое" }.sorted()
    }

    // Диалог выбора периода
    if (state.showPeriodDialog) {
        PeriodSelectionDialog(
            selectedPeriod = state.periodType,
            startDate = state.startDate,
            endDate = state.endDate,
            onPeriodSelected = {
                viewModel.onEvent(TransactionHistoryEvent.SetPeriodType(it))
                if (it != PeriodType.CUSTOM) {
                    viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog)
                }
            },
            onStartDateClick = {
                viewModel.onEvent(TransactionHistoryEvent.ShowStartDatePicker)
            },
            onEndDateClick = {
                viewModel.onEvent(TransactionHistoryEvent.ShowEndDatePicker)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HidePeriodDialog)
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
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideStartDatePicker)
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
            }
        )
    }

    // Диалог выбора категории
    if (state.showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategories = state.selectedCategories,
            expenseCategories = expenseCategoryNames,
            incomeCategories = incomeCategoryNames,
            onCategoriesSelected = { categories ->
                viewModel.onEvent(TransactionHistoryEvent.SetCategories(categories))
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideCategoryDialog)
            }
        )
    }

    // Диалог подтверждения удаления категории
    state.categoryToDelete?.let { (category, isExpense) ->
        // Определяем, является ли категория стандартной
        val isDefaultCategory = if (isExpense) {
            viewModel.categoriesViewModel.isDefaultExpenseCategory(category)
        } else {
            viewModel.categoriesViewModel.isDefaultIncomeCategory(category)
        }
        
        DeleteCategoryConfirmDialog(
            category = category,
            onConfirm = {
                viewModel.onEvent(TransactionHistoryEvent.DeleteCategory(category, isExpense))
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideDeleteCategoryConfirmDialog)
            },
            isDefaultCategory = isDefaultCategory
        )
    }

    // Диалог подтверждения удаления транзакции
    state.transactionToDelete?.let { transaction ->
        DeleteTransactionDialog(
            transaction = transaction,
            onConfirm = {
                viewModel.onEvent(TransactionHistoryEvent.DeleteTransaction(transaction))
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideDeleteConfirmDialog)
            }
        )
    }

    // Диалог выбора источника
    if (state.showSourceDialog) {
        // Получаем список всех источников из utils
        val sources = remember {
            val defaultSources = ColorUtils.defaultSources
            defaultSources + listOf(Source(name = "Наличные", color = 0xFF9E9E9E.toInt()))
        }

        SourceSelectionDialog(
            selectedSources = state.selectedSources,
            sources = sources,
            onSourcesSelected = { selectedSources ->
                viewModel.onEvent(TransactionHistoryEvent.SetSources(selectedSources))
                viewModel.onEvent(TransactionHistoryEvent.HideSourceDialog)
            },
            onDismiss = {
                viewModel.onEvent(TransactionHistoryEvent.HideSourceDialog)
            }
        )
    }

    Scaffold(
        topBar = {
            AdaptiveAppBar(
                title = stringResource(R.string.history_title),
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowSourceDialog) }) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = stringResource(R.string.select_sources),
                            tint = if (state.selectedSources.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(TransactionHistoryEvent.ShowCategoryDialog) }) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = stringResource(R.string.select_category),
                            tint = if (state.selectedCategories.isNotEmpty())
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
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    
                    Text(
                        text = when (state.periodType) {
                            PeriodType.ALL -> stringResource(R.string.all_time)
                            PeriodType.DAY -> stringResource(R.string.day)
                            PeriodType.WEEK -> stringResource(R.string.week)
                            PeriodType.MONTH -> stringResource(R.string.month)
                            PeriodType.QUARTER -> stringResource(R.string.period_quarter)
                            PeriodType.YEAR -> stringResource(R.string.year)
                            PeriodType.CUSTOM -> {
                                val startDate = state.startDate
                                val endDate = state.endDate
                                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
                            }
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Показываем статистику по категории
                state.categoryStats?.let { (currentTotal, previousTotal, percentChange) ->
                    if (state.selectedCategories.size == 1) {
                        CategoryStatsCard(
                            category = state.selectedCategories.first(),
                            currentTotal = currentTotal,
                            previousTotal = previousTotal,
                            percentChange = percentChange
                        )
                    }
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
                            // Для доходов
                            val income = transactions
                                .filter { !it.isExpense }
                                .map { Money(it.amount) }
                                .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                            // Для расходов берем абсолютное значение (без минуса)
                            val expense = transactions
                                .filter { it.isExpense }
                                .map { Money(it.amount) }
                                .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                            // Для баланса: доходы - расходы
                            val balance = income.minus(expense)

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
                    CenteredLoadingIndicator(message = stringResource(R.string.loading_data))
                }
            }
        }
    }
}
