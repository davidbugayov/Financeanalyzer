package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

/**
 * Заголовок группы транзакций с возможностью сворачивания/разворачивания.
 * Отображает период и суммарную информацию о транзакциях в группе.
 *
 * @param period Название периода (например, "Январь 2024")
 * @param transactions Список транзакций в группе
 * @param isExpanded Флаг, указывающий, развернута ли группа
 * @param onExpandToggle Callback, вызываемый при изменении состояния развернутости
 */
@Composable
fun GroupHeader(
    period: String,
    transactions: List<Transaction>,
    isExpanded: Boolean = true,
    onExpandToggle: (Boolean) -> Unit = {}
) {
    // Вычисляем суммы только при изменении списка транзакций
    val financialSummary = remember(transactions) {
        val income = transactions.filter { !it.isExpense }
            .map { Money(it.amount) }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val expense = transactions.filter { it.isExpense }
            .map { Money(it.amount) }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val balance = income.minus(expense)

        Triple(income, expense, balance)
    }

    val (income, expense, balance) = financialSummary
    
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    
    // Определяем цвет фона в зависимости от баланса
    val backgroundColor = if (balance >= Money.zero()) {
        Color(0xFFE0F7E0) // Светло-зеленый для положительного баланса
    } else {
        Color(0xFFFFE0E0) // Светло-красный для отрицательного баланса
    }
    
    // Определяем цвет текста заголовка в зависимости от баланса
    val headerTextColor = if (balance >= Money.zero()) {
        Color(0xFF2E7D32) // Темно-зеленый для положительного баланса
    } else {
        Color(0xFFB71C1C) // Темно-красный для отрицательного баланса
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp) // Уменьшаем вертикальные отступы
            .clip(MaterialTheme.shapes.medium)
            .clickable { onExpandToggle(!isExpanded) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Уменьшаем высоту тени
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp) // Уменьшаем внутренние отступы
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period,
                    fontSize = 15.sp, // Уменьшаем размер шрифта
                    fontWeight = FontWeight.Bold,
                    color = headerTextColor
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    tint = headerTextColor,
                    modifier = Modifier.size(20.dp) // Уменьшаем размер иконки
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp) // Уменьшаем верхний отступ
                ) {
                    // Строка с метками
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Доход",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "Расход",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "Баланс",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp)) // Маленький отступ между метками и значениями
                    
                    // Строка со значениями
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.currency_format, income.format(false)),
                            fontSize = 14.sp, // Уменьшаем размер шрифта
                            color = incomeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = stringResource(R.string.expense_currency_format, expense.abs().format(false)),
                            fontSize = 14.sp, // Уменьшаем размер шрифта
                            color = expenseColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = stringResource(R.string.currency_format, balance.format(false)),
                            fontSize = 14.sp, // Уменьшаем размер шрифта
                            color = if (balance >= Money.zero()) incomeColor else expenseColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
} 