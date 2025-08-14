package com.davidbugayov.financeanalyzer.presentation.chart.detail.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.HealthAndSafety
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.HealthScoreBreakdown
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Карточка с коэффициентом финансового здоровья.
 * Отображает общий скор, его компоненты и визуальную индикацию.
 */
@Composable
fun FinancialHealthScoreCard(
    healthScore: Double,
    breakdown: HealthScoreBreakdown,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                dimensionResource(UiR.dimen.financial_statistics_card_corner_radius),
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    dimensionResource(
                        UiR.dimen.financial_statistics_card_elevation,
                    ),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        dimensionResource(
                            UiR.dimen.financial_statistics_card_padding,
                        ),
                    ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Коэффициент финансового здоровья",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(
                Modifier.height(
                    dimensionResource(UiR.dimen.financial_statistics_spacer_large),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Круговая диаграмма с общим скором
                HealthScoreCircle(
                    score = healthScore,
                    modifier = Modifier.size(120.dp),
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Детализация по компонентам
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    HealthScoreComponent(
                        title = "Норма сбережений",
                        score = breakdown.savingsRateScore,
                        maxScore = 25.0,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthScoreComponent(
                        title = "Стабильность доходов",
                        score = breakdown.incomeStabilityScore,
                        maxScore = 25.0,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthScoreComponent(
                        title = "Контроль расходов",
                        score = breakdown.expenseControlScore,
                        maxScore = 25.0,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthScoreComponent(
                        title = "Диверсификация",
                        score = breakdown.diversificationScore,
                        maxScore = 25.0,
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        dimensionResource(
                            UiR.dimen.financial_statistics_spacer_medium,
                        ),
                    ),
            )

            // Интерпретация скора
            HealthScoreInterpretation(healthScore)
        }
    }
}

/**
 * Круговая диаграмма с общим скором
 */
@Composable
private fun HealthScoreCircle(
    score: Double,
    modifier: Modifier = Modifier,
) {
    val progress = (score / 100.0).toFloat()
    val scoreColor = getHealthScoreColor(score)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Фоновая окружность
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(width = strokeWidth),
            )

            // Прогресс
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${score.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Компонент скора (например, норма сбережений)
 */
@Composable
private fun HealthScoreComponent(
    title: String,
    score: Double,
    maxScore: Double,
    modifier: Modifier = Modifier,
) {
    val progress = (score / maxScore).coerceIn(0.0, 1.0)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Прогресс-бар
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progress.toFloat())
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(getComponentScoreColor(progress)),
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${score.toInt()}/${maxScore.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Интерпретация общего скора
 */
@Composable
private fun HealthScoreInterpretation(score: Double) {
    val (interpretation, icon, color) =
        when {
            score >= 80 ->
                Triple(
                    "Отлично",
                    Icons.AutoMirrored.Filled.TrendingUp,
                    Color(0xFF4CAF50),
                )
            score >= 60 ->
                Triple(
                    "Хорошо",
                    Icons.AutoMirrored.Filled.TrendingUp,
                    Color(0xFF8BC34A),
                )
            score >= 40 ->
                Triple(
                    "Средне",
                    Icons.AutoMirrored.Filled.TrendingFlat,
                    Color(0xFFFF9800),
                )
            else ->
                Triple(
                    "Плохо",
                    Icons.AutoMirrored.Filled.TrendingDown,
                    Color(0xFFF44336),
                )
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.1f))
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = interpretation,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Получает цвет для общего скора здоровья
 */
private fun getHealthScoreColor(score: Double): Color =
    when {
        score >= 80 -> Color(0xFF4CAF50) // Зеленый
        score >= 60 -> Color(0xFF8BC34A) // Светло-зеленый
        score >= 40 -> Color(0xFFFF9800) // Оранжевый
        else -> Color(0xFFF44336) // Красный
    }

/**
 * Получает цвет для компонента скора
 */
private fun getComponentScoreColor(progress: Double): Color =
    when {
        progress >= 0.8 -> Color(0xFF4CAF50)
        progress >= 0.6 -> Color(0xFF8BC34A)
        progress >= 0.4 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
