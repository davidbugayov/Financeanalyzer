package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
 * Расширенный макет для планшетов
 */
@Composable
fun ExpandedLayout(
    state: HomeState,
    categoriesViewModel: CategoriesViewModel,
    pagingItems: LazyPagingItems<TransactionListItem>,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
) {
    val listState: LazyListState = rememberLazyListState()

    LaunchedEffect(state.currentFilter) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is LoadState.NotLoading) {
            listState.scrollToItem(0)
        }
    }

    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ExpandedLeftPanel(
            state = state,
            pagingItems = pagingItems,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected,
            modifier = Modifier.weight(1f),
        )
        ExpandedRightPanel(
            state = state,
            categoriesViewModel = categoriesViewModel,
            pagingItems = pagingItems,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick,
            modifier = Modifier.weight(1f),
            listState = listState,
        )
    }
}

@Composable
private fun ExpandedLeftPanel(
    state: HomeState,
    pagingItems: LazyPagingItems<TransactionListItem>,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BalanceCard(balance = state.balance, income = state.income, expense = state.expense)

        HomeFilterChips(
            currentFilter = state.currentFilter,
            onFilterSelected = onFilterSelected,
        )

        // Tips card moved to transaction list header for scrolling interaction
        HomeTransactionsHeader(
            currentFilter = state.currentFilter,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
        )

        if (showGroupSummary) {
            HomeGroupSummary(
                filteredTransactions = state.filteredTransactions,
                totalIncome = state.filteredIncome,
                totalExpense = state.filteredExpense,
                currentFilter = state.currentFilter,
                balance = state.filteredBalance,
                periodStartDate = state.periodStartDate,
                periodEndDate = state.periodEndDate,
                isLoading = pagingItems.loadState.refresh is androidx.paging.LoadState.Loading,
            )
        }
    }
}

@Composable
private fun ExpandedRightPanel(
    state: HomeState,
    categoriesViewModel: CategoriesViewModel,
    pagingItems: LazyPagingItems<TransactionListItem>,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState,
) {
    Timber.d(
        "[UI] ExpandedRightPanel: isLoading=%s, filteredTransactions.isEmpty()=%s",
        state.isLoading,
        state.filteredTransactions.isEmpty(),
    )
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(start = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Определяем, показывать ли пустое состояние
        val isEmptyState = pagingItems.itemCount == 0 && pagingItems.loadState.refresh is androidx.paging.LoadState.NotLoading

        Timber.d("ExpandedLayout: isEmptyState=$isEmptyState, filteredTransactions.size=${state.filteredTransactions.size}, pagingItems.itemCount=${pagingItems.itemCount}, loadState=${pagingItems.loadState.refresh}")

        if (isEmptyState) {
            Timber.d("ExpandedLayout: Showing ExpandedEmptyState")
            ExpandedEmptyState(onAddClick)
        } else {
            Timber.d("ExpandedLayout: Showing transaction list")
            transactionPagingList(
                items = pagingItems,
                categoriesViewModel = categoriesViewModel,
                onTransactionClick = onTransactionClick,
                onTransactionLongClick = onTransactionLongClick,
                listState = listState,
                headerContent = {
                    // Показываем приветственную карточку только один раз (shared pref flag)
                    val context = LocalContext.current
                    val prefs = remember { context.getSharedPreferences("finance_analyzer_prefs", 0) }
                    var dismissed by remember { mutableStateOf(prefs.getBoolean("welcome_tips_dismissed", false)) }
                    if (!dismissed) {
                        HomeTipsCard(
                            onClose = {
                                prefs.edit().putBoolean("welcome_tips_dismissed", true).commit()
                                dismissed = true
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun ExpandedEmptyState(onAddClick: () -> Unit) {
    Timber.d("[UI] ExpandedEmptyState отображается")
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            modifier = Modifier.padding(bottom = 16.dp),
        )
        androidx.compose.material3.Button(
            onClick = {
                Timber.d("[UI] Нажата кнопка 'Добавить первую транзакцию' в ExpandedEmptyState")
                onAddClick()
            },
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
