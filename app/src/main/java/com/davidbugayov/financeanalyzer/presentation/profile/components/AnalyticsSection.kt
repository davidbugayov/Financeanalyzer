package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Компонент для отображения финансовой статистики и аналитики.
 * @param totalIncome Общий доход
 * @param totalExpense Общий расход
 * @param balance Текущий баланс
 * @param savingsRate Норма сбережений
 * @param totalTransactions Общее количество транзакций
 * @param totalExpenseCategories Общее количество категорий расходов
 * @param totalIncomeCategories Общее количество категорий доходов
 * @param averageExpense Средний расход
 * @param totalSourcesUsed Общее количество использованных источников
 * @param dateRange Строка с диапазоном дат для отображения
 * @param onSavingsRateClick Обработчик нажатия на норму сбережений
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun AnalyticsSection(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    savingsRate: Double,
    totalTransactions: Int,
    totalExpenseCategories: Int,
    totalIncomeCategories: Int,
    averageExpense: String,
    totalSourcesUsed: Int,
    dateRange: String = "Все время",
    onSavingsRateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            // Заголовок секции
            SectionHeader(
                title = stringResource(R.string.analytics_title),
                onClick = onSavingsRateClick
            )
            
            // Отображение диапазона дат
            if (dateRange.isNotEmpty()) {
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_small))
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Финансовый обзор
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Доход
                FinancialCard(
                    title = stringResource(R.string.income),
                    value = Money(totalIncome).format(),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                // Расходы
                FinancialCard(
                    title = stringResource(R.string.expenses),
                    value = Money(totalExpense).format(),
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Баланс и норма сбережений
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Баланс
                FinancialCard(
                    title = stringResource(R.string.balance),
                    value = Money(balance).format(),
                    color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                // Норма сбережений
                FinancialCard(
                    title = stringResource(R.string.savings_rate),
                    value = String.format("%.1f%%", savingsRate),
                    color = when {
                        savingsRate >= 20 -> Color(0xFF4CAF50)
                        savingsRate >= 10 -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    },
                    icon = if (savingsRate >= 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSavingsRateClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
            
            // Первый ряд с аналитикой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalyticCard(
                    icon = Icons.Default.MonetizationOn,
                    title = stringResource(R.string.total_transactions),
                    value = totalTransactions.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                AnalyticCard(
                    icon = Icons.Default.Category,
                    title = stringResource(R.string.expense_categories),
                    value = totalExpenseCategories.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Второй ряд с аналитикой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalyticCard(
                    icon = Icons.Default.Category,
                    title = stringResource(R.string.income_categories),
                    value = totalIncomeCategories.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                AnalyticCard(
                    icon = Icons.Default.Assessment,
                    title = stringResource(R.string.average_expense),
                    value = averageExpense,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
            
            // Дополнительная информация
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sources_used),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = totalSourcesUsed.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Карточка с финансовой информацией.
 */
@Composable
private fun FinancialCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Иконка
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            // Заголовок
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            // Значение
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Карточка с аналитической информацией.
 */
@Composable
private fun AnalyticCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Иконка
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            // Заголовок
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            
            // Значение
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Заголовок секции.
 */
@Composable
private fun SectionHeader(
    title: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}