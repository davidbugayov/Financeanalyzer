package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.davidbugayov.financeanalyzer.R
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
    // Используем фиксированный списочный стейт для улучшения управления скроллингом
    val listState = rememberLazyListState()
    
    // Наблюдаем за жизненным циклом для оптимизации обновлений
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Отключаем обновления, когда экран неактивен
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Возможно выполнять действия по остановке ненужных операций
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Кэшируем обновленные колбэки для предотвращения ненужных перерисовок
    val updateOnClick = rememberUpdatedState(onTransactionClick)
    val updateOnLongClick = rememberUpdatedState(onTransactionLongClick)

    // Оптимизация запроса дополнительных данных
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && 
            lastVisibleItem >= totalItems - 3 && // Уменьшаем буфер для производительности
            hasMoreData && !isLoading
        }
    }

    // Запрашиваем дополнительные данные с тротлингом для предотвращения частых вызовов
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad) {
                    onLoadMore()
                }
            }
    }

    // Кэшируем состояние раскрытия групп
    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            transactionGroups.forEach { group -> put(group.date, true) }
        }
    }
    
    // Обновляем состояние раскрытия только для новых групп
    LaunchedEffect(transactionGroups) {
        transactionGroups.forEach { group -> 
            if (!expandedState.containsKey(group.date)) {
                expandedState[group.date] = true
            }
        }
    }
    
    // Кэшируем обработчики событий раскрытия
    val expandHandlers = remember {
        mutableMapOf<String, (Boolean) -> Unit>()
    }
    LaunchedEffect(transactionGroups) {
        transactionGroups.forEach { group ->
            if (!expandHandlers.containsKey(group.date)) {
                expandHandlers[group.date] = { 
                    expandedState[group.date] = it 
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.spacing_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_tiny)),
        ) {
            transactionGroups.forEach { group ->
                val date = group.date
                
                // Заголовок группы
                item(key = "header_$date") {
                    GroupHeader(
                        period = date,
                        transactions = group.transactions,
                        isExpanded = expandedState[date] ?: true,
                        onExpandToggle = expandHandlers[date] ?: { 
                            expandedState[date] = it 
                        }
                    )
                }

                // Транзакции, если группа раскрыта
                if (expandedState[date] == true) {
                    items(
                        items = group.transactions,
                        key = { transaction -> "tx_${transaction.id}" }
                    ) { transaction ->
                        // Используем одни и те же колбэки для всех элементов
                        TransactionItemWithActions(
                            transaction = transaction,
                            onClick = { updateOnClick.value(transaction) },
                            onLongClick = { updateOnLongClick.value(transaction) }
                        )
                    }
                }
                
                // Маленький разделитель между группами
                item(key = "spacer_$date") {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // Индикатор загрузки
            if (isLoading) {
                item(key = "loading") {
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
            if (!hasMoreData && transactionGroups.isNotEmpty()) {
                item(key = "end_message") {
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

        // Показываем индикатор загрузки на весь экран
        if (transactionGroups.isEmpty() && isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
} 