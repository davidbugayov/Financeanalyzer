package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    
    // Состояние для отслеживания - показывать ли все группы
    var showAllGroups by rememberSaveable { mutableStateOf(false) }
    
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

            // Определяем, сколько групп показывать
            val visibleGroupsCount = if (showAllGroups) groups.size else 5
            val visibleGroups = remember(groups, showAllGroups) {
                // Сначала фильтруем группы с нулевым доходом, затем берем нужное количество
                groups.filter { it.total.amount > BigDecimal.ZERO }.take(visibleGroupsCount)
            }

            // Предварительно обрабатываем данные для каждой группы
            val processedGroups = remember(visibleGroups) {
                visibleGroups.map { group ->
                    val isExpense = group.transactions.isNotEmpty() && group.transactions.first().isExpense
                    val formattedAmount = if (isExpense) {
                        "-" + group.total.abs().formatted(false)
                    } else {
                        "+" + group.total.abs().formatted(false)
                    }
                    
                    Triple(group.name, isExpense, formattedAmount)
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                processedGroups.forEach { (name, isExpense, formattedAmount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            fontSize = 13.sp,
                            color = if (isExpense) expenseColor else incomeColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = formattedAmount,
                            fontSize = 13.sp,
                            color = if (isExpense) expenseColor else incomeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Если есть еще группы и не показываем все, отображаем текст "И ещё X элементов"
            val filteredGroupsSize = remember(groups) { groups.filter { it.total.amount > BigDecimal.ZERO }.size }
            
            if (filteredGroupsSize > 5 && !showAllGroups) {
                Text(
                    text = "И ещё ${filteredGroupsSize - 5} элементов",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clickable { showAllGroups = true },
                    fontWeight = FontWeight.Medium
                )
            } 
            // Если показываем все группы, добавляем кнопку "Скрыть"
            else if (showAllGroups && filteredGroupsSize > 5) {
                Text(
                    text = "Скрыть",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clickable { showAllGroups = false },
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 