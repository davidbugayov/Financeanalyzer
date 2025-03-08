package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

/**
 * Основной компонент для отображения истории транзакций.
 * Отображает список групп транзакций с возможностью сворачивания/разворачивания.
 *
 * @param transactionGroups Список групп транзакций для отображения
 * @param onTransactionClick Callback, вызываемый при нажатии на транзакцию
 * @param onTransactionLongClick Callback, вызываемый при долгом нажатии на транзакцию
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun TransactionHistory(
    transactionGroups: List<TransactionGroup>,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Используем rememberLazyListState для оптимизации прокрутки
    val listState = rememberLazyListState()

    // Кэшируем список групп для предотвращения ненужных перерисовок
    val groups = remember(transactionGroups) { transactionGroups }
    
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = groups,
            key = { _, group -> group.date }
        ) { _, group ->
            TransactionGroupItem(
                group = group,
                onTransactionClick = onTransactionClick,
                onTransactionLongClick = onTransactionLongClick
            )
        }
    }
}

/**
 * Компонент для отображения группы транзакций.
 * Включает заголовок с суммарной информацией и список транзакций.
 *
 * @param group Группа транзакций для отображения
 * @param onTransactionClick Callback, вызываемый при нажатии на транзакцию
 * @param onTransactionLongClick Callback, вызываемый при долгом нажатии на транзакцию
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun TransactionGroupItem(
    group: TransactionGroup,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    // Кэшируем данные группы для предотвращения ненужных перерисовок
    val date = remember(group) { group.date }
    val balance = remember(group) { group.balance }
    val transactions = remember(group) { group.transactions }

    // Кэшируем форматированные значения
    val amount = remember(balance) { balance.format(false) }
    val amountColor = remember(balance) {
        if (balance >= Money.zero()) Color(0xFF4CAF50) else Color(0xFFF44336)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (balance >= Money.zero())
                        stringResource(R.string.income_currency_format, amount)
                    else
                        stringResource(R.string.currency_format, amount),
                    color = amountColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Используем AnimatedVisibility для анимации сворачивания/разворачивания
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Используем key для каждой транзакции, чтобы избежать ненужных перерисовок
                transactions.forEach { transaction ->
                    key(transaction.id) {
                        TransactionLongPressItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction) },
                            onLongClick = { onTransactionLongClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Заголовок группы транзакций с возможностью сворачивания/разворачивания.
 * Отображает период и суммарную информацию о транзакциях в группе.
 *
 * @param period Название периода (например, "Январь 2024")
 * @param transactions Список транзакций в группе
 */
@Composable
fun GroupHeader(
    period: String,
    transactions: List<Transaction>
) {
    var isExpanded by remember { mutableStateOf(true) }

    // Вычисляем суммы только при изменении списка транзакций
    val financialSummary = remember(transactions) {
        val income = transactions.filter { !it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val expense = transactions.filter { it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val balance = income - expense

        Triple(income, expense, balance)
    }

    val (income, expense, balance) = financialSummary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .shadow(elevation = 1.dp)
            .clickable { isExpanded = !isExpanded },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GroupSummary(
                    income = income,
                    expense = expense,
                    balance = balance,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Компонент для отображения одной транзакции в истории.
 * Использует общий компонент TransactionItem.
 *
 * @param transaction Транзакция для отображения
 */
@Composable
fun TransactionHistoryItem(
    transaction: Transaction
) {
    // Используем key для предотвращения ненужных перерисовок
    key(transaction.id) {
        TransactionItem(transaction = transaction)
    }
}

/**
 * Компонент для отображения группы транзакций с заголовком.
 * Объединяет GroupHeader и список транзакций.
 *
 * @param groupTitle Название группы
 * @param transactions Список транзакций в группе
 * @param onTransactionClick Callback, вызываемый при нажатии на транзакцию
 */
@Composable
fun TransactionGroup(
    groupTitle: String,
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    // Кэшируем список транзакций для предотвращения ненужных перерисовок
    val transactionsList = remember(transactions) { transactions }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        GroupHeader(
            period = groupTitle,
            transactions = transactionsList
        )

        transactionsList.forEach { transaction ->
            key(transaction.id) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTransactionClick(transaction) }
                ) {
                    TransactionHistoryItem(transaction = transaction)
                }
            }
        }
    }
}

/**
 * Компонент для отображения сводки по группе транзакций
 */
@Composable
fun GroupSummary(
    income: Money,
    expense: Money,
    balance: Money,
    modifier: Modifier = Modifier
) {
    // Кэшируем форматированные значения
    val formattedIncome = remember(income) { income.format(false) }
    val formattedExpense = remember(expense) { expense.format(false) }
    val formattedBalance = remember(balance) { balance.format(false) }
    val balanceColor = remember(balance) {
        if (balance >= Money.zero()) Color(0xFF4CAF50) else Color(0xFFF44336)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.income),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.income_currency_format, formattedIncome),
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.expense),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.expense_currency_format, formattedExpense),
                    fontSize = 14.sp,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.balance),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = if (balance >= Money.zero())
                        stringResource(R.string.income_currency_format, formattedBalance)
                    else
                        stringResource(R.string.expense_currency_format, formattedBalance),
                    fontSize = 14.sp,
                    color = balanceColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Компонент для отображения списка транзакций с пагинацией.
 * Использует LazyColumn для эффективного отображения больших списков.
 *
 * @param groupedTransactions Сгруппированные транзакции для отображения
 */
@Composable
fun TransactionsList(
    groupedTransactions: Map<String, List<Transaction>>
) {
    // Используем rememberLazyListState для оптимизации прокрутки
    val listState = rememberLazyListState()

    // Кэшируем сгруппированные транзакции для предотвращения ненужных перерисовок
    val groups = remember(groupedTransactions) { groupedTransactions }

    // Получаем список периодов (ключей) для использования в качестве ключей элементов списка
    val periods = remember(groups) { groups.keys.toList() }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        periods.forEach { period ->
            val transactions = groups[period] ?: emptyList()

            item(key = "header_$period") {
                GroupHeader(
                    period = period,
                    transactions = transactions
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            itemsIndexed(
                items = transactions,
                key = { _, transaction -> "transaction_${transaction.id}" }
            ) { _, transaction ->
                TransactionHistoryItem(transaction = transaction)
            }

            item(key = "spacer_$period") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ComparisonCard(
    currentTotal: Money,
    previousTotal: Money
) {
    // Кэшируем форматированные значения
    val formattedCurrent = remember(currentTotal) { currentTotal.format(false) }
    val formattedPrevious = remember(previousTotal) { previousTotal.format(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = formattedCurrent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formattedPrevious,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TransactionHistoryGroupSummary(transactions: List<Transaction>) {
    // Получаем цвета из темы
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current

    // Кэшируем вычисления
    val financialSummary = remember(transactions) {
        val income = transactions
            .filter { !it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val expense = transactions
            .filter { it.isExpense }
            .map { it.amount }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val balance = income - expense

        Triple(income, expense, balance)
    }

    val (income, expense, balance) = financialSummary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(R.string.income),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_format, income.format(false)),
                    fontSize = 14.sp,
                    color = incomeColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.expense),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_format, expense.format(false)),
                    fontSize = 14.sp,
                    color = expenseColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.balance),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.currency_format, balance.format(false)),
                    fontSize = 14.sp,
                    color = if (balance >= Money.zero()) incomeColor else expenseColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 