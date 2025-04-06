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
import java.math.BigDecimal

/**
 * Компонент для отображения сводки по группам транзакций и категориям.
 *
 * @param filteredTransactions Отфильтрованные транзакции для группировки по категориям
 * @param totalIncome Общий доход
 * @param totalExpense Общий расход
 * @param currentFilter Текущий фильтр периода (день/неделя/месяц)
 */
@Composable
fun HomeGroupSummary(
    filteredTransactions: List<Transaction> = emptyList(),
    totalIncome: Money,
    totalExpense: Money,
    currentFilter: TransactionFilter = TransactionFilter.MONTH
) {
    // Определяем цвета для доходов и расходов из темы
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current

    // Вычисляем баланс
    val balance = totalIncome - totalExpense
    val balanceColor = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor

    // Состояние для отслеживания - показывать ли все группы
    var showAllGroups by rememberSaveable { mutableStateOf(false) }

    // Состояние для выбора типа - расходы или доходы
    var showExpenses by rememberSaveable { mutableStateOf(true) }

    // Получаем строки для заголовков за пределами remember
    val titleToday = "Сводка за сегодня"
    val titleWeek = "Сводка за неделю"
    val titleMonth = "Сводка за месяц"
    val titleAll = stringResource(R.string.summary)

    // Определяем заголовок периода в зависимости от фильтра
    val periodTitle = remember(currentFilter) {
        when (currentFilter) {
            TransactionFilter.TODAY -> titleToday
            TransactionFilter.WEEK -> titleWeek
            TransactionFilter.MONTH -> titleMonth
            TransactionFilter.ALL -> titleAll
        }
    }

    // Группируем транзакции по категориям
    val categoryGroups = remember(filteredTransactions, showExpenses) {
        // Фильтруем по типу (расходы/доходы)
        val filteredByType = filteredTransactions.filter {
            it.isExpense == showExpenses
        }

        // Группируем по категориям и суммируем
        val groupedByCategory = filteredByType.groupBy { it.category }

        groupedByCategory.map { (category, transactions) ->
            // Суммируем все транзакции в этой категории
            val total = transactions.sumOf { it.amount }
            val money = Money(total)

            // Создаем объект с информацией о категории
            CategorySummary(
                category = category,
                amount = money,
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
            // Отображение заголовка в зависимости от периода
            Text(
                text = periodTitle,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
                color = MaterialTheme.colorScheme.primary
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

            // Переключатель между расходами и доходами
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showExpenses) "Категории расходов" else "Категории доходов",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Показать ${if (showExpenses) "доходы" else "расходы"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showExpenses = !showExpenses }
                )
            }

            // Определяем, сколько категорий показывать
            val visibleCount =
                if (showAllGroups) categoryGroups.size else minOf(5, categoryGroups.size)
            val visibleCategories = categoryGroups.take(visibleCount)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (visibleCategories.isEmpty()) {
                    Text(
                        text = if (showExpenses)
                            "Нет расходов за выбранный период"
                        else
                            "Нет доходов за выбранный период",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
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

            // Если есть еще категории и не показываем все, отображаем текст "И ещё X элементов"
            if (categoryGroups.size > 5 && !showAllGroups) {
                Text(
                    text = "И ещё ${categoryGroups.size - 5} категорий",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clickable { showAllGroups = true },
                    fontWeight = FontWeight.Medium
                )
            }
            // Если показываем все категории, добавляем кнопку "Скрыть"
            else if (showAllGroups && categoryGroups.size > 5) {
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

/**
 * Класс для хранения сводной информации о категории
 */
data class CategorySummary(
    val category: String,
    val amount: Money,
    val isExpense: Boolean
)