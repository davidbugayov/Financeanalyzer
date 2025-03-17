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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction

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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small))
            .clip(MaterialTheme.shapes.medium)
            .shadow(elevation = dimensionResource(R.dimen.card_elevation))
            .clickable { onExpandToggle(!isExpanded) },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period,
                    fontSize = dimensionResource(R.dimen.text_size_large).value.sp,
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
                        .padding(top = dimensionResource(R.dimen.spacing_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.currency_format, income.format(false)),
                        fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                        color = colorResource(R.color.income),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = stringResource(R.string.expense_currency_format, expense.abs().format(false)),
                        fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                        color = colorResource(R.color.expense),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = stringResource(R.string.currency_format, balance.format(false)),
                        fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                        color = if (balance >= Money.zero()) colorResource(R.color.income) else colorResource(R.color.expense),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 