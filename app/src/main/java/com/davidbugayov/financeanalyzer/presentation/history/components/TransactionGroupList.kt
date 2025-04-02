package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.davidbugayov.financeanalyzer.domain.model.amountFormatted
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Компонент для отображения сгруппированных транзакций с поддержкой пагинации.
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
    
    // Проверяем, нужно ли загружать больше данных
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemCount = listState.layoutInfo.totalItemsCount
            
            hasMoreData && !isLoading && lastVisibleItem >= totalItemCount - 3
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
            // Заголовок группы
            item {
                GroupHeader(
                    date = group.date,
                    balance = group.balance
                )
            }
            
            // Список транзакций в группе
            items(
                items = group.transactions,
                key = { transaction -> transaction.id }
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
            
            // Разделитель между группами
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.spacing_medium))
                )
            }
        }
        
        // Индикатор загрузки внизу списка
        if (isLoading) {
            item {
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
 * Заголовок для группы транзакций
 */
@Composable
private fun GroupHeader(
    date: String,
    balance: Money
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.spacing_normal),
                vertical = dimensionResource(id = R.dimen.spacing_small)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.spacing_normal)),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (balance.isPositive()) "+" else if (balance.isNegative()) "-" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = balanceColor
                )
                Text(
                    text = balanceText.replace("+", "").replace("-", ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
    }
} 