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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Карточка с рекомендациями по планированию бюджета.
 * Отображает советы по планированию бюджета на основе трат пользователя.
 *
 * @param averageMonthlyExpense Средние ежемесячные расходы пользователя
 * @param recommendedSavings Рекомендуемая сумма сбережений
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun BudgetPlanningCard(
    averageMonthlyExpense: Money,
    recommendedSavings: Money,
    modifier: Modifier = Modifier
) {
    // Создаем градиент для фона карточки
    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4F6BED), // Более темный синий вверху
            Color(0xFF66B1FF)  // Более светлый синий внизу
        )
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Будем использовать градиент
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(blueGradient)
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
                        color = Color.White
                    )

                    IconButton(
                        onClick = { /* Показать информацию о бюджетном планировании */ },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Информация о бюджетном планировании",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Рекомендуемое распределение бюджета
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BudgetItem(
                        title = "Необходимые траты",
                        value = "${(averageMonthlyExpense.amount.toFloat() * 0.5f).toInt()} ₽",
                        percentage = "50%",
                        icon = Icons.Rounded.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )

                    BudgetItem(
                        title = "Личные расходы",
                        value = "${(averageMonthlyExpense.amount.toFloat() * 0.3f).toInt()} ₽",
                        percentage = "30%",
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BudgetItem(
                        title = "Рекомендуемые сбережения",
                        value = "${(averageMonthlyExpense.amount.toFloat() * 0.2f).toInt()} ₽",
                        percentage = "20%",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )

                    BudgetItem(
                        title = "Резервный фонд",
                        value = "${recommendedSavings.amount.toInt()} ₽",
                        subtitle = "Цель на 3-6 месяцев",
                        icon = Icons.Rounded.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Совет по бюджету
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Правило 50/30/20 - идеальный баланс для распределения вашего бюджета: " +
                               "50% на необходимые расходы, 30% на личные нужды и 20% на сбережения.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Элемент рекомендации по бюджету
 */
@Composable
private fun BudgetItem(
    title: String,
    value: String,
    percentage: String? = null,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (percentage != null) {
            Text(
                text = percentage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
} 