package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import timber.log.Timber

/**
 * Компактный макет для телефонов
 */
@Composable
private fun CompactBalanceAndFilters(
    state: HomeState,
    onFilterSelected: (TransactionFilter) -> Unit,
    onToggleGroupSummary: (Boolean) -> Unit,
    showGroupSummary: Boolean
) {
    BalanceCard(balance = state.balance)
    state.error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
    HomeFilterChips(
        currentFilter = state.currentFilter,
        onFilterSelected = onFilterSelected
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                contentDescription = stringResource(R.string.empty_state_icon_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(48.dp)
            )
            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.empty_state_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            androidx.compose.material3.Button(onClick = onAddClick) {
                Text(stringResource(R.string.empty_state_add_first_transaction))
            }
        }
    }
}

@Composable
private fun CompactNoFilteredState(currentFilter: TransactionFilter) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (currentFilter) {
                TransactionFilter.TODAY -> stringResource(R.string.no_transactions_today)
                TransactionFilter.WEEK -> stringResource(R.string.no_transactions_week)
                TransactionFilter.MONTH -> stringResource(R.string.no_transactions_month)
                TransactionFilter.ALL -> stringResource(R.string.no_transactions_all)
            },
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompactTransactionList(
    state: HomeState,
    showGroupSummary: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit
) {
    val lazyListState = rememberLazyListState()
    LaunchedEffect(showGroupSummary) {
        if (showGroupSummary && state.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
            Timber.d("CompactLayout: Скроллим к началу списка при показе сводки")
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
            item {
                HomeGroupSummary(
                    filteredTransactions = state.filteredTransactions,
                    totalIncome = state.filteredIncome,
                    totalExpense = state.filteredExpense,
                    currentFilter = state.currentFilter,
                    balance = state.filteredBalance
                )
            }
        }
        items(
            items = state.filteredTransactions,
            key = { it.id },
            contentType = { "transaction" }
        ) { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = onTransactionClick,
                onLongClick = onTransactionLongClick,
                showDivider = true
            )
        }
    }
}

@Composable
fun CompactLayout(
    state: HomeState,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        CompactBalanceAndFilters(
            state = state,
            onFilterSelected = onFilterSelected,
            onToggleGroupSummary = onToggleGroupSummary,
            showGroupSummary = showGroupSummary
        )
        when {
            !state.isLoading && state.transactions.isEmpty() -> {
                CompactEmptyState(onAddClick)
            }

            !state.isLoading && state.filteredTransactions.isEmpty() -> {
                CompactNoFilteredState(state.currentFilter)
            }

            else -> {
                CompactTransactionList(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick
                )
            }
        }
    }
} 