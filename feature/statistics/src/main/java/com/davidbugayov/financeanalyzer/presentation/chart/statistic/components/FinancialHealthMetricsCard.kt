package com.davidbugayov.financeanalyzer.presentation.chart.statistic.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.feature.statistics.R

/**
 * Карточка с метриками финансового здоровья.
 * Отображает ключевые показатели финансового состояния пользователя.
 */
@Composable
fun FinancialHealthMetricsCard(
    savingsRate: Double,
    averageDailyExpense: Money,
    monthsOfSavings: Double,
    modifier: Modifier = Modifier,
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(R.dimen.financial_health_card_corner_radius)),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = dimensionResource(R.dimen.financial_health_card_elevation),
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.financial_health_card_padding)),
        ) {
            CardHeader(
                onInfoClick = { showInfoDialog = true },
            )

            Spacer(
                modifier = Modifier.height(dimensionResource(R.dimen.financial_health_card_spacing)),
            )

            // Секция средних расходов
            ExpensesSection(averageDailyExpense)

            SectionDivider()

            // Секция нормы сбережений
            SavingsRateSection(savingsRate)

            SectionDivider()

            // Секция финансовой подушки
            if (monthsOfSavings.isFinite() && monthsOfSavings > 0) {
                FinancialCushionSection(monthsOfSavings, averageDailyExpense)
            }
        }
    }

    // Диалог с подробным объяснением финансового здоровья
    if (showInfoDialog) {
        FinancialHealthInfoDialog(
            onDismiss = { showInfoDialog = false },
        )
    }
}

/**
 * Заголовок карточки с метриками финансового здоровья
 */
@Composable
private fun CardHeader(onInfoClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(com.davidbugayov.financeanalyzer.ui.R.string.insight_financial_health),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.padding(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription =
                        stringResource(
                            R.string.financial_health_info_description,
                        ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = stringResource(R.string.financial_health_info_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Секция с информацией о средних расходах
 */
@Composable
private fun ExpensesSection(averageDailyExpense: Money) {
    val monthlyExpense = averageDailyExpense.times(30.toBigDecimal())
    val yearlyExpense = averageDailyExpense.times(365.toBigDecimal())

    MetricItemEnhanced(
        title = stringResource(R.string.average_expenses_title),
        icon = Icons.Filled.MonetizationOn,
        explanation = stringResource(R.string.average_expenses_explanation),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MetricItem(
                title = stringResource(R.string.daily_expenses_title),
                value = averageDailyExpense.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                modifier = Modifier.weight(1f),
            )

            MetricItem(
                title = stringResource(R.string.monthly_expenses_title),
                value = monthlyExpense.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                modifier = Modifier.weight(1f),
            )

            MetricItem(
                title = stringResource(R.string.yearly_expenses_title),
                value = yearlyExpense.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Секция с информацией о норме сбережений
 */
@Composable
private fun SavingsRateSection(savingsRate: Double) {
    val qualification =
        when {
            savingsRate >= 30 -> stringResource(R.string.savings_rate_excellent_qualification)
            savingsRate >= 15 -> stringResource(R.string.savings_rate_good_qualification)
            savingsRate >= 5 -> stringResource(R.string.savings_rate_satisfactory_qualification)
            else -> stringResource(R.string.savings_rate_needs_attention_qualification)
        }

    val qualificationColor =
        when {
            savingsRate >= 30 -> colorResource(R.color.savings_rate_excellent)
            savingsRate >= 15 -> colorResource(R.color.savings_rate_good)
            savingsRate >= 5 -> colorResource(R.color.savings_rate_satisfactory)
            else -> colorResource(R.color.savings_rate_needs_attention)
        }

    val trendDirection =
        when {
            savingsRate >= 30 -> TrendDirection.UP
            savingsRate >= 15 -> TrendDirection.NEUTRAL
            else -> TrendDirection.DOWN
        }

    val recommendationText =
        when {
            savingsRate >= 30 -> stringResource(R.string.savings_rate_excellent_recommendation)
            savingsRate >= 15 -> stringResource(R.string.savings_rate_good_recommendation)
            savingsRate >= 5 -> stringResource(R.string.savings_rate_satisfactory_recommendation)
            else -> stringResource(R.string.savings_rate_needs_attention_recommendation)
        }

    MetricItemEnhanced(
        title = stringResource(R.string.savings_rate_title),
        icon = Icons.Filled.Savings,
        explanation = stringResource(R.string.savings_rate_explanation),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.savings_rate_percent,
                            savingsRate.toBigDecimal().setScale(0, java.math.RoundingMode.FLOOR).toInt(),
                        ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                QualificationBadge(qualification, qualificationColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SavingsRateProgressIndicator(
                    savingsRate = savingsRate,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                TrendIcon(trendDirection)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recommendationText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Секция с информацией о финансовой подушке
 */
@Composable
private fun FinancialCushionSection(
    monthsOfSavings: Double,
    averageDailyExpense: Money,
) {
    val qualification =
        when {
            monthsOfSavings >= 6 -> stringResource(R.string.financial_cushion_excellent_qualification)
            monthsOfSavings >= 3 -> stringResource(R.string.financial_cushion_good_qualification)
            else -> stringResource(R.string.financial_cushion_insufficient_qualification)
        }

    val qualificationColor =
        when {
            monthsOfSavings >= 6 -> colorResource(R.color.financial_cushion_excellent)
            monthsOfSavings >= 3 -> colorResource(R.color.financial_cushion_good)
            else -> colorResource(R.color.financial_cushion_insufficient)
        }

    val recommendationText =
        when {
            monthsOfSavings >= 6 -> stringResource(R.string.financial_cushion_excellent_recommendation)
            monthsOfSavings >= 3 -> stringResource(R.string.financial_cushion_good_recommendation)
            else -> stringResource(R.string.financial_cushion_insufficient_recommendation)
        }

    val monthlyExpense = averageDailyExpense.times(30.toBigDecimal())

    MetricItemEnhanced(
        title = stringResource(R.string.financial_cushion_title),
        icon = Icons.Filled.CalendarMonth,
        explanation = stringResource(R.string.financial_cushion_explanation),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string.financial_cushion_months,
                                    monthsOfSavings.toBigDecimal().setScale(
                                        0,
                                        java.math.RoundingMode.FLOOR,
                                    ).toPlainString(),
                                ),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${monthsOfSavings.toInt()} мес.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    Text(
                        text =
                            stringResource(
                                R.string.financial_cushion_expenses,
                                monthlyExpense.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                QualificationBadge(qualification, qualificationColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recommendationText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Разделитель между секциями
 */
@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_health_section_spacing)))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_health_section_spacing)))
}

/**
 * Бейдж с квалификацией
 */
@Composable
private fun QualificationBadge(
    qualification: String,
    qualificationColor: Color,
) {
    Box(
        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(dimensionResource(R.dimen.financial_health_badge_corner_radius)),
                )
                .background(qualificationColor.copy(alpha = 0.15f))
                .padding(
                    horizontal = dimensionResource(R.dimen.financial_health_badge_padding_horizontal),
                    vertical = dimensionResource(R.dimen.financial_health_badge_padding_vertical),
                ),
    ) {
        Text(
            text = qualification,
            style = MaterialTheme.typography.bodyMedium,
            color = qualificationColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Иконка тренда
 */
@Composable
private fun TrendIcon(trendDirection: TrendDirection) {
    val (imageVector, contentDescription, tint) =
        when (trendDirection) {
            TrendDirection.UP ->
                Triple(
                    Icons.Filled.ArrowUpward,
                    stringResource(R.string.trend_positive_description),
                    colorResource(R.color.trend_positive),
                )
            TrendDirection.DOWN ->
                Triple(
                    Icons.Filled.ArrowDownward,
                    stringResource(R.string.trend_negative_description),
                    colorResource(R.color.trend_negative),
                )
            TrendDirection.NEUTRAL ->
                Triple(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    stringResource(R.string.trend_neutral_description),
                    colorResource(R.color.trend_neutral),
                )
        }

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(dimensionResource(R.dimen.financial_health_icon_size)),
    )
}

/**
 * Диалог с подробной информацией о финансовом здоровье
 */
@Composable
private fun FinancialHealthInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.financial_health_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = dimensionResource(R.dimen.financial_health_dialog_max_height))
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(R.string.financial_health_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                // Пояснение норма сбережений
                InfoSectionTitle(
                    text = stringResource(R.string.financial_health_savings_rate_title),
                )
                InfoSectionContent(
                    text = stringResource(R.string.financial_health_savings_rate_content),
                )

                // Пояснение средние расходы
                InfoSectionTitle(
                    text = stringResource(R.string.financial_health_average_expenses_title),
                )
                InfoSectionContent(
                    text = stringResource(R.string.financial_health_average_expenses_content),
                )

                // Пояснение финансовая подушка
                InfoSectionTitle(
                    text = stringResource(R.string.financial_health_financial_cushion_title),
                )
                InfoSectionContent(
                    text = stringResource(R.string.financial_health_financial_cushion_content),
                )

                Spacer(modifier = Modifier.height(8.dp))

                InfoSectionTitle(text = stringResource(R.string.financial_health_tips_title))
                InfoSectionContent(
                    text = stringResource(R.string.financial_health_tips_content),
                )

                // Дополнительная информация о значениях метрик
                InfoSectionTitle(
                    text = stringResource(R.string.financial_health_recommended_values_title),
                    topPadding = 12.dp,
                )

                InfoSectionContent(
                    text = stringResource(R.string.financial_health_recommended_values_content),
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.financial_health_ok_button))
            }
        },
    )
}

/**
 * Заголовок раздела в информационном диалоге
 */
@Composable
private fun InfoSectionTitle(
    text: String,
    topPadding: Dp = 8.dp,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = topPadding, bottom = 4.dp),
    )
}

/**
 * Содержимое раздела в информационном диалоге
 */
@Composable
private fun InfoSectionContent(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
    )
}

/**
 * Улучшенный компонент для отображения метрики с заголовком, иконкой и подсказкой
 */
@Composable
fun MetricItemEnhanced(
    title: String,
    icon: ImageVector,
    explanation: String,
    content: @Composable () -> Unit,
) {
    var showExplanation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.financial_health_icon_size)),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = { showExplanation = !showExplanation },
                modifier = Modifier.size(dimensionResource(R.dimen.financial_health_icon_size)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Help,
                    contentDescription =
                        stringResource(
                            R.string.financial_health_tooltip_description,
                        ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.size(
                            dimensionResource(R.dimen.financial_health_help_icon_size),
                        ),
                )
            }
        }

        // Отображаем пояснение, если нажата кнопка подсказки
        if (showExplanation) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp),
            ) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    trend: TrendDirection? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (trend != null) {
                Spacer(modifier = Modifier.width(4.dp))
                TrendIcon(trend)
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
    modifier: Modifier = Modifier,
) {
    val normalizedRate = (savingsRate / 100.0).coerceIn(0.0, 1.0)
    val progressColor =
        when {
            savingsRate >= 30 -> colorResource(R.color.savings_rate_excellent)
            savingsRate >= 15 -> colorResource(R.color.savings_rate_good)
            savingsRate >= 5 -> colorResource(R.color.savings_rate_satisfactory)
            else -> colorResource(R.color.savings_rate_needs_attention)
        }

    Box(modifier = modifier) {
        // Фоновая шкала с цветовыми зонами
        Row(modifier = Modifier.fillMaxWidth()) {
            // Красная зона (0-5%)
            Box(
                modifier =
                    Modifier
                        .weight(0.15f)
                        .height(dimensionResource(R.dimen.financial_health_progress_height))
                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        .background(
                            colorResource(R.color.savings_rate_needs_attention).copy(alpha = 0.2f),
                        ),
            )
            // Оранжевая зона (5-15%)
            Box(
                modifier =
                    Modifier
                        .weight(0.1f)
                        .height(dimensionResource(R.dimen.financial_health_progress_height))
                        .background(colorResource(R.color.savings_rate_satisfactory).copy(alpha = 0.2f)),
            )
            // Зеленая зона (15-30%)
            Box(
                modifier =
                    Modifier
                        .weight(0.15f)
                        .height(dimensionResource(R.dimen.financial_health_progress_height))
                        .background(colorResource(R.color.savings_rate_good).copy(alpha = 0.2f)),
            )
            // Ярко-зеленая зона (30%+)
            Box(
                modifier =
                    Modifier
                        .weight(0.6f)
                        .height(dimensionResource(R.dimen.financial_health_progress_height))
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(colorResource(R.color.savings_rate_excellent).copy(alpha = 0.2f)),
            )
        }

        // Индикатор прогресса
        LinearProgressIndicator(
            progress = { normalizedRate.toFloat() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.financial_health_progress_height))
                    .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = Color.Transparent,
        )

        // Метки процентов
        ProgressLabels()
    }
}

/**
 * Метки процентов для индикатора сбережений
 */
@Composable
private fun ProgressLabels() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.savings_rate_0_percent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.savings_rate_15_percent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.savings_rate_30_percent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.savings_rate_50_percent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    NEUTRAL,
}
