package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import java.math.BigDecimal

/**
 * Компонент для отображения сводки по группам транзакций.
 *
 * @param groups Список групп транзакций
 * @param totalIncome Общий доход
 * @param totalExpense Общий расход
 */
@Composable
fun HomeGroupSummary(
    groups: List<TransactionGroup>,
    totalIncome: Money,
    totalExpense: Money
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Отображение общего дохода и расхода
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total_income),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = totalIncome.formatted(false),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total_expense),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = totalExpense.formatted(false),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Проверяем, есть ли транзакции или доходы/расходы
            val hasTransactions = groups.isNotEmpty() ||
                    totalIncome.amount > BigDecimal.ZERO ||
                    totalExpense.amount > BigDecimal.ZERO

            if (hasTransactions) {
                // Отображаем группы, если они есть
                if (groups.isNotEmpty()) {
                    // Заменяем LazyColumn на обычный Column с ограниченным количеством элементов
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Отображаем максимум 5 групп, чтобы не перегружать интерфейс
                        groups.take(5).forEach { group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.name,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = group.total.formatted(false),
                                    fontSize = 14.sp,
                                    color = if (group.total.amount >= BigDecimal.ZERO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    // Если есть доходы или расходы, но нет групп, показываем сообщение о том, что данные обобщены
                    Text(
                        text = stringResource(R.string.summary),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Если нет ни транзакций, ни доходов/расходов, показываем сообщение об отсутствии транзакций
                Text(
                    text = stringResource(R.string.no_transactions_in_period),
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 