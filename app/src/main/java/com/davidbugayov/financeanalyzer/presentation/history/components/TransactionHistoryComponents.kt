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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import com.davidbugayov.financeanalyzer.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun GroupingChips(
    currentGrouping: GroupingType,
    onGroupingSelected: (GroupingType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentGrouping == GroupingType.DAY,
            onClick = { onGroupingSelected(GroupingType.DAY) },
            label = { Text(stringResource(R.string.group_by_day)) }
        )

        FilterChip(
            selected = currentGrouping == GroupingType.WEEK,
            onClick = { onGroupingSelected(GroupingType.WEEK) },
            label = { Text(stringResource(R.string.group_by_week)) }
        )

        FilterChip(
            selected = currentGrouping == GroupingType.MONTH,
            onClick = { onGroupingSelected(GroupingType.MONTH) },
            label = { Text(stringResource(R.string.group_by_month)) }
        )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.income_label,
                            stringResource(R.string.currency_format, formatNumber(income, useDecimals = true))
                        ),
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )

                    Text(
                        text = stringResource(
                            R.string.expense_label,
                            stringResource(R.string.currency_format, formatNumber(expense, useDecimals = true))
                        ),
                        fontSize = 12.sp,
                        color = Color(0xFFF44336)
                    )

                    Text(
                        text = stringResource(
                            R.string.balance_label,
                            stringResource(R.string.currency_format, formatNumber(balance, useDecimals = true))
                        ),
                        fontSize = 12.sp,
                        color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryItem(
    transaction: Transaction
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        R.string.category_date_format,
                        transaction.category,
                        dateFormat.format(transaction.date)
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                transaction.note?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = if (transaction.isExpense)
                    stringResource(R.string.expense_currency_format, String.format("%.2f", transaction.amount))
                else
                    stringResource(R.string.income_currency_format, String.format("%.2f", transaction.amount)),
                color = if (transaction.isExpense) Color(0xFFF44336) else Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
} 