package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

@Composable
fun PieChartSection(
    showExpenses: Boolean,
    onShowExpensesChange: (Boolean) -> Unit,
    filteredTransactions: List<Transaction>,
    viewModel: ChartViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_large)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = if (showExpenses)
                    stringResource(R.string.chart_expenses)
                else
                    stringResource(R.string.chart_income),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (showExpenses) LocalExpenseColor.current else LocalIncomeColor.current
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = if (showExpenses)
                    stringResource(R.string.expense_type)
                else
                    stringResource(R.string.income_type),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (showExpenses) LocalExpenseColor.current else LocalIncomeColor.current
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Переключатель доходы/расходы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.expense),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.show_expenses),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (showExpenses) LocalExpenseColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onShowExpensesChange(true) }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.income),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.show_income),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (!showExpenses) LocalIncomeColor.current else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onShowExpensesChange(false) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Визуальное представление выбора
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.height_small))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
            ) {
                Box(
                    modifier = Modifier
                        .weight(if (showExpenses) 0.7f else 0.3f)
                        .fillMaxHeight()
                        .background(LocalExpenseColor.current)
                )
                Box(
                    modifier = Modifier
                        .weight(if (!showExpenses) 0.7f else 0.3f)
                        .fillMaxHeight()
                        .background(LocalIncomeColor.current)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Отображение соответствующей диаграммы
            if (showExpenses) {
                val expensesByCategory = viewModel.getExpensesByCategory(filteredTransactions)
                if (expensesByCategory.isNotEmpty()) {
                    CategoryPieChart(
                        data = expensesByCategory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.chart_height_large))
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    CategoryList(
                        data = expensesByCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    EmptyDataMessage(stringResource(R.string.no_expense_data))
                }
            } else {
                val incomeByCategory = viewModel.getIncomeByCategory(filteredTransactions)
                if (incomeByCategory.isNotEmpty()) {
                    CategoryPieChart(
                        data = incomeByCategory,
                        isIncome = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.chart_height_large))
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    CategoryList(
                        data = incomeByCategory,
                        modifier = Modifier.fillMaxWidth(),
                        isIncome = true
                    )
                } else {
                    EmptyDataMessage(stringResource(R.string.no_income_data))
                }
            }
        }
    }
}

@Composable
private fun EmptyDataMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.chart_height_large)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
} 