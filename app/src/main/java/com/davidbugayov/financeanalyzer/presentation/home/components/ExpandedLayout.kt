package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import timber.log.Timber

/**
 * Расширенный макет для планшетов
 */
@Composable
fun ExpandedLayout(
    state: HomeState,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ExpandedLeftPanel(
            state = state,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
            onFilterSelected = onFilterSelected
        )
        ExpandedRightPanel(
            state = state,
            showGroupSummary = showGroupSummary,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick
        )
    }
}

@Composable
private fun ExpandedLeftPanel(
    state: HomeState,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(end = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BalanceCard(balance = state.balance)
        Spacer(modifier = Modifier.height(16.dp))
        HomeFilterChips(
            currentFilter = state.currentFilter,
            onFilterSelected = onFilterSelected
        )
        HomeTransactionsHeader(
            currentFilter = state.currentFilter,
            showGroupSummary = showGroupSummary,
            onToggleGroupSummary = onToggleGroupSummary,
        )
        if (showGroupSummary && state.filteredTransactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HomeGroupSummary(
                filteredTransactions = state.filteredTransactions,
                totalIncome = state.filteredIncome,
                totalExpense = state.filteredExpense,
                currentFilter = state.currentFilter,
                balance = state.filteredBalance
            )
        }
    }
}

@Composable
private fun ExpandedRightPanel(
    state: HomeState,
    showGroupSummary: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 8.dp)
    ) {
        when {
            !state.isLoading && state.filteredTransactions.isEmpty() -> {
                ExpandedEmptyState(onAddClick)
            }
            else -> {
                ExpandedTransactionList(
                    state = state,
                    showGroupSummary = showGroupSummary,
                    onTransactionClick = onTransactionClick,
                    onTransactionLongClick = onTransactionLongClick
                )
            }
        }
    }
}

@Composable
private fun ExpandedEmptyState(onAddClick: () -> Unit) {
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
private fun ExpandedTransactionList(
    state: HomeState,
    showGroupSummary: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit
) {
    val lazyListState = rememberLazyListState()
    LaunchedEffect(showGroupSummary) {
        if (showGroupSummary && state.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
            Timber.d("ExpandedLayout: Скроллим к началу списка при показе сводки")
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = state.filteredTransactions,
            key = { it.id },
            contentType = { "transaction" }
        ) { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = onTransactionClick,
                onTransactionLongClick = onTransactionLongClick,
                showDivider = true
            )
        }
    }
} 