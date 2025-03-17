package com.davidbugayov.financeanalyzer.presentation.profile.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * Компонент для отображения финансовой статистики и аналитики.
 * @param totalIncome Общий доход.
 * @param totalExpense Общий расход.
 * @param balance Текущий баланс.
 * @param savingsRate Процент сбережений.
 * @param onNavigateToChart Обработчик нажатия для перехода на экран статистики.
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun AnalyticsSection(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    savingsRate: Double,
    onNavigateToChart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    val percentFormat = NumberFormat.getPercentInstance(Locale("ru", "RU"))
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Финансовая сводка",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Основные показатели
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Доходы
                AnalyticsCard(
                    title = "Доходы",
                    value = currencyFormat.format(totalIncome),
                    icon = Icons.Default.ArrowUpward,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Расходы
                AnalyticsCard(
                    title = "Расходы",
                    value = currencyFormat.format(totalExpense),
                    icon = Icons.Default.ArrowDownward,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Баланс
                AnalyticsCard(
                    title = "Баланс",
                    value = currencyFormat.format(balance),
                    icon = Icons.Default.BarChart,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Процент сбережений
                AnalyticsCard(
                    title = "Сбережения",
                    value = percentFormat.format(savingsRate / 100),
                    icon = if (savingsRate > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconTint = if (savingsRate > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Дополнительная информация с возможностью перехода на экран статистики
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToChart)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Подробная аналитика доступна в разделе \"Статистика\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Перейти к статистике",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Карточка с аналитическими данными.
 * @param title Заголовок карточки.
 * @param value Значение.
 * @param icon Иконка.
 * @param backgroundColor Цвет фона.
 * @param contentColor Цвет содержимого.
 * @param iconTint Цвет иконки.
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 