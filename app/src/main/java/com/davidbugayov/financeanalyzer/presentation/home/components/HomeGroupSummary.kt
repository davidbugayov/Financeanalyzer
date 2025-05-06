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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

/**
 * Компонент для отображения сводки по группам транзакций и категориям.
 *
 * @param filteredTransactions Отфильтрованные транзакции для группировки по категориям
 * @param totalIncome Общий доход
 * @param totalExpense Общий расход
 * @param currentFilter Текущий фильтр периода (день/неделя/месяц)
 * @param balance Новый опциональный параметр для прямой передачи баланса
 */
@Composable
fun HomeGroupSummary(
    filteredTransactions: List<Transaction> = emptyList(),
    totalIncome: Money,
    totalExpense: Money,
    currentFilter: TransactionFilter = TransactionFilter.MONTH,
    balance: Money? = null
) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val calculatedBalance = balance ?: totalIncome.minus(totalExpense)
    val balanceColor = if (calculatedBalance.amount.signum() >= 0) incomeColor else expenseColor
    var showAllGroups by rememberSaveable { mutableStateOf(false) }
    var showExpenses by rememberSaveable { mutableStateOf(true) }

    val periodTitle = periodTitleForFilter(currentFilter)

    val categoryGroups = remember(filteredTransactions, showExpenses) {
        val filteredByType = filteredTransactions.filter { it.isExpense == showExpenses }
        val groupedByCategory = filteredByType.groupBy { it.category }
        groupedByCategory.map { (category, transactions) ->
            CategorySummary(
                category = category,
                amount = transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount },
                isExpense = showExpenses
            )
        }.sortedByDescending { it.amount.amount }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            SummaryHeader(periodTitle)
            SummaryTotals(totalIncome, totalExpense, calculatedBalance, incomeColor, expenseColor, balanceColor)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SummaryCategorySwitcher(showExpenses, onSwitch = { showExpenses = !showExpenses })
            val visibleCount = if (showAllGroups) categoryGroups.size else minOf(5, categoryGroups.size)
            val visibleCategories = categoryGroups.take(visibleCount)
            SummaryCategoryList(
                visibleCategories = visibleCategories,
                incomeColor = incomeColor,
                expenseColor = expenseColor
            )
            if (visibleCategories.isEmpty()) {
                SummaryEmptyState(showExpenses)
            }
            if (categoryGroups.size > 5 && !showAllGroups) {
                SummaryShowMoreButton(categoryGroups.size - 5) { showAllGroups = true }
            } else if (showAllGroups && categoryGroups.size > 5) {
                SummaryHideButton { showAllGroups = false }
            }
        }
    }
}

@Composable
private fun SummaryHeader(periodTitle: String) {
    Text(
        text = periodTitle,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SummaryTotals(
    totalIncome: Money,
    totalExpense: Money,
    balance: Money,
    incomeColor: androidx.compose.ui.graphics.Color,
    expenseColor: androidx.compose.ui.graphics.Color,
    balanceColor: androidx.compose.ui.graphics.Color
) {
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
        Text(
            text = balance.format(false),
            fontSize = 14.sp,
            color = balanceColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryCategorySwitcher(
    showExpenses: Boolean,
    onSwitch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(if (showExpenses) R.string.expense_categories else R.string.income_categories),
            style = MaterialTheme.typography.titleSmall,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(if (showExpenses) R.string.show_income else R.string.show_expenses),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onSwitch() }
        )
    }
}

@Composable
private fun SummaryCategoryList(
    visibleCategories: List<CategorySummary>,
    incomeColor: androidx.compose.ui.graphics.Color,
    expenseColor: androidx.compose.ui.graphics.Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        visibleCategories.forEach { categorySummary ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categorySummary.category,
                    fontSize = 13.sp,
                    color = if (categorySummary.isExpense) expenseColor else incomeColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (categorySummary.isExpense)
                        "-" + categorySummary.amount.abs().formatted(false)
                    else
                        "+" + categorySummary.amount.abs().formatted(false),
                    fontSize = 13.sp,
                    color = if (categorySummary.isExpense) expenseColor else incomeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SummaryEmptyState(showExpenses: Boolean) {
    Text(
        text = stringResource(if (showExpenses) R.string.no_expenses_period else R.string.no_income_period),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SummaryShowMoreButton(moreCount: Int, onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.and_more_categories, moreCount),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable { onClick() },
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SummaryHideButton(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.hide),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clickable { onClick() },
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun periodTitleForFilter(filter: TransactionFilter): String {
    return when (filter) {
        TransactionFilter.TODAY -> stringResource(R.string.summary_today)
        TransactionFilter.WEEK -> stringResource(R.string.summary_week)
        TransactionFilter.MONTH -> stringResource(R.string.summary_month)
        TransactionFilter.ALL -> stringResource(R.string.summary_all_time)
    }
}

/**
 * Класс для хранения сводной информации о категории
 */
data class CategorySummary(
    val category: String,
    val amount: Money,
    val isExpense: Boolean
)