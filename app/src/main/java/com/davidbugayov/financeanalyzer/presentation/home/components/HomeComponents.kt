package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Карточка с балансом и общей статистикой
 */
@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.current_balance),
                fontSize = 16.sp,
                color = Color.Gray
            )

            Text(
                text = stringResource(R.string.currency_format, String.format("%.2f", balance)),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", income)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", expense)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

/**
 * Карточка с ежедневной статистикой
 */
@Composable
fun DailyStatsCard(
    dailyIncome: Double,
    dailyExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.today),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", dailyIncome)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.currency_format, String.format("%.2f", dailyExpense)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

/**
 * Фильтры для транзакций
 */
@Composable
fun FilterChips(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == TransactionFilter.TODAY,
            onClick = { onFilterSelected(TransactionFilter.TODAY) },
            label = { Text(stringResource(R.string.today)) }
        )

        FilterChip(
            selected = currentFilter == TransactionFilter.WEEK,
            onClick = { onFilterSelected(TransactionFilter.WEEK) },
            label = { Text(stringResource(R.string.week)) }
        )

        FilterChip(
            selected = currentFilter == TransactionFilter.MONTH,
            onClick = { onFilterSelected(TransactionFilter.MONTH) },
            label = { Text(stringResource(R.string.month)) }
        )
    }
}

/**
 * Элемент списка транзакций
 */
@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

/**
 * Индикатор загрузки
 */
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Отображение ошибки с возможностью повторить
 */
@Composable
fun ErrorContent(
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error ?: stringResource(R.string.unknown_error),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
} 