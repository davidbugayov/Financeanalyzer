package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.DailyExpense
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

/**
 * График для отображения ежедневных расходов.
 *
 * @param dailyExpenses Список ежедневных расходов
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun DailyExpensesChart(
    dailyExpenses: List<DailyExpense>,
    modifier: Modifier = Modifier
) {
    val expenseColor = LocalExpenseColor.current

    // Состояние для отслеживания, нужно ли показывать все транзакции
    var showAllTransactions by remember { mutableStateOf(false) }

    // Сортируем транзакции по дате (сначала новые)
    val sortedExpenses = dailyExpenses.sortedByDescending { it.date }

    // Определяем, какие транзакции показывать
    val displayedExpenses = if (showAllTransactions || sortedExpenses.size <= 5) {
        sortedExpenses
    } else {
        sortedExpenses.take(5)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.spacing_medium), vertical = dimensionResource(R.dimen.spacing_small)),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = stringResource(R.string.daily_expenses),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleMedium.fontSize.times(0.95f)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            if (dailyExpenses.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_expense_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.spacing_large))
                )
            } else {
                // График расходов
                ExpensesLineChart(
                    dailyExpenses = dailyExpenses,
                    color = expenseColor,
                    axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.chart_height))
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                // Легенда графика - показываем только выбранные транзакции
                displayedExpenses.forEach { expense ->
                    DailyExpenseItem(expense = expense, color = expenseColor)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                }

                // Кнопка "Показать все", если есть скрытые транзакции
                if (sortedExpenses.size > 5 && !showAllTransactions) {
                    TextButton(
                        onClick = { showAllTransactions = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.show_all_transactions, sortedExpenses.size - 5),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (showAllTransactions && sortedExpenses.size > 5) {
                    TextButton(
                        onClick = { showAllTransactions = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.show_less),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Линейный график расходов.
 */
@Composable
private fun ExpensesLineChart(
    dailyExpenses: List<DailyExpense>,
    color: Color,
    axisColor: Color,
    modifier: Modifier = Modifier
) {
    val sortedExpenses = dailyExpenses.sortedBy { it.date }
    val maxExpense = sortedExpenses.maxOfOrNull { it.amount.amount.toDouble() } ?: 0.0

    // Форматтер для дат
    val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    // Создаем скроллируемый контейнер
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column {
            // Вертикальные подписи сумм
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.chart_height))
            ) {
                // Вертикальная ось с подписями сумм
                Column(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.chart_axis_width))
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Верхняя сумма (максимальная)
                    Text(
                        text = Money(maxExpense).format(false),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Средняя сумма
                    Text(
                        text = Money(maxExpense / 2).format(false),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Нижняя сумма (0)
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Скроллируемый график
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Определяем минимальную ширину для графика в зависимости от количества точек
                    val minWidth = max(sortedExpenses.size * 25, 250).dp

                    Column {
                        // Сам график
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .width(minWidth)
                        ) {
                            val width = size.width
                            val height = size.height
                            val padding = 20f

                            // Рисуем оси
                            drawLine(
                                color = axisColor,
                                start = Offset(padding, padding),
                                end = Offset(padding, height - padding),
                                strokeWidth = 1f
                            )

                            drawLine(
                                color = axisColor,
                                start = Offset(padding, height - padding),
                                end = Offset(width - padding, height - padding),
                                strokeWidth = 1f
                            )

                            if (sortedExpenses.size > 1 && maxExpense > 0) {
                                val xStep = (width - 2 * padding) / (sortedExpenses.size - 1)
                                val yScale = (height - 2 * padding) / maxExpense

                                // Создаем путь для линии графика
                                val path = Path()
                                var firstPoint = true

                                sortedExpenses.forEachIndexed { index, expense ->
                                    val x = padding + index * xStep
                                    val y = height - padding - (expense.amount.amount.toDouble() * yScale).toFloat()

                                    if (firstPoint) {
                                        path.moveTo(x, y)
                                        firstPoint = false
                                    } else {
                                        path.lineTo(x, y)
                                    }

                                    // Рисуем точку
                                    drawCircle(
                                        color = color,
                                        radius = 4f,
                                        center = Offset(x, y)
                                    )
                                }

                                // Рисуем линию
                                drawPath(
                                    path = path,
                                    color = color,
                                    style = Stroke(width = 2f)
                                )
                            }
                        }

                        // Подписи дат
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sortedExpenses.forEach { expense ->
                                Text(
                                    text = dateFormat.format(expense.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Элемент списка ежедневных расходов.
 *
 * @param expense Информация о ежедневных расходах
 * @param color Цвет для отображения суммы
 */
@Composable
private fun DailyExpenseItem(
    expense: DailyExpense,
    color: Color
) {
    val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault()).format(expense.date)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_small)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Дата
        Text(
            text = dateFormat,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Сумма
        Text(
            text = expense.amount.format(),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
} 