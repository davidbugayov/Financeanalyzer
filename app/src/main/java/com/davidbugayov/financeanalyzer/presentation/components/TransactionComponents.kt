package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.util.formatTransactionAmount
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Общий компонент для отображения транзакции в списке.
 * Отображает основную информацию о транзакции: название, категорию, дату, сумму.
 * При нажатии вызывает переданный callback.
 *
 * @param transaction Транзакция для отображения
 * @param onClick Callback, вызываемый при нажатии на элемент
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
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
                    text = transaction.category,
                    modifier = Modifier,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                Text(
                    text = dateFormat.format(transaction.date),
                    modifier = Modifier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val amount = formatTransactionAmount(transaction.amount)
            val formattedAmount = stringResource(
                if (transaction.isExpense) R.string.expense_currency_format else R.string.income_currency_format,
                amount
            )

            Text(
                text = formattedAmount,
                color = if (transaction.isExpense) Color(0xFFF44336) else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun TransactionItemContent(
    transaction: Transaction,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
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
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Text(
            text = if (transaction.isExpense)
                stringResource(R.string.expense_currency_format, formatTransactionAmount(transaction.amount))
            else
                stringResource(R.string.income_currency_format, formatTransactionAmount(transaction.amount)),
            color = if (transaction.isExpense) Color(0xFFF44336) else Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold
        )
    }
} 