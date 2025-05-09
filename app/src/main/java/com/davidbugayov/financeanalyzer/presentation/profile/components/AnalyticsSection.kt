package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalInfoColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalWarningColor
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.util.Locale

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
    modifier: Modifier = Modifier,
    totalIncome: Money,
    totalExpense: Money,
    balance: Money,
    savingsRate: Double,
    totalTransactions: Int,
    totalExpenseCategories: Int,
    totalIncomeCategories: Int,
    averageExpense: String,
    totalSourcesUsed: Int,
    dateRange: String = stringResource(R.string.all_time),
    onSavingsRateClick: () -> Unit = {},
) {
    // Цвета для финансовых показателей
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val balanceColor = if (balance.amount >= BigDecimal.ZERO) incomeColor else expenseColor

    // Расчет норм сбережений - используем переданное значение вместо расчета
    val calculatedSavingsRate = savingsRate
    
    // Состояние анимации
    var showFinancialOverview by remember { mutableStateOf(false) }
    var showAdditionalStats by remember { mutableStateOf(false) }
    
    // Запускаем анимацию с задержкой
    LaunchedEffect(Unit) {
        showFinancialOverview = true
        delay(300) // Небольшая задержка для каскадной анимации
        showAdditionalStats = true
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        colors = CardDefaults.cardColors(containerColor = LocalFriendlyCardBackgroundColor.current)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Заголовок секции с иконкой аналитики
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSavingsRateClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_28dp))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_8dp)))
                    Text(
                        text = stringResource(R.string.analytics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
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
            
            // Финансовый обзор с анимацией
            AnimatedVisibility(
                visible = showFinancialOverview,
                enter = fadeIn(animationSpec = tween(500)) + 
                        expandVertically(animationSpec = tween(500))
            ) {
                Column {
                    // Доходы и расходы
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Доход
                        AnimatedFinancialCard(
                            title = stringResource(R.string.income),
                            value = totalIncome.format(),
                            color = incomeColor,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            animationDelay = 0,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                        
                        // Расходы
                        AnimatedFinancialCard(
                            title = stringResource(R.string.expenses),
                            value = totalExpense.format(),
                            color = expenseColor,
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            animationDelay = 100,
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
                        AnimatedFinancialCard(
                            title = stringResource(R.string.balance),
                            value = balance.format(),
                            color = balanceColor,
                            icon = Icons.Default.MonetizationOn,
                            animationDelay = 200,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                        
                        // Норма сбережений
                        val savingsRateColor = when {
                            calculatedSavingsRate >= 20 -> incomeColor // Зеленый
                            calculatedSavingsRate >= 10 -> LocalWarningColor.current // Желтый
                            calculatedSavingsRate > 0 -> LocalInfoColor.current.copy(alpha = 0.8f)  // Оранжевый
                            else -> expenseColor // Красный
                        }
                        
                        AnimatedFinancialCard(
                            title = stringResource(R.string.savings_rate),
                            value = String.format(Locale.US, "%.1f%%", calculatedSavingsRate),
                            color = savingsRateColor,
                            icon = if (calculatedSavingsRate > 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            animationDelay = 300,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSavingsRateClick() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
            
            // Дополнительная статистика с анимацией
            AnimatedVisibility(
                visible = showAdditionalStats,
                enter = fadeIn(animationSpec = tween(500)) + 
                        expandVertically(animationSpec = tween(500))
            ) {
                Column {
                    // Первый ряд с аналитикой - улучшенные карточки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnimatedAnalyticCard(
                            icon = Icons.Default.Assessment,
                            title = stringResource(R.string.total_transactions),
                            value = totalTransactions.toString(),
                            animationDelay = 0,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                        
                        AnimatedAnalyticCard(
                            icon = Icons.Default.Category,
                            title = stringResource(R.string.expense_categories),
                            value = totalExpenseCategories.toString(),
                            animationDelay = 100,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    
                    // Второй ряд с аналитикой - улучшенные карточки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnimatedAnalyticCard(
                            icon = Icons.Default.Category,
                            title = stringResource(R.string.income_categories),
                            value = totalIncomeCategories.toString(),
                            animationDelay = 200,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                        
                        AnimatedAnalyticCard(
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            title = stringResource(R.string.average_expense),
                            value = averageExpense,
                            animationDelay = 300,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                    
                    // Дополнительная информация - улучшенный дизайн
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_20dp))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_8dp)))
                            Text(
                                text = stringResource(R.string.sources_used),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_12dp))
                        ) {
                            Text(
                                text = totalSourcesUsed.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(R.dimen.padding_horizontal_12dp),
                                    vertical = dimensionResource(R.dimen.padding_vertical_6dp)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Карточка с финансовой информацией с анимацией.
 */
@Composable
private fun AnimatedFinancialCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = scale
            },
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Иконка в кружке
            Surface(
                modifier = Modifier.size(dimensionResource(R.dimen.icon_container_size_40dp)),
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_12dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_8dp))
                        .size(dimensionResource(R.dimen.icon_size_24dp))
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Значение - сначала, чтобы акцентировать
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            // Заголовок - после значения
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Карточка с аналитической информацией с анимацией.
 */
@Composable
private fun AnimatedAnalyticCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = scale
            },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка в кружке
            Surface(
                modifier = Modifier.size(dimensionResource(R.dimen.icon_container_size_40dp)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_12dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_8dp))
                        .size(dimensionResource(R.dimen.icon_size_24dp))
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Значение - более акцентированное
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Заголовок
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}