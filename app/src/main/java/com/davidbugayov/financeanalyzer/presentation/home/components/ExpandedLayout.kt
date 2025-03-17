package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItemWithActions
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState

/**
 * Расширенный макет для планшетов
 */
@Composable
fun ExpandedLayout(
    state: HomeState,
    showGroupSummary: Boolean,
    onShowGroupSummaryChange: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onNavigateToHistory: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit
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
                .verticalScroll(rememberScrollState())
        ) {
            BalanceCard(
                balance = state.balance
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeFilterChips(
                currentFilter = state.currentFilter,
                onFilterSelected = onFilterSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Отображение суммы для выбранного периода
            if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
                HomeGroupSummary(
                    groups = state.transactionGroups,
                    totalIncome = state.filteredIncome,
                    totalExpense = state.filteredExpense
                )
            }
        }

        // Правая панель со списком транзакций
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            // Заголовок для транзакций с кнопкой для отображения/скрытия сводки
            HomeTransactionsHeader(
                currentFilter = state.currentFilter,
                showGroupSummary = showGroupSummary,
                onShowGroupSummaryChange = onShowGroupSummaryChange,
                onShowAllClick = onNavigateToHistory
            )

            // Список транзакций
            Box(modifier = Modifier.weight(1f)) {
                if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                    // Отображаем сообщение, если нет транзакций
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                    // Отображаем список транзакций
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        state.filteredTransactions.forEachIndexed { index, transaction ->
                            TransactionItemWithActions(
                                transaction = transaction,
                                onClick = onTransactionClick,
                                onLongClick = onTransactionLongClick
                            )
                            if (index < state.filteredTransactions.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
} 