package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.LocalSummaryCardBackground
import com.davidbugayov.financeanalyzer.ui.theme.LocalSummaryDivider
import com.davidbugayov.financeanalyzer.ui.theme.LocalSummaryExpense
import com.davidbugayov.financeanalyzer.ui.theme.LocalSummaryIncome
import com.davidbugayov.financeanalyzer.ui.theme.LocalSummaryTextPrimary
import com.davidbugayov.financeanalyzer.ui.theme.LocalSummaryTextSecondary
import java.math.BigDecimal

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
    balance: Money? = null,
) {
    val cardBg = LocalSummaryCardBackground.current
    val incomeColor = LocalSummaryIncome.current
    val expenseColor = LocalSummaryExpense.current
    val textPrimary = LocalSummaryTextPrimary.current
    val textSecondary = LocalSummaryTextSecondary.current
    val dividerColor = LocalSummaryDivider.current

    val calculatedBalance = balance ?: totalIncome.minus(totalExpense)
    val balanceColor = if (calculatedBalance.amount >= BigDecimal.ZERO) incomeColor else expenseColor
    var showAllGroups by rememberSaveable { mutableStateOf(false) }
    var showExpenses by rememberSaveable { mutableStateOf(true) }

    val periodTitle = periodTitleForFilter(currentFilter)

    val categoryGroups =
        remember(filteredTransactions, showExpenses) {
            val filteredByType = filteredTransactions.filter { it.isExpense == showExpenses }
            val groupedByCategory = filteredByType.groupBy { it.category }
            groupedByCategory
                .map { (category, transactions) ->
                    CategorySummary(
                        category = category,
                        amount = transactions.fold(Money.zero()) { acc, transaction -> acc + transaction.amount },
                        isExpense = showExpenses,
                    )
                }.sortedByDescending { it.amount.amount }
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            SummaryHeader(periodTitle, textPrimary)
            SummaryTotals(
                totalIncome,
                totalExpense,
                calculatedBalance,
                incomeColor,
                expenseColor,
                balanceColor,
                textSecondary,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = dividerColor)
            SummaryCategorySwitcher(
                showExpenses,
                onSwitch = { showExpenses = !showExpenses },
                textSecondary = textSecondary,
            )
            val visibleCount =
                if (showAllGroups) {
                    categoryGroups.size
                } else {
                    minOf(
                        5,
                        categoryGroups.size,
                    )
                }
            val visibleCategories = categoryGroups.take(visibleCount)
            SummaryCategoryList(
                visibleCategories = visibleCategories,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                textSecondary = textSecondary,
            )
            if (visibleCategories.isEmpty()) {
                SummaryEmptyState(showExpenses, textSecondary)
            }
            if (categoryGroups.size > 5 && !showAllGroups) {
                SummaryShowMoreButton(categoryGroups.size - 5, textSecondary) { showAllGroups = true }
            } else if (showAllGroups && categoryGroups.size > 5) {
                SummaryHideButton(textSecondary) { showAllGroups = false }
            }
        }
    }
}

@Composable
private fun SummaryHeader(
    periodTitle: String,
    textPrimary: Color,
) {
    Text(
        text = periodTitle,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp),
        color = textPrimary,
    )
}

@Composable
private fun SummaryTotals(
    totalIncome: Money,
    totalExpense: Money,
    balance: Money,
    incomeColor: Color,
    expenseColor: Color,
    balanceColor: Color,
    textSecondary: Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Доходы
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = incomeColor.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = incomeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(UiR.string.total_income),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                    )
                }
                Text(
                    text = "+${totalIncome.abs().formatForDisplay(showCurrency = false)}",
                    fontSize = 16.sp,
                    color = incomeColor,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        // Расходы
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = expenseColor.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = expenseColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(UiR.string.total_expense),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                    )
                }
                Text(
                    text = "-${totalExpense.abs().formatForDisplay(showCurrency = false)}",
                    fontSize = 16.sp,
                    color = expenseColor,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        // Баланс
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = balanceColor.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(UiR.string.balance),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                )
                Text(
                    text = if (balance.amount >= java.math.BigDecimal.ZERO) {
                        "+${balance.formatForDisplay(showCurrency = false)}"
                    } else {
                        balance.formatForDisplay(showCurrency = false)
                    },
                    fontSize = 18.sp,
                    color = balanceColor,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun SummaryCategorySwitcher(
    showExpenses: Boolean,
    onSwitch: () -> Unit,
    textSecondary: Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text =
                if (showExpenses) {
                    stringResource(
                        UiR.string.expense_categories,
                    )
                } else {
                    stringResource(UiR.string.income_categories)
                },
            style = MaterialTheme.typography.titleSmall,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondary,
        )
        Text(
            text =
                if (showExpenses) stringResource(UiR.string.show_incomes) else stringResource(UiR.string.show_expenses),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onSwitch() },
        )
    }
}

@Composable
private fun SummaryCategoryList(
    visibleCategories: List<CategorySummary>,
    incomeColor: Color,
    expenseColor: Color,
    textSecondary: Color,
) {
    val totalAmount = visibleCategories.sumOf { it.amount.abs().amount }.toFloat()

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleCategories.forEach { categorySummary ->
            val progress = if (totalAmount > 0f) {
                (categorySummary.amount.abs().amount.toFloat() / totalAmount).coerceIn(0f, 1f)
            } else {
                0f
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryLocalization.displayName(
                                androidx.compose.ui.platform.LocalContext.current,
                                categorySummary.category,
                            ),
                        fontSize = 13.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text =
                            if (categorySummary.isExpense) {
                                "-" + categorySummary.amount.abs().formatForDisplay(showCurrency = false)
                            } else {
                                "+" + categorySummary.amount.abs().formatForDisplay(showCurrency = false)
                            },
                        fontSize = 13.sp,
                        color = if (categorySummary.isExpense) expenseColor else incomeColor,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Progress bar для визуализации доли категории
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            color = (if (categorySummary.isExpense) expenseColor else incomeColor).copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                        )
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(
                                color = if (categorySummary.isExpense) expenseColor else incomeColor,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryEmptyState(
    showExpenses: Boolean,
    textSecondary: Color,
) {
    Text(
        text =
            if (showExpenses) {
                stringResource(
                    UiR.string.no_expenses_period,
                )
            } else {
                stringResource(UiR.string.no_incomes_period)
            },
        fontSize = 13.sp,
        color = textSecondary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SummaryShowMoreButton(
    moreCount: Int,
    textSecondary: Color,
    onClick: () -> Unit,
) {
    Text(
        text = stringResource(UiR.string.and_more_categories, moreCount),
        fontSize = 12.sp,
        color = textSecondary,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { onClick() },
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun SummaryHideButton(
    textSecondary: Color,
    onClick: () -> Unit,
) {
    Text(
        text = stringResource(UiR.string.hide),
        fontSize = 12.sp,
        color = textSecondary,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { onClick() },
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun periodTitleForFilter(filter: TransactionFilter): String =
    when (filter) {
        TransactionFilter.TODAY -> stringResource(UiR.string.filter_today)
        TransactionFilter.WEEK -> stringResource(UiR.string.filter_week)
        TransactionFilter.MONTH -> stringResource(UiR.string.filter_month)
        TransactionFilter.ALL -> stringResource(UiR.string.all_time)
    }

/**
 * Класс для хранения сводной информации о категории
 */
data class CategorySummary(
    val category: String,
    val amount: Money,
    val isExpense: Boolean,
)
