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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.presentation.components.GroupSummary
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.util.formatTransactionAmount

@Composable
fun TransactionHistory(
    transactionGroups: List<TransactionGroup>,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactionGroups) { group ->
            TransactionGroupItem(
                group = group,
                onTransactionClick = onTransactionClick
            )
        }
    }
}

@Composable
fun TransactionGroupItem(
    group: TransactionGroup,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

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
                text = group.date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val amount = formatTransactionAmount(group.balance)
                val formattedAmount = stringResource(
                    if (group.balance >= 0) R.string.income_currency_format else R.string.expense_currency_format,
                    amount
                )

                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (group.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
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

        if (expanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                group.transactions.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupHeader(
    period: String,
    transactions: List<Transaction>
) {
    var isExpanded by remember { mutableStateOf(true) }
    val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val balance = income - expense

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

@Composable
fun TransactionHistoryItem(
    transaction: Transaction
) {
    TransactionItem(
        transaction = transaction,
        onClick = {}
    )
}

@Composable
fun TransactionGroup(
    groupTitle: String,
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        GroupHeader(
            period = groupTitle,
            transactions = transactions
        )

        transactions.forEach { transaction ->
            TransactionHistoryItem(transaction = transaction)
        }
    }
}

/**
 * Компонент для отображения сводки по группе транзакций
 */
@Composable
fun GroupSummary(
    income: Double,
    expense: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
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
                    text = stringResource(R.string.income_currency_format, formatTransactionAmount(income)),
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
                    text = stringResource(R.string.expense_currency_format, formatTransactionAmount(expense)),
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
                    text = stringResource(
                        if (balance >= 0) R.string.income_currency_format else R.string.expense_currency_format,
                        formatTransactionAmount(balance)
                    ),
                    fontSize = 14.sp,
                    color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 