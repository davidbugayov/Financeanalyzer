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
            .padding(horizontal = dimensionResource(R.dimen.spacing_large), vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        // Увеличиваем высоту для размещения подписей дат
                        .height(240.dp)
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                // Легенда графика - показываем только выбранные транзакции
                displayedExpenses.forEach { expense ->
                    DailyExpenseItem(expense = expense, color = expenseColor)
                    Spacer(modifier = Modifier.height(8.dp))
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
                    .height(200.dp)
            ) {
                // Вертикальная ось с подписями сумм
                Column(
                    modifier = Modifier
                        .width(50.dp)
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
                    val minWidth = max(sortedExpenses.size * 30, 300).dp

                    Column {
                        // Сам график
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
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

                                // Рисуем линию графика
                                val path = Path()
                                sortedExpenses.forEachIndexed { index, expense ->
                                    val x = padding + index * xStep
                                    val y = height - padding - (expense.amount.amount.toDouble() / maxExpense).toFloat() * (height - 2 * padding)

                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }

                                    // Рисуем точки
                                    drawCircle(
                                        color = color,
                                        radius = 4f,
                                        center = Offset(x, y)
                                    )
                                }

                                // Рисуем линию
                                drawPath(
                                    path = path,
                                    color = color.copy(alpha = 0.7f),
                                    style = Stroke(width = 2f)
                                )

                                // Рисуем заполнение под линией
                                val fillPath = Path()
                                fillPath.addPath(path)
                                fillPath.lineTo(padding + (sortedExpenses.size - 1) * xStep, height - padding)
                                fillPath.lineTo(padding, height - padding)
                                fillPath.close()

                                drawPath(
                                    path = fillPath,
                                    color = color.copy(alpha = 0.1f)
                                )
                            }
                        }

                        // Подписи дат внизу
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .width(minWidth)
                                .padding(start = 20.dp, end = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Показываем только некоторые даты, чтобы не перегружать график
                            val datesToShow = if (sortedExpenses.size <= 7) {
                                sortedExpenses
                            } else {
                                // Выбираем равномерно распределенные даты
                                val step = sortedExpenses.size / 5
                                sortedExpenses.filterIndexed { index, _ -> index % step == 0 || index == sortedExpenses.size - 1 }
                            }

                            datesToShow.forEach { expense ->
                                Text(
                                    text = dateFormat.format(expense.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
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
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

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
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Сумма без копеек
        Text(
            text = stringResource(R.string.expense_currency_format, expense.amount.format(false)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
} 