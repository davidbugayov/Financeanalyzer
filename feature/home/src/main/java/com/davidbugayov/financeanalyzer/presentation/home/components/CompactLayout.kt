package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import timber.log.Timber
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidbugayov.financeanalyzer.presentation.components.paging.TransactionPagingList
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import androidx.paging.compose.LazyPagingItems
import androidx.compose.foundation.lazy.LazyListState
import androidx.paging.LoadState

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
    BalanceCard(balance = state.balance)
    state.error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 4.dp),
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

@Composable
private fun CompactEmptyState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                contentDescription = stringResource(R.string.empty_state_icon_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(36.dp),
            )
            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = stringResource(R.string.empty_state_subtitle),
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
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 44.dp),
            ) {
                Text(
                    text = stringResource(R.string.empty_state_add_first_transaction),
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
        Timber.d("UI: CompactTransactionList - isLoading изменился на: ${state.isLoading}")
    }
    
    LaunchedEffect(state.filteredTransactions.size) {
        Timber.d("UI: CompactTransactionList - количество транзакций изменилось на: ${state.filteredTransactions.size}")
    }
    
    LaunchedEffect(showGroupSummary) {
        if (showGroupSummary && state.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
            Timber.d("CompactLayout: Скроллим к началу списка при показе сводки")
        }
    }
    
    // Отслеживаем количество транзакций для анимации новых элементов
    val previousTransactionCount = remember { mutableStateOf(0) }
    val currentTransactionCount = state.filteredTransactions.size
    
    Timber.d("UI: CompactTransactionList рендеринг - isLoading: ${state.isLoading}, транзакций: ${state.filteredTransactions.size}")
    
    val pagingItems = remember { com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel::class }
    // NOTE: viewModel not accessible here; require param. Simplify: keep old for now.
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
    ) {
        CompactBalanceAndFilters(
            state = state,
            onFilterSelected = onFilterSelected,
            onToggleGroupSummary = onToggleGroupSummary,
            showGroupSummary = showGroupSummary,
        )
        when {
            !state.isLoading && state.filteredTransactions.isEmpty() -> {
                CompactEmptyState(onAddClick)
            }
            else -> {
                TransactionPagingList(
                    items = pagingItems,
                    categoriesViewModel = categoriesViewModel,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick,
                    listState = listState,
                )
            }
        }
    }
}
