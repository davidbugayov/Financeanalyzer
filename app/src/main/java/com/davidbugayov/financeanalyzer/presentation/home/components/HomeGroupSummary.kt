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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.TransactionGroup
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
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
    // Определяем цвета для доходов и расходов из темы
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    
    // Вычисляем баланс
    val balance = totalIncome - totalExpense
    val balanceColor = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Отображение заголовка "Сводка"
            Text(
                text = stringResource(R.string.summary),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
                color = balanceColor // Цвет заголовка соответствует балансу
            )

            // Отображение общего дохода и расхода в более компактном виде
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total_income),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "+" + totalIncome.abs().formatted(false),
                    fontSize = 14.sp,
                    color = incomeColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total_expense),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "-" + totalExpense.abs().formatted(false),
                    fontSize = 14.sp,
                    color = expenseColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Отображение баланса
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.balance),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                val balanceText = if (balance.amount >= BigDecimal.ZERO) {
                    "+" + balance.abs().formatted(false)
                } else {
                    "-" + balance.abs().formatted(false)
                }
                Text(
                    text = balanceText,
                    fontSize = 14.sp,
                    color = balanceColor,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Отображаем группы, если они есть (макс. 5 групп)
            if (groups.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    groups.take(5).forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Определяем, является ли транзакция расходом
                            val isExpense =
                                group.transactions.isNotEmpty() && group.transactions.first().isExpense
                            
                            Text(
                                text = group.name,
                                fontSize = 13.sp,
                                color = if (isExpense) expenseColor else incomeColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Добавляем знак + или - в зависимости от типа транзакции
                            val formattedAmount = if (isExpense) {
                                "-" + group.total.abs().formatted(false)
                            } else {
                                "+" + group.total.abs().formatted(false)
                            }
                            
                            Text(
                                text = formattedAmount,
                                fontSize = 13.sp,
                                color = if (isExpense) expenseColor else incomeColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Если групп больше 5, показываем сообщение о том, что есть еще группы
                    if (groups.size > 5) {
                        Text(
                            text = "И ещё ${groups.size - 5} элементов",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
} 