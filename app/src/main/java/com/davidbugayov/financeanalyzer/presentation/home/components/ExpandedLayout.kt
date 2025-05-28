package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
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
    categoriesViewModel: CategoriesViewModel,
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
            onFilterSelected = onFilterSelected,
            modifier = Modifier.weight(1f)
        )
        ExpandedRightPanel(
            state = state,
            categoriesViewModel = categoriesViewModel,
            showGroupSummary = showGroupSummary,
            onTransactionClick = onTransactionClick,
            onTransactionLongClick = onTransactionLongClick,
            onAddClick = onAddClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExpandedLeftPanel(
    state: HomeState,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
    categoriesViewModel: CategoriesViewModel,
    showGroupSummary: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Timber.d("[UI] ExpandedRightPanel: isLoading=%s, filteredTransactions.isEmpty()=%s", state.isLoading, state.filteredTransactions.isEmpty())
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            !state.isLoading && state.filteredTransactions.isEmpty() -> {
                Timber.d("[UI] Показываем ExpandedEmptyState (пустое состояние)")
                ExpandedEmptyState(onAddClick)
            }
            else -> {
                ExpandedTransactionList(
                    state = state,
                    categoriesViewModel = categoriesViewModel,
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
    Timber.d("[UI] ExpandedEmptyState отображается")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Filled.Add,
            contentDescription = stringResource(R.string.empty_state_icon_desc),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(36.dp)
        )
        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = stringResource(R.string.empty_state_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        androidx.compose.material3.Button(
            onClick = {
                Timber.d("[UI] Нажата кнопка 'Добавить первую транзакцию' в ExpandedEmptyState")
                onAddClick()
            },
            shape = RoundedCornerShape(50),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(min = 44.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_state_add_first_transaction),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ExpandedTransactionList(
    state: HomeState,
    categoriesViewModel: CategoriesViewModel,
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
                categoriesViewModel = categoriesViewModel,
                onClick = onTransactionClick,
                onTransactionLongClick = onTransactionLongClick,
            )
        }
    }
} 