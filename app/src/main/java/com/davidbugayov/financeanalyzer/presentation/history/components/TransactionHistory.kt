package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItemWithActions

/**
 * Основной компонент для отображения истории транзакций.
 * Отображает список групп транзакций.
 *
 * @param transactionGroups Список групп транзакций для отображения
 * @param modifier Модификатор для настройки внешнего вида
 * @param onTransactionClick Callback, вызываемый при нажатии на транзакцию
 * @param onTransactionLongClick Callback, вызываемый при долгом нажатии на транзакцию
 */
@Composable
fun TransactionHistory(
    transactionGroups: List<TransactionGroup>,
    modifier: Modifier = Modifier,
    onTransactionClick: (Transaction) -> Unit = {},
    onTransactionLongClick: (Transaction) -> Unit = {}
) {
    val listState = rememberLazyListState()

    // Кэшируем список групп для предотвращения ненужных перерисовок
    val groups = remember(transactionGroups) { transactionGroups }

    // Создаем карту для хранения состояния развернутости каждой группы
    // По умолчанию все группы развернуты (true)
    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            groups.forEach { group -> put(group.date, true) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        items(
            items = groups,
            key = { group -> group.date }
        ) { group ->
            // Заголовок группы с возможностью сворачивания/разворачивания
            GroupHeader(
                period = group.date,
                transactions = group.transactions,
                isExpanded = expandedState[group.date] ?: true,
                onExpandToggle = { isExpanded ->
                    expandedState[group.date] = isExpanded
                }
            )

            // Список транзакций в группе, отображаем только если группа развернута
            if (expandedState[group.date] == true) {
                group.transactions.forEach { transaction ->
                    TransactionItemWithActions(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                        onLongClick = { onTransactionLongClick(transaction) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        }
    }
} 