package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Секция для отображения ежедневных расходов.
 *
 * @param dailyExpenses Список ежедневных расходов
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun DailyExpensesSection(
    dailyExpenses: List<DailyExpense>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.daily_expenses),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                dailyExpenses.forEach { expense ->
                    DailyExpenseItem(expense = expense)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Элемент списка ежедневных расходов.
 *
 * @param expense Информация о ежедневных расходах
 */
@Composable
private fun DailyExpenseItem(expense: DailyExpense) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = dateFormat.format(expense.date),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.amount_format, expense.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
} 