package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.paging.transactionPagingList
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import timber.log.Timber

/**
 * Компактный макет для телефонов
 */
@Composable
private fun CompactBalanceAndFilters(
    state: HomeState,
    onFilterSelected: (TransactionFilter) -> Unit,
    onToggleGroupSummary: (Boolean) -> Unit,
    showGroupSummary: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        BalanceCard(balance = state.balance, income = state.income, expense = state.expense)

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }

        HomeFilterChips(
            currentFilter = state.currentFilter,
            onFilterSelected = onFilterSelected,
        )

        HomeTransactionsHeader(
            currentFilter = state.currentFilter,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
        )
    }
}

@Composable
private fun CompactEmptyState(onAddClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                contentDescription = stringResource(UiR.string.empty_state_icon),
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .padding(bottom = 8.dp)
                        .size(36.dp),
            )
            Text(
                text = stringResource(UiR.string.no_transactions),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = stringResource(UiR.string.add_first_transaction_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            androidx.compose.material3.Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(50),
                colors =
                    androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 44.dp),
            ) {
                Text(
                    text = stringResource(UiR.string.add_transaction),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CompactTransactionList(
    state: HomeState,
    categoriesViewModel: CategoriesViewModel,
    showGroupSummary: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    // Логируем изменения состояния
    LaunchedEffect(state.isLoading) {
    }

    LaunchedEffect(state.filteredTransactions.size) {
    }

    LaunchedEffect(showGroupSummary) {
        if (showGroupSummary && state.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }

    // Отслеживаем количество транзакций для анимации новых элементов
    remember { mutableIntStateOf(0) }
    state.filteredTransactions.size
}

@Composable
fun CompactLayout(
    state: HomeState,
    categoriesViewModel: CategoriesViewModel,
    pagingItems: LazyPagingItems<TransactionListItem>,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
    isFilterSwitching: Boolean = false,
) {
    val listState: LazyListState = rememberLazyListState()

    // При смене фильтра возвращаемся к началу списка
    LaunchedEffect(state.currentFilter) {
        listState.scrollToItem(0)
    }

    // После завершения первой загрузки Paging убеждаемся, что находимся в начале списка
    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is LoadState.NotLoading) {
            listState.scrollToItem(0)
        }
    }

    // --- Добавлено: автоскролл при показе сводки ---
    LaunchedEffect(showGroupSummary, state.filteredTransactions.size) {
        if (showGroupSummary && state.filteredTransactions.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    // --- Конец добавления ---

    val itemsToDisplay = pagingItems

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        CompactBalanceAndFilters(
            state = state,
            onFilterSelected = onFilterSelected,
            onToggleGroupSummary = onToggleGroupSummary,
            showGroupSummary = showGroupSummary,
        )

        // Извлекаем реальные транзакции из pagingItems для определения пустого состояния
        val realTransactionsCount = (0 until pagingItems.itemCount).count { index ->
            val item = pagingItems[index]
            item is com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem.Item
        }

        // Определяем, показывать ли пустое состояние
        // Если идет переключение фильтра, показываем пустое состояние до загрузки новых данных
        val isEmptyState = (realTransactionsCount == 0 && pagingItems.loadState.refresh is androidx.paging.LoadState.NotLoading) || isFilterSwitching

        Timber.d("CompactLayout: isEmptyState=$isEmptyState, realTransactionsCount=$realTransactionsCount, totalItems=${pagingItems.itemCount}, loadState=${pagingItems.loadState.refresh}, isFilterSwitching=$isFilterSwitching")

        if (isEmptyState) {
            Timber.d("CompactLayout: Showing CompactEmptyState")
            CompactEmptyState(onAddClick)
        } else {
            Timber.d("CompactLayout: Showing transaction list")
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("finance_analyzer_prefs", 0) }

            val headerContent: (@Composable () -> Unit)? =
                if (showGroupSummary) {
                        {
                            // Извлекаем транзакции из pagingItems для гарантированно актуальных данных
                            val transactionsFromPaging = (0 until pagingItems.itemCount).mapNotNull { index ->
                                val item = pagingItems[index]
                                when (item) {
                                    is com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem.Item -> item.transaction
                                    else -> null
                                }
                            }

                            Timber.d("CompactLayout: Extracted ${transactionsFromPaging.size} transactions from pagingItems (total items: ${pagingItems.itemCount})")

                            val transactionsToUse = if (transactionsFromPaging.isNotEmpty()) {
                                Timber.d("CompactLayout: Using transactions from pagingItems")
                                transactionsFromPaging
                            } else {
                                Timber.d("CompactLayout: Using transactions from state")
                                state.filteredTransactions
                            }

                            val incomeToUse = state.filteredIncome
                            val expenseToUse = state.filteredExpense
                            val balanceToUse = state.filteredBalance

                            Timber.d("CompactLayout: Creating HomeGroupSummary with data - transactions: ${transactionsToUse.size}, income: $incomeToUse, expense: $expenseToUse, balance: $balanceToUse")
                            Timber.d("CompactLayout: HomeGroupSummary will calculate its own stats from ${transactionsToUse.size} transactions")
                            Timber.d("CompactLayout: Transaction IDs being passed: ${transactionsToUse.map { it.id }}")
                            HomeGroupSummary(
                                filteredTransactions = transactionsToUse,
                                totalIncome = incomeToUse,
                                totalExpense = expenseToUse,
                                currentFilter = state.currentFilter,
                                balance = balanceToUse,
                                periodStartDate = state.periodStartDate,
                                periodEndDate = state.periodEndDate,
                                isLoading = pagingItems.loadState.refresh is androidx.paging.LoadState.Loading,
                            )
                        }
                    } else {
                        null
                    }

            com.davidbugayov.financeanalyzer.presentation.components.paging.transactionPagingList(
                items = itemsToDisplay,
                categoriesViewModel = categoriesViewModel,
                onTransactionClick = onTransactionClick,
                onTransactionLongClick = onTransactionLongClick,
                listState = listState,
                headerContent = headerContent,
            )
        }
    }
}
