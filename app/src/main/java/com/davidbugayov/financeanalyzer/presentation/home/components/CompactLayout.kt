package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState

/**
 * Компактный макет для телефонов
 */
@Composable
fun CompactLayout(
    state: HomeState,
    showGroupSummary: Boolean,
    onShowGroupSummaryChange: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onNavigateToHistory: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit
) {
    // Используем Column вместо LazyColumn для основного контента
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 88.dp) // Добавляем отступ снизу для кнопок
    ) {
        // Карточка с балансом
        BalanceCard(
            balance = state.balance
        )

        // Сообщение об ошибке
        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Фильтры транзакций
        HomeFilterChips(
            currentFilter = state.currentFilter,
            onFilterSelected = onFilterSelected
        )

        // Заголовок для транзакций с кнопкой для отображения/скрытия сводки
        HomeTransactionsHeader(
            currentFilter = state.currentFilter,
            showGroupSummary = showGroupSummary,
            onShowGroupSummaryChange = onShowGroupSummaryChange,
            onShowAllClick = onNavigateToHistory
        )

        // Список транзакций и сводка - теперь в одном скроллируемом контейнере
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Отображение суммы для выбранного периода
                if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
                    HomeGroupSummary(
                        groups = state.transactionGroups,
                        totalIncome = state.filteredIncome,
                        totalExpense = state.filteredExpense
                    )
                }

                // Список транзакций
                if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                    // Отображаем сообщение, если нет транзакций
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
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