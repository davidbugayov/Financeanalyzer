package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItemWithActions
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Основной компонент для отображения истории транзакций.
 * Отображает список групп транзакций с поддержкой бесконечной прокрутки.
 *
 * @param transactionGroups Список групп транзакций для отображения
 * @param modifier Модификатор для настройки внешнего вида
 * @param onTransactionClick Callback, вызываемый при нажатии на транзакцию
 * @param onTransactionLongClick Callback, вызываемый при долгом нажатии на транзакцию
 * @param onLoadMore Callback, вызываемый для загрузки дополнительных транзакций
 * @param isLoading Флаг загрузки дополнительных данных
 * @param hasMoreData Флаг наличия дополнительных данных для загрузки
 */
@Composable
fun TransactionHistory(
    transactionGroups: List<TransactionGroup>,
    modifier: Modifier = Modifier,
    onTransactionClick: (Transaction) -> Unit = {},
    onTransactionLongClick: (Transaction) -> Unit = {},
    onLoadMore: () -> Unit = {},
    isLoading: Boolean = false,
    hasMoreData: Boolean = true
) {
    val listState = rememberLazyListState()

    // Оптимизация: увеличение буфера предзагрузки с 5 до 10 элементов
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem > 0 && lastVisibleItem >= totalItems - 10 && hasMoreData && !isLoading
        }
    }

    // Запускаем загрузку дополнительных данных, когда нужно
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad) {
                    onLoadMore()
                }
            }
    }

    // Кэшируем список групп для предотвращения ненужных перерисовок
    val groups = remember(transactionGroups) { transactionGroups }

    // Создаем карту для хранения состояния развернутости каждой группы
    // По умолчанию все группы развернуты (true)
    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            groups.forEach { group -> put(group.date, true) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groups.forEach { group ->
                item(key = "header_${group.date}") {
                    // Заголовок группы с возможностью сворачивания/разворачивания
                    GroupHeader(
                        period = group.date,
                        transactions = group.transactions,
                        isExpanded = expandedState[group.date] ?: true,
                        onExpandToggle = { isExpanded ->
                            expandedState[group.date] = isExpanded
                        }
                    )
                }

                // Список транзакций в группе, отображаем только если группа развернута
                if (expandedState[group.date] == true) {
                    itemsIndexed(
                        items = group.transactions,
                        key = { _, transaction -> transaction.id }
                    ) { index, transaction ->
                        TransactionItemWithActions(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction) },
                            onLongClick = { onTransactionLongClick(transaction) }
                        )
                        
                        // Добавляем разделитель только между элементами, а не после последнего
                        if (index < group.transactions.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                item(key = "spacer_${group.date}") {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Индикатор загрузки в нижней части списка
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // Сообщение о конце списка
            item {
                if (!hasMoreData && groups.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Больше транзакций нет",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Показываем индикатор загрузки на весь экран, если список пуст и идет загрузка
        if (groups.isEmpty() && isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
} 