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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

/**
 * Улучшенная карточка с информацией о балансе, доходах и расходах.
 * Дизайн основан на современном стиле Т-Банка.
 *
 * @param income Общий доход за период
 * @param expense Общие расходы за период
 * @param period Текстовое представление периода
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun EnhancedSummaryCard(
    income: Money,
    expense: Money,
    period: String,
    modifier: Modifier = Modifier
) {
    val balance = income.minus(expense)
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current

    // Градиент для фона карточки
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Прозрачный, так как будем использовать градиент
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardGradient)
                .padding(16.dp)
        ) {
            Column {
                // Период
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                // Баланс
                Text(
                    text = balance.format(true),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Строка с доходами и расходами
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Доходы
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(
                                    color = incomeColor,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = null,
                                    tint = incomeColor,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = income.format(true),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = incomeColor
                                    )
                                )
                            }
                            Text(
                                text = "Доходы",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Расходы
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(
                                    color = expenseColor,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = expenseColor,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = expense.format(true),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = expenseColor
                                    )
                                )
                            }
                            Text(
                                text = "Расходы",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
} 