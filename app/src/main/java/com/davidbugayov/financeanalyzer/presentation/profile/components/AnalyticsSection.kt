package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
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
    // Преобразуем Double в Money для лучшего форматирования
    val incomeAmount = Money(totalIncome)
    val expenseAmount = Money(totalExpense)
    val balanceAmount = Money(balance)

    // Процентный формат для нормы сбережений
    val percentFormat = NumberFormat.getPercentInstance(Locale("ru", "RU"))
    
    // Определяем цвета для доходов и расходов
    val incomeColor = Color(0xFF2E7D32) // Темно-зеленый для доходов
    val expenseColor = Color(0xFFB71C1C) // Темно-красный для расходов
    
    // Определяем цвет для баланса в зависимости от его значения
    val balanceColor = if (balance >= 0) incomeColor else expenseColor
    
    // Цвета фона для карточек
    val incomeBackgroundColor = Color(0xFFE0F7E0) // Светло-зеленый фон
    val expenseBackgroundColor = Color(0xFFFFE0E0) // Светло-красный фон
    val balanceBackgroundColor = if (balance >= 0) incomeBackgroundColor else expenseBackgroundColor
    val savingsBackgroundColor = Color(0xFFF5F5F5) // Нейтральный серый фон
    
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
            Text(
                text = stringResource(R.string.profile_financial_summary),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))

            // Основные показатели: доходы и расходы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Доходы
                AnalyticsCard(
                    title = stringResource(R.string.income),
                    value = incomeAmount.formatted(),
                    icon = Icons.Default.KeyboardArrowUp,
                    backgroundColor = incomeBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = incomeColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                // Расходы
                AnalyticsCard(
                    title = stringResource(R.string.expense),
                    value = expenseAmount.formatted(),
                    icon = Icons.Default.KeyboardArrowDown,
                    backgroundColor = expenseBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = expenseColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            // Баланс и норма сбережений в одном ряду
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Баланс
                AnalyticsCard(
                    title = stringResource(R.string.balance),
                    value = balanceAmount.formatted(),
                    icon = Icons.Default.BarChart,
                    backgroundColor = balanceBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = balanceColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))

                // Норма сбережений
                AnalyticsCard(
                    title = stringResource(R.string.savings_rate),
                    value = percentFormat.format(savingsRate / 100),
                    icon = if (savingsRate > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    backgroundColor = savingsBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = if (savingsRate > 0) incomeColor else expenseColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.profile_section_spacing)))
            
            // Дополнительная информация с возможностью перехода на экран статистики, без рамки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToChart)
                    .padding(vertical = dimensionResource(R.dimen.spacing_small)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.show_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSystemInDarkTheme()) Color(0xFF81CFEF) else MaterialTheme.colorScheme.primary
                )
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.cd_show_statistics),
                    tint = if (isSystemInDarkTheme()) Color(0xFF81CFEF) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
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
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.profile_analytics_card_height))
                .padding(dimensionResource(R.dimen.spacing_medium)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                )

                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = iconTint
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        }
    }
}
