package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Компонент для отображения отдельной транзакции в списке.
 *
 * @param transaction Транзакция для отображения
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая часть - категория и описание
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (transaction.title != null && transaction.title.isNotBlank()) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Правая часть - сумма
        Text(
            text = transaction.amount.formatted(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (transaction.amount.amount >= BigDecimal.ZERO) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    }
} 