package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
            onShowAllClick = onNavigateToHistory
        )

        // Список транзакций теперь использует LazyColumn для виртуализации
        Box(modifier = Modifier.weight(1f)) {
            // ЛОГИРОВАНИЕ: Проверяем состояние перед отображением списка/сообщения
            Timber.d("CompactLayout: filteredTransactions.isEmpty=${state.filteredTransactions.isEmpty()}, isLoading=${state.isLoading}, filter=${state.currentFilter}")

            // Если список отфильтрованных транзакций пуст И загрузка не идет, показываем сообщение
            if (state.filteredTransactions.isEmpty() && !state.isLoading) {
                Timber.d("CompactLayout: Отображаем сообщение 'Нет транзакций'") // ЛОГ
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Заполняем все доступное пространство
                        .padding(bottom = 100.dp), // Добавляем отступ снизу, чтобы сообщение не перекрывалось нижней панелью
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
                Timber.d("CompactLayout: Отображаем LazyColumn (список транзакций или индикатор загрузки)") // ЛОГ
                val lazyListState = rememberLazyListState()
                
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Удаляем хинт для noTransactionsForPeriod, так как он больше не нужен
                    
                    // Добавляем сводку как первый элемент списка, если она нужна и есть транзакции
                    if (state.filteredTransactions.isNotEmpty() && showGroupSummary) {
                        item {
                            HomeGroupSummary(
                                groups = state.transactionGroups,
                                totalIncome = state.filteredIncome,
                                totalExpense = state.filteredExpense
                            )
                        }
                    }
                    
                    // Добавляем транзакции
                    items(
                        items = state.filteredTransactions,
                        key = { it.id } // Используем уникальный ID как ключ для лучшей производительности
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = onTransactionClick,
                            onLongClick = onTransactionLongClick
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Добавляем пространство внизу для нижней панели
                    item {
                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }
    }
} 