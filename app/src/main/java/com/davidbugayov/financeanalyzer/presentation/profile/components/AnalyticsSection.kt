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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            .padding(vertical = 8.dp), // Добавляем вертикальный отступ
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp) // Увеличиваем скругление углов
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Финансовая сводка",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = balanceColor // Цвет заголовка соответствует балансу
            )

            Spacer(modifier = Modifier.height(20.dp)) // Увеличиваем отступ

            // Основные показатели
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Доходы
                AnalyticsCard(
                    title = "Доходы",
                    value = currencyFormat.format(totalIncome),
                    icon = Icons.Default.KeyboardArrowUp,
                    backgroundColor = incomeBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = incomeColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp)) // Увеличиваем расстояние между карточками

                // Расходы
                AnalyticsCard(
                    title = "Расходы",
                    value = currencyFormat.format(totalExpense),
                    icon = Icons.Default.KeyboardArrowDown,
                    backgroundColor = expenseBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = expenseColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // Увеличиваем отступ между рядами

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Баланс
                AnalyticsCard(
                    title = "Баланс",
                    value = currencyFormat.format(balance),
                    icon = Icons.Default.BarChart,
                    backgroundColor = balanceBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = balanceColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp)) // Увеличиваем расстояние между карточками

                // Процент сбережений
                AnalyticsCard(
                    title = "Сбережения",
                    value = percentFormat.format(savingsRate / 100),
                    icon = if (savingsRate > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    backgroundColor = savingsBackgroundColor,
                    contentColor = Color.Black,
                    iconTint = if (savingsRate > 0) incomeColor else expenseColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp)) // Увеличиваем отступ

            // Дополнительная информация с возможностью перехода на экран статистики, без рамки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToChart)
                    .padding(vertical = 4.dp), // Уменьшаем отступ
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Подробная аналитика доступна в разделе \"Статистика\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSystemInDarkTheme()) Color(0xFF81CFEF) else MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Перейти к статистике",
                    tint = if (isSystemInDarkTheme()) Color(0xFF81CFEF) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
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
        shape = RoundedCornerShape(12.dp) // Увеличиваем скругление углов карточек
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp) // Увеличиваем внутренние отступы
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp) // Увеличиваем размер иконки
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = iconTint // Цвет заголовка соответствует цвету иконки
                )
            }

            Spacer(modifier = Modifier.height(10.dp)) // Увеличиваем отступ

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconTint // Цвет значения соответствует цвету иконки
            )
        }
    }
}