package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

            // Удаляем сводку из левой колонки, теперь она будет в LazyColumn
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
                // ЛОГИРОВАНИЕ: Проверяем состояние перед отображением списка/сообщения
                Timber.d("ExpandedLayout: filteredTransactions.isEmpty=${state.filteredTransactions.isEmpty()}, isLoading=${state.isLoading}, filter=${state.currentFilter}")
                
                // Если список отфильтрованных транзакций пуст И загрузка не идет, показываем сообщение
                if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                    Timber.d("ExpandedLayout: Отображаем сообщение 'Нет транзакций'") // ЛОГ
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp), // Добавляем отступ снизу
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (state.currentFilter) {
                                TransactionFilter.TODAY -> stringResource(R.string.no_transactions_today)
                                TransactionFilter.WEEK -> stringResource(R.string.no_transactions_week)
                                TransactionFilter.MONTH -> stringResource(R.string.no_transactions_month)
                            },
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center // Центрируем текст
                        )
                    }
                } else {
                    // Иначе (если есть транзакции ИЛИ идет загрузка), показываем LazyColumn
                    Timber.d("ExpandedLayout: Отображаем LazyColumn (список транзакций или индикатор загрузки)") // ЛОГ
                    val lazyListState = rememberLazyListState()
                    
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Добавляем сводку по группам, если она нужна и есть транзакции
                        if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
                            item {
                                HomeGroupSummary(
                                    groups = state.transactionGroups,
                                    totalIncome = state.filteredIncome,
                                    totalExpense = state.filteredExpense
                                )
                            }
                        }
                        
                        // Добавляем транзакции с виртуализацией и contentType
                        items(
                            items = state.filteredTransactions,
                            key = { it.id },
                            contentType = { "transaction" } // Указываем тип контента для оптимизации рекомпозиций
                        ) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = onTransactionClick,
                                onLongClick = onTransactionLongClick,
                                // Разделитель включаем только если это не последний элемент
                                showDivider = true
                            )
                            // Отступ между элементами создаем через padding в TransactionItem для уменьшения иерархии представлений
                        }
                        
                        // Добавляем большой отступ внизу для нижней панели навигации
                        item {
                            Spacer(modifier = Modifier.height(140.dp))
                        }
                    }
                }
            }
        }
    }
} 