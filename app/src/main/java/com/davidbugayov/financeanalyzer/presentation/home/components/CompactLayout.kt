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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.presentation.home.state.HomeState
import timber.log.Timber

/**
 * Компактный макет для телефонов
 */
@Composable
fun CompactLayout(
    state: HomeState,
    showGroupSummary: Boolean,
    onShowGroupSummaryChange: (Boolean) -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onAddClick: () -> Unit
) {
    // Используем Column вместо LazyColumn для основного контента
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Добавляем большой отступ сверху для гарантии, что контент не накладывается на TopAppBar
        Spacer(modifier = Modifier.height(24.dp))
        
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
        )

        // Определяем, нужно ли показывать LazyColumn или сообщение "Нет транзакций"
        when {
            !state.isLoading && state.transactions.isEmpty() -> {
                // Глобально пусто — показываем красивое пустое состояние
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                            contentDescription = "Пусто",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .size(48.dp)
                        )
                        Text(
                            text = "Здесь пока пусто",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Добавьте свою первую транзакцию, чтобы начать анализировать финансы!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        androidx.compose.material3.Button(onClick = onAddClick) {
                            Text("Добавить первую транзакцию")
                        }
                    }
                }
            }

            !state.isLoading && state.filteredTransactions.isEmpty() -> {
                // Есть транзакции, но в выбранном фильтре их нет
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (state.currentFilter) {
                            TransactionFilter.TODAY -> "Нет транзакций за сегодня"
                            TransactionFilter.WEEK -> "Нет транзакций за эту неделю"
                            TransactionFilter.MONTH -> "Нет транзакций за этот месяц"
                            TransactionFilter.ALL -> "Нет транзакций"
                        },
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                // Показываем список транзакций (LazyColumn)
                Timber.d("CompactLayout: Отображаем LazyColumn (список транзакций или индикатор загрузки)")
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
        }
    }
} 