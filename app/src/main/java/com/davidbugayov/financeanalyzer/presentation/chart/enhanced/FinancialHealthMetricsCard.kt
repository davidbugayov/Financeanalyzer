package com.davidbugayov.financeanalyzer.presentation.chart.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Карточка с метриками финансового здоровья.
 * Отображает ключевые показатели финансового состояния пользователя с подробными объяснениями.
 *
 * @param savingsRate Процент сохраненного дохода (Норма сбережений) - отношение сбережений к доходу, 
 *                    показывает, какую часть своего дохода вы откладываете. Чем выше показатель, тем лучше.
 * @param averageDailyExpense Средние ежедневные расходы - рассчитываются на основе данных за выбранный период, 
 *                            помогают планировать бюджет.
 * @param monthsOfSavings На сколько месяцев хватит средств при текущих расходах - рассчитывается путем деления 
 *                       общей суммы сбережений на средние месячные расходы.
 * @param modifier Модификатор для настройки внешнего вида
 * @param onInfoClick Коллбэк для отображения дополнительной информации
 */
@Composable
fun FinancialHealthMetricsCard(
    savingsRate: Double,
    averageDailyExpense: Money,
    monthsOfSavings: Double,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {}
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    
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
                    text = "Анализ финансового здоровья",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Информация о метриках",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = "Ключевые показатели для планирования вашего бюджета",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Блок с ежедневными расходами
            MetricItemEnhanced(
                title = "Средние расходы",
                icon = Icons.Filled.MonetizationOn, 
                explanation = "Рассчитывается на основе ваших трат за выбранный период. Помогает планировать ежедневный бюджет."
            ) {
                // Блок с метриками расходов
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Средние расходы в день
                    MetricItem(
                        title = "В день",
                        value = averageDailyExpense.format(true),
                        modifier = Modifier.weight(1f)
                    )

                    // Средние расходы в месяц
                    val monthlyExpense = averageDailyExpense.times(30.toBigDecimal())
                    MetricItem(
                        title = "В месяц",
                        value = monthlyExpense.format(true),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Средние расходы в год
                    val yearlyExpense = averageDailyExpense.times(365.toBigDecimal())
                    MetricItem(
                        title = "В год",
                        value = yearlyExpense.format(true),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Норма сбережений
            MetricItemEnhanced(
                title = "Норма сбережений",
                icon = Icons.Filled.Savings,
                explanation = "Показывает процент дохода, который вы откладываете. Рекомендуемый показатель – от 15% до 30%. Чем выше, тем быстрее растут ваши накопления."
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${savingsRate.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val qualification = when {
                            savingsRate >= 30 -> "Отлично"
                            savingsRate >= 15 -> "Хорошо"
                            savingsRate >= 5 -> "Удовлетворительно"
                            else -> "Требует внимания"
                        }

                        val qualificationColor = when {
                            savingsRate >= 30 -> Color(0xFF00C853) // Ярко-зеленый
                            savingsRate >= 15 -> Color(0xFF66BB6A) // Зеленый
                            savingsRate >= 5 -> Color(0xFFFFA726)  // Оранжевый
                            else -> Color(0xFFEF5350)              // Красный
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Индикатор
                        SavingsRateProgressIndicator(
                            savingsRate = savingsRate,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Стрелочка тренда
                        val trendDirection = if (savingsRate >= 30) TrendDirection.UP
                            else if (savingsRate >= 15) TrendDirection.NEUTRAL
                            else TrendDirection.DOWN
                        
                        Icon(
                            imageVector = when(trendDirection) {
                                TrendDirection.UP -> Icons.Filled.ArrowUpward
                                TrendDirection.DOWN -> Icons.Filled.ArrowDownward
                                TrendDirection.NEUTRAL -> Icons.Filled.ArrowForward
                            },
                            contentDescription = "Тренд сбережений",
                            tint = when(trendDirection) {
                                TrendDirection.UP -> Color(0xFF00C853)
                                TrendDirection.DOWN -> Color(0xFFEF5350)
                                TrendDirection.NEUTRAL -> Color(0xFFFFA726)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = when {
                            savingsRate >= 30 -> "Вы отлично справляетесь с накоплениями! Продолжайте в том же духе."
                            savingsRate >= 15 -> "Хороший результат. Стремитесь увеличить до 30% для более быстрого роста накоплений."
                            savingsRate >= 5 -> "Неплохое начало. Попробуйте увеличить процент сбережений."
                            else -> "Рекомендуется откладывать минимум 10-15% от дохода."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Блок "Хватит на X месяцев"
            if (monthsOfSavings.isFinite() && monthsOfSavings > 0) {
                MetricItemEnhanced(
                    title = "Финансовая подушка",
                    icon = Icons.Filled.CalendarMonth,
                    explanation = "Показывает, на сколько месяцев хватит ваших сбережений при текущем уровне расходов. Рекомендуемый минимум - 3-6 месяцев."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "${monthsOfSavings.toInt()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = "мес.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Text(
                                text = "При ежемесячных расходах ${averageDailyExpense.times(30.toBigDecimal()).format(true)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val qualification = when {
                            monthsOfSavings >= 6 -> "Отлично"
                            monthsOfSavings >= 3 -> "Хорошо"
                            else -> "Недостаточно"
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when {
                            monthsOfSavings >= 6 -> "Вы хорошо защищены от финансовых неожиданностей! Ваш запас превышает рекомендуемый минимум в 6 месяцев."
                            monthsOfSavings >= 3 -> "У вас есть базовая финансовая подушка. Старайтесь увеличить её до 6 месяцев расходов."
                            else -> "Рекомендуется иметь запас на 3-6 месяцев расходов. Работайте над увеличением сбережений."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Диалог с подробным объяснением финансового здоровья
    if (showInfoDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Финансовое здоровье",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Анализ финансового здоровья включает несколько ключевых метрик:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Пояснение норма сбережений
                    Text(
                        text = "1. Норма сбережений",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Показывает, какой процент дохода вы откладываете. Рассчитывается как отношение сбережений к общему доходу. Финансовые эксперты рекомендуют сохранять минимум 15-20% дохода.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Пояснение средние расходы
                    Text(
                        text = "2. Средние расходы",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Ваши расходы в среднем за день, месяц и год, рассчитанные на основе истории трат. Помогают планировать бюджет и оценивать уровень потребления.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Пояснение финансовая подушка
                    Text(
                        text = "3. Финансовая подушка",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Показывает, на сколько месяцев хватит ваших сбережений при текущем уровне расходов. Рекомендуемый минимум - 3-6 месяцев для защиты от непредвиденных ситуаций.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Советы по улучшению финансового здоровья:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "• Постарайтесь откладывать не менее 15-20% от дохода\n• Контролируйте ежедневные расходы\n• Создайте финансовую подушку на 3-6 месяцев\n• Инвестируйте регулярно\n• Избегайте кредитов с высокими процентами",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Дополнительная информация о значениях метрик
                    Text(
                        text = "Рекомендуемые значения показателей:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• Норма сбережений: выше 15% - хорошо, выше 30% - отлично\n• Финансовая подушка: 3-6 месяцев для наемных работников, 6-12 месяцев для предпринимателей\n• Соотношение долга к доходу: не более 30-40% ежемесячного дохода",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { showInfoDialog = false }
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}

/**
 * Улучшенный компонент для отображения метрики с заголовком, иконкой и подсказкой
 */
@Composable
fun MetricItemEnhanced(
    title: String,
    icon: ImageVector,
    explanation: String,
    content: @Composable () -> Unit
) {
    var showExplanation by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            IconButton(
                onClick = { showExplanation = !showExplanation },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Help,
                    contentDescription = "Подсказка",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Отображаем пояснение, если нажата кнопка подсказки
        if (showExplanation) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        content()
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
                Icon(
                    imageVector = when(trend) {
                        TrendDirection.UP -> Icons.Filled.ArrowUpward
                        TrendDirection.DOWN -> Icons.Filled.ArrowDownward
                        TrendDirection.NEUTRAL -> Icons.Filled.ArrowForward
                    },
                    contentDescription = when(trend) {
                        TrendDirection.UP -> "Положительный тренд"
                        TrendDirection.DOWN -> "Отрицательный тренд"
                        TrendDirection.NEUTRAL -> "Нейтральный тренд"
                    },
                    tint = when(trend) {
                        TrendDirection.UP -> Color(0xFF00C853)
                        TrendDirection.DOWN -> Color(0xFFEF5350)
                        TrendDirection.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
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
    
    Box(modifier = modifier) {
        // Фоновая шкала
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .background(Color(0xFFEF5350).copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .height(8.dp)
                    .background(Color(0xFFFFA726).copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .weight(0.15f)
                    .height(8.dp)
                    .background(Color(0xFF66BB6A).copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(Color(0xFF00C853).copy(alpha = 0.2f))
            )
        }
        
        // Прогресс
        LinearProgressIndicator(
            progress = normalizedRate.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = Color.Transparent
        )
        
        // Метки процентов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "15%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "30%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "50%+",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Направление тренда для метрик
 */
enum class TrendDirection {
    /** Положительный тренд (увеличение значения хорошо) */
    UP, 
    /** Отрицательный тренд (уменьшение значения плохо) */
    DOWN, 
    /** Нейтральный тренд (значение стабильно) */
    NEUTRAL
} 