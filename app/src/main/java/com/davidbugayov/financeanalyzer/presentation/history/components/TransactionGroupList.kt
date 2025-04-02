package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Компонент для отображения сгруппированных транзакций с поддержкой пагинации.
 * Оптимизирован для улучшения производительности при большом количестве данных.
 *
 * @param transactionGroups Сгруппированные транзакции
 * @param onTransactionClick Обработчик клика по транзакции
 * @param onTransactionLongClick Обработчик долгого нажатия на транзакцию
 * @param onLoadMore Функция загрузки следующей страницы транзакций
 * @param isLoading Идет ли загрузка дополнительных транзакций
 * @param hasMoreData Есть ли еще данные для загрузки
 */
@Composable
fun TransactionGroupList(
    transactionGroups: List<TransactionGroup>,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onLoadMore: () -> Unit,
    isLoading: Boolean = false,
    hasMoreData: Boolean = false
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Используем mutableStateMapOf для хранения состояния раскрытия групп
    // По умолчанию все группы раскрыты, кроме последних двух
    val expandedGroups = remember(transactionGroups) {
        mutableStateMapOf<String, Boolean>().apply {
            transactionGroups.forEachIndexed { index, group ->
                this[group.date] = index < 2 // Автоматически раскрываем только первые 2 группы
            }
        }
    }
    
    // Проверяем, нужно ли загружать больше данных
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemCount = listState.layoutInfo.totalItemsCount
            
            hasMoreData && !isLoading && lastVisibleItem >= totalItemCount - 5
        }
    }
    
    // Загружаем больше данных, если нужно
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth()
    ) {
        transactionGroups.forEach { group ->
            val isExpanded = expandedGroups[group.date] ?: false
            
            // Заголовок группы
            item(key = "header_${group.date}") {
                ExpandableGroupHeader(
                    date = group.date,
                    balance = group.balance,
                    isExpanded = isExpanded,
                    onToggle = { expanded ->
                        expandedGroups[group.date] = expanded
                        // Прокручиваем к заголовку, если группа свернута
                        if (!expanded) {
                            coroutineScope.launch {
                                // Находим индекс текущего заголовка и прокручиваем к нему
                                val headerIndex = transactionGroups.indexOfFirst { it.date == group.date }
                                if (headerIndex >= 0) {
                                    listState.animateScrollToItem(headerIndex)
                                }
                            }
                        }
                    }
                )
            }
            
            // Список транзакций в группе, показываем только если группа развернута
            if (isExpanded) {
                items(
                    items = group.transactions,
                    key = { transaction -> "transaction_${transaction.id}" }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                        onLongClick = { onTransactionLongClick(transaction) }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
            
            // Разделитель между группами
            item(key = "spacer_${group.date}") {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.spacing_small))
                )
            }
        }
        
        // Индикатор загрузки внизу списка
        if (isLoading) {
            item(key = "loading_indicator") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Заголовок для группы транзакций с возможностью сворачивания/разворачивания
 */
@Composable
private fun ExpandableGroupHeader(
    date: String,
    balance: Money,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_normal),
                vertical = dimensionResource(id = R.dimen.spacing_small)
            )
            .clickable { onToggle(!isExpanded) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.spacing_normal)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка для сворачивания/разворачивания
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            val balanceText = balance.formatted(showSign = true)
            val balanceColor = when {
                balance.isPositive() -> MaterialTheme.colorScheme.primary
                balance.isNegative() -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
            
            Text(
                text = balanceText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
        }
    }
} 