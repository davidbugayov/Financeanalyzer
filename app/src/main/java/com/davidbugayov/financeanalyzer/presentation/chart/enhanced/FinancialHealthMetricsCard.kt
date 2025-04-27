package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Карточка с метриками финансового здоровья.
 * Отображает ключевые показатели финансового состояния пользователя.
 *
 * @param savingsRate Процент сохраненного дохода
 * @param averageDailyExpense Средние ежедневные расходы
 * @param monthsOfSavings На сколько месяцев хватит средств при текущих расходах
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun FinancialHealthMetricsCard(
    savingsRate: Double,
    averageDailyExpense: Money,
    monthsOfSavings: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок секции
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Анализ ваших средних трат для планирования бюджета",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { /* Показать информацию о метриках */ },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Информация о метриках",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Блок с метриками
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Средние расходы в день
                MetricItem(
                    title = "Среднее за день",
                    value = averageDailyExpense.format(true),
                    modifier = Modifier.weight(1f)
                )

                // Средние расходы в месяц
                val monthlyExpense = averageDailyExpense.times(30.toBigDecimal())
                MetricItem(
                    title = "Среднее за месяц",
                    value = monthlyExpense.format(true),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Вторая строка метрик
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Средние расходы в год
                val yearlyExpense = averageDailyExpense.times(365.toBigDecimal())
                MetricItem(
                    title = "Среднее за год",
                    value = yearlyExpense.format(true),
                    modifier = Modifier.weight(1f)
                )

                // Норма сбережений
                MetricItem(
                    title = "Норма сбережений",
                    value = "${savingsRate.toInt()}%",
                    trend = if (savingsRate >= 30) TrendDirection.UP
                    else if (savingsRate >= 15) TrendDirection.NEUTRAL
                    else TrendDirection.DOWN,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Индикатор сбережений
            Column {
                Text(
                    text = "Процент дохода, который вы сохраняете",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                SavingsRateProgressIndicator(
                    savingsRate = savingsRate,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val qualification = when {
                        savingsRate >= 30 -> "Отлично"
                        savingsRate >= 15 -> "Хорошая"
                        savingsRate >= 5 -> "Средняя"
                        else -> "Требует внимания"
                    }

                    val qualificationColor = when {
                        savingsRate >= 30 -> Color(0xFF00C853) // Ярко-зеленый
                        savingsRate >= 15 -> Color(0xFF66BB6A) // Зеленый
                        savingsRate >= 5 -> Color(0xFFFFA726)  // Оранжевый
                        else -> Color(0xFFEF5350)              // Красный
                    }

                    Text(
                        text = qualification,
                        style = MaterialTheme.typography.bodyMedium,
                        color = qualificationColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Блок "Хватит на X месяцев"
            if (monthsOfSavings.isFinite() && monthsOfSavings > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Хватит на ${monthsOfSavings.toInt()} месяца",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "При ваших средних тратах ${averageDailyExpense.times(30.toBigDecimal()).format(true)}/мес",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val qualification = when {
                            monthsOfSavings >= 6 -> "Отлично"
                            monthsOfSavings >= 3 -> "Хорошо"
                            else -> "Средне"
                        }

                        val qualificationColor = when {
                            monthsOfSavings >= 6 -> Color(0xFF00C853) // Ярко-зеленый
                            monthsOfSavings >= 3 -> Color(0xFF66BB6A) // Зеленый
                            else -> Color(0xFFFFA726)  // Оранжевый
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(qualificationColor.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = qualification,
                                style = MaterialTheme.typography.bodyMedium,
                                color = qualificationColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения отдельной метрики
 */
@Composable
fun MetricItem(
    title: String,
    value: String,
    trend: TrendDirection? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (trend != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (trend) {
                        TrendDirection.UP -> "↑"
                        TrendDirection.DOWN -> "↓"
                        TrendDirection.NEUTRAL -> "→"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = when (trend) {
                        TrendDirection.UP -> Color(0xFF00C853)
                        TrendDirection.DOWN -> Color(0xFFEF5350)
                        TrendDirection.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Индикатор процента сохраняемого дохода
 */
@Composable
fun SavingsRateProgressIndicator(
    savingsRate: Double,
    modifier: Modifier = Modifier
) {
    val normalizedRate = (savingsRate / 100.0).coerceIn(0.0, 1.0)
    val progressColor = when {
        savingsRate >= 30.0 -> Color(0xFF00C853) // Ярко-зеленый
        savingsRate >= 15.0 -> Color(0xFF66BB6A) // Зеленый
        savingsRate >= 5.0 -> Color(0xFFFFA726)  // Оранжевый
        else -> Color(0xFFEF5350)              // Красный
    }

    LinearProgressIndicator(
        progress = normalizedRate.toFloat(),
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = progressColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Направление тренда для метрик
 */
enum class TrendDirection {

    UP, DOWN, NEUTRAL
} 