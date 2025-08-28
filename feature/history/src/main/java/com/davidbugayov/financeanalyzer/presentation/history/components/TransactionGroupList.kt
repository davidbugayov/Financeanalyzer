package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.transactionItem
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Компонент для отображения сгруппированных транзакций с поддержкой пагинации.
 * Оптимизирован для улучшения производительности при большом количестве данных.
 *
 * @param transactionGroups Сгруппированные транзакции
 * @param categoriesViewModel ViewModel для категорий
 * @param onTransactionClick Обработчик клика по транзакции
 * @param onTransactionLongClick Обработчик долгого нажатия на транзакцию
 * @param onLoadMore Функция загрузки следующей страницы транзакций
 * @param isLoading Идет ли загрузка дополнительных транзакций
 * @param hasMoreData Есть ли еще данные для загрузки
 */
@Composable
fun TransactionGroupList(
    transactionGroups: List<TransactionGroup>,
    categoriesViewModel: CategoriesViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    onLoadMore: () -> Unit,
    isLoading: Boolean = false,
    hasMoreData: Boolean = false,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    // Используем mutableStateMapOf для хранения состояния раскрытия групп
    // По умолчанию все группы свернуты
    val expandedGroups =
        remember(transactionGroups) {
            mutableStateMapOf<String, Boolean>().apply {
                transactionGroups.forEachIndexed { index, group ->
                    this[dateFormat.format(group.date)] = false // Все группы свернуты по умолчанию
                }
            }
        }

    // Проверяем, нужно ли загружать больше данных
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem =
                listState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
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
        modifier = Modifier.fillMaxWidth(),
    ) {
        transactionGroups.forEachIndexed { groupIndex, group ->
            val formattedDate = dateFormat.format(group.date)
            val isExpanded = expandedGroups[formattedDate] == true

            // Заголовок группы
            item(key = "header_${groupIndex}_$formattedDate") {
                ExpandableGroupHeader(
                    date = group.displayPeriod,
                    balance = group.balance,
                    isExpanded = isExpanded,
                    onToggle = { expanded ->
                        expandedGroups[formattedDate] = expanded
                        // Прокручиваем к заголовку, если группа свернута
                        if (!expanded) {
                            coroutineScope.launch {
                                // Находим индекс текущего заголовка и прокручиваем к нему
                                val headerIndex =
                                    transactionGroups.indexOfFirst {
                                        dateFormat.format(it.date) ==
                                            formattedDate
                                    }
                                if (headerIndex >= 0) {
                                    var currentItemIndex = 0
                                    for (i in 0 until headerIndex) {
                                        currentItemIndex++ // For the header
                                        if (expandedGroups[dateFormat.format(transactionGroups[i].date)] == true) {
                                            currentItemIndex += transactionGroups[i].transactions.size // For transactions in this group
                                        }
                                        currentItemIndex++ // For the spacer
                                    }
                                    if (currentItemIndex >= 0) {
                                        listState.animateScrollToItem(currentItemIndex)
                                    }
                                }
                            }
                        }
                    },
                )
            }

            // Список транзакций в группе, показываем только если группа развернута
            if (isExpanded) {
                items(
                    items = group.transactions,
                    key = { transaction -> "transaction_${groupIndex}_${transaction.id}" },
                ) { transaction ->
                    com.davidbugayov.financeanalyzer.presentation.components.transactionItem(
                        transaction = transaction,
                        categoriesViewModel = categoriesViewModel,
                        onClick = { onTransactionClick(transaction) },
                        onTransactionLongClick = { onTransactionLongClick(transaction) },
                        animationDelay = 0L,
                        animated = false,
                    )

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = dimensionResource(id = UiR.dimen.spacing_normal),
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    )
                }
            }

            // Разделитель между группами
            item(key = "spacer_${groupIndex}_$formattedDate") {
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = UiR.dimen.spacing_small)),
                )
            }
        }

        // Индикатор загрузки внизу списка
        if (isLoading) {
            item(key = "loading_indicator") {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = UiR.dimen.spacing_normal)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(dimensionResource(id = UiR.dimen.spacing_small)),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Spacer для отступа под FloatingActionButton
        item(key = "fab_spacer") {
            Spacer(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = UiR.dimen.height_empty_state)),
            )
        }
    }
}

/**
 * Заголовок для группы транзакций с возможностью сворачивания/разворачивания
 */
@Composable
private fun ExpandableGroupHeader(
    date: String,
    balance: Double,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val isPositive = balance >= 0
    val primaryColor = if (isPositive) incomeColor else expenseColor

    // Градиент для заголовка
    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.08f),
            primaryColor.copy(alpha = 0.04f),
            primaryColor.copy(alpha = 0.01f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable { onToggle(!isExpanded) },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            color = primaryColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerGradient)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Левая часть с датой и иконкой
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) {
                            stringResource(UiR.string.collapse)
                        } else {
                            stringResource(UiR.string.expand)
                        },
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Правая часть с суммой и иконкой тренда
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Иконка тренда
                    Icon(
                        imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )

                    // Сумма
                    Text(
                        text = if (isPositive) {
                            "+${Money.fromMajor(balance).formatForDisplay(showCurrency = false)}"
                        } else {
                            "-${Money.fromMajor(balance).abs().formatForDisplay(showCurrency = false)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
