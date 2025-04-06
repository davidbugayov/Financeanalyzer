package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    // Цвета из темы
    val positiveColor = LocalIncomeColor.current
    val negativeColor = LocalExpenseColor.current
    
    // Предопределенные цвета для улучшения производительности
    val positiveBackgroundColor = positiveColor.copy(alpha = 0.1f) 
    val positiveTextColor = positiveColor
    val negativeBackgroundColor = negativeColor.copy(alpha = 0.1f)
    val negativeTextColor = negativeColor
    
    // Оптимизация вычисления финансовых сумм с использованием sequence
    val financialSummary = remember(transactions) {
        val income = transactions
            .asSequence()
            .filter { !it.isExpense }
            .map { Money(it.amount) }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val expense = transactions
            .asSequence()
            .filter { it.isExpense }
            .map { Money(it.amount) }
            .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

        val balance = income.minus(expense)
        Triple(income, expense, balance)
    }

    val (income, expense, balance) = financialSummary
    
    // Определяем цвета заранее, не вычисляя их каждый раз
    val isPositive = balance >= Money.zero()
    val backgroundColor = if (isPositive) positiveBackgroundColor else negativeBackgroundColor
    val textColor = if (isPositive) positiveTextColor else negativeTextColor
    
    // Оптимизируем обработчик нажатия
    val clickHandler = remember(onExpandToggle, isExpanded) {
        { onExpandToggle(!isExpanded) }
    }

    // Упрощенная структура компонента для улучшения производительности
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = clickHandler),
        color = backgroundColor,
        shadowElevation = 1.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Доходы
                FinancialInfoColumn(
                    label = stringResource(R.string.income),
                    value = income.toString(),
                    color = positiveTextColor
                )
                
                // Расходы
                FinancialInfoColumn(
                    label = stringResource(R.string.expense),
                    value = expense.toString(),
                    color = negativeTextColor
                )
                
                // Баланс
                FinancialInfoColumn(
                    label = stringResource(R.string.balance),
                    value = balance.toString(),
                    color = textColor
                )
            }
        }
    }
}

/**
 * Вспомогательный компонент для отображения финансовой информации
 */
@Composable
private fun FinancialInfoColumn(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
} 