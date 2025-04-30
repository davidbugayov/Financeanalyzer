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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Карточка с рекомендациями по улучшению финансового здоровья
 *
 * @param averageMonthlyExpense Средние ежемесячные расходы пользователя
 * @param savingsRate Текущий процент сбережений
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun BudgetRecommendationsCard(
    averageMonthlyExpense: Money,
    savingsRate: Double,
    modifier: Modifier = Modifier
) {
    // Создаем градиент для фона карточки - синие оттенки
    val blueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E88E5), // Более темный синий вверху
            Color(0xFF64B5F6)  // Более светлый синий внизу
        )
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Прозрачный для градиента
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
                        text = "Финансовые рекомендации",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { /* TODO: Показать подробную информацию */ },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Информация о рекомендациях",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Рекомендации на основе текущего состояния
                if (savingsRate < 0.1) {
                    RecommendationItem(
                        title = "Увеличьте норму сбережений",
                        description = "Стремитесь откладывать минимум 10% от дохода. Сейчас: ${(savingsRate * 100).toInt()}%",
                        icon = Icons.Default.Savings
                    )
                } else {
                    RecommendationItem(
                        title = "Хорошая норма сбережений!",
                        description = "Вы откладываете ${(savingsRate * 100).toInt()}% от дохода. Продолжайте в том же духе!",
                        icon = Icons.Default.TrendingUp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Рекомендация по инвестированию
                RecommendationItem(
                    title = "Инвестируйте свободные средства",
                    description = "Рассмотрите инвестирование ${averageMonthlyExpense.formatForDisplay(false)} ежемесячно для долгосрочного роста",
                    icon = Icons.Default.AccountBalance
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Совет по финансовому планированию
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Регулярно пересматривайте свой бюджет и корректируйте финансовые цели. " +
                               "Создание резервного фонда на случай непредвиденных расходов должно быть вашим приоритетом.",
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
 * Элемент рекомендации с иконкой, заголовком и описанием
 */
@Composable
private fun RecommendationItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
} 