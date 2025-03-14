package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

/**
 * Секция для отображения ежедневных расходов в виде графика.
 *
 * @param dailyExpenses Список ежедневных расходов
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun DailyExpensesSection(
    dailyExpenses: List<DailyExpense>,
    modifier: Modifier = Modifier
) {
    // Фильтруем дни без расходов
    val filteredExpenses = dailyExpenses.filter { it.amount.toDouble() > 0 }

    if (filteredExpenses.isEmpty()) return

    // Находим максимальное значение для масштабирования графика
    val maxAmount = filteredExpenses.maxOfOrNull { it.amount.toDouble() } ?: 0.0
    val expenseColor = LocalExpenseColor.current

    // Состояние для отслеживания, развернут ли список
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.spacing_large)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_large))
        ) {
            Text(
                text = stringResource(R.string.daily_expenses),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // График ежедневных расходов
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Вертикальная ось с суммами
                Column(
                    modifier = Modifier
                        .width(50.dp)
                        .height(150.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Отображаем метки для вертикальной оси (сверху вниз)
                    val gridLines = 4
                    for (i in gridLines downTo 0) {
                        val value = maxAmount * i / gridLines
                        // Используем класс Money для форматирования
                        val money = Money(value)
                        Text(
                            text = money.format(showCurrency = false),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Рисуем линии графика
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val barWidth = width / (filteredExpenses.size + 1)

                        // Рисуем горизонтальные линии сетки
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = height - (height * i / gridLines)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Рисуем линию графика
                        for (i in 0 until filteredExpenses.size - 1) {
                            val startX = barWidth * (i + 1)
                            val startY = height - (height * (filteredExpenses[i].amount.toDouble() / max(maxAmount, 1.0))).toFloat()
                            val endX = barWidth * (i + 2)
                            val endY = height - (height * (filteredExpenses[i + 1].amount.toDouble() / max(maxAmount, 1.0))).toFloat()

                            drawLine(
                                color = expenseColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Рисуем точки на графике
                        for (i in 0 until filteredExpenses.size) {
                            val x = barWidth * (i + 1)
                            val y = height - (height * (filteredExpenses[i].amount.toDouble() / max(maxAmount, 1.0))).toFloat()

                            drawCircle(
                                color = expenseColor,
                                radius = 6f,
                                center = Offset(x, y)
                            )

                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            // Легенда с датами
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Показываем только первую и последнюю дату для экономии места
                if (filteredExpenses.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("dd.MM", Locale("ru"))

                    Text(
                        text = dateFormat.format(filteredExpenses.first().date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (filteredExpenses.size > 1) {
                        Text(
                            text = dateFormat.format(filteredExpenses.last().date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            // Детализация по дням
            Column {
                // Определяем, сколько элементов показывать
                val itemsToShow = if (isExpanded) filteredExpenses.size else minOf(5, filteredExpenses.size)

                filteredExpenses.take(itemsToShow).forEach { expense ->
                    DailyExpenseItem(
                        expense = expense,
                        maxAmount = maxAmount,
                        expenseColor = expenseColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Если есть больше 5 дней и список не развернут, показываем "Еще..."
                if (filteredExpenses.size > 5 && !isExpanded) {
                    Text(
                        text = stringResource(R.string.more_items_format, filteredExpenses.size - 5),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = true }
                            .padding(vertical = 8.dp)
                    )
                } else if (isExpanded && filteredExpenses.size > 5) {
                    // Если список развернут, показываем кнопку "Свернуть"
                    Text(
                        text = stringResource(R.string.collapse),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = false }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Элемент списка ежедневных расходов с визуальным индикатором.
 *
 * @param expense Информация о ежедневных расходах
 * @param maxAmount Максимальная сумма расходов для масштабирования
 * @param expenseColor Цвет для отображения расходов
 */
@Composable
private fun DailyExpenseItem(
    expense: DailyExpense,
    maxAmount: Double,
    expenseColor: Color
) {
    val dateFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
    val percentage = (expense.amount.toDouble() / max(maxAmount, 1.0) * 100).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Дата
        Text(
            text = dateFormat.format(expense.date),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )

        // Визуальный индикатор
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(8.dp)
                    .background(
                        color = expenseColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Сумма - используем класс Money для форматирования
        val money = Money(expense.amount)
        Text(
            text = money.format(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = expenseColor
        )
    }
} 