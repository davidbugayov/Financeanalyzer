package com.davidbugayov.financeanalyzer.presentation.budget.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import kotlin.math.min

@Composable
fun BudgetCategoryCard(
    category: BudgetCategory,
    onCategoryClick: (String) -> Unit,
    onMenuClick: (BudgetCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Рассчитываем процент расходов от лимита (не больше 100%)
    val spentPercentage = min((category.spent / category.limit).toFloat(), 1f)
    
    // Определяем, превышен ли лимит бюджета
    val isOverBudget = category.spent > category.limit
    
    // Определяем цвет индикатора в зависимости от процента трат
    val indicatorColor = when {
        isOverBudget -> Color.Red
        spentPercentage > 0.8f -> Color(0xFFFF9800) // Оранжевый при приближении к лимиту
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок карточки с названием категории и кнопкой меню
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(onClick = { onMenuClick(category) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Опции для ${category.name}"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Прогресс расходов
            LinearProgressIndicator(
                progress = { spentPercentage },
                modifier = Modifier.fillMaxWidth(),
                color = indicatorColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Информация о расходах и лимите
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Потрачено: ${category.spent.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOverBudget) Color.Red else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Лимит: ${category.limit.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Информация о кошельке с иконкой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Баланс кошелька",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "  Кошелёк: ${category.walletBalance.toInt()} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Кнопка для перехода к транзакциям категории
                IconButton(onClick = { onCategoryClick(category.id) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Перейти к транзакциям ${category.name}"
                    )
                }
            }
        }
    }
} 