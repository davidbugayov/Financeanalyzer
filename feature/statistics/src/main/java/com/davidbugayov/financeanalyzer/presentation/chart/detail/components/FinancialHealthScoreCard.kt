package com.davidbugayov.financeanalyzer.presentation.chart.detail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.HealthScoreBreakdown
import com.davidbugayov.financeanalyzer.feature.statistics.R

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
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.financial_statistics_card_corner_radius)),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = dimensionResource(R.dimen.financial_statistics_card_elevation),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.financial_statistics_card_padding)),
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
                    text = stringResource(R.string.financial_health_score_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_large)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Круговая диаграмма с общим скором
                HealthScoreCircle(
                    score = healthScore,
                    isVisible = isVisible,
                    modifier = Modifier.size(120.dp),
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Детализация по компонентам
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    HealthScoreComponent(
                        title = stringResource(R.string.savings_rate_component),
                        score = breakdown.savingsRateScore,
                        maxScore = 25.0,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthScoreComponent(
                        title = stringResource(R.string.income_stability_component),
                        score = breakdown.incomeStabilityScore,
                        maxScore = 25.0,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthScoreComponent(
                        title = stringResource(R.string.expense_control_component),
                        score = breakdown.expenseControlScore,
                        maxScore = 25.0,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthScoreComponent(
                        title = stringResource(R.string.diversification_component),
                        score = breakdown.diversificationScore,
                        maxScore = 25.0,
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.financial_statistics_spacer_medium)))

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
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) (score / 100.0).toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "health_score_progress",
    )

    val scoreColor = getHealthScoreColor(score)
    val animatedColor by animateColorAsState(
        targetValue = scoreColor,
        animationSpec = tween(durationMillis = 1000),
        label = "health_score_color",
    )

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
                color = animatedColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
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
                color = animatedColor,
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
                    stringResource(R.string.health_score_excellent),
                    Icons.AutoMirrored.Filled.TrendingUp,
                    Color(0xFF4CAF50),
                )
            score >= 60 ->
                Triple(
                    stringResource(R.string.health_score_good),
                    Icons.AutoMirrored.Filled.TrendingUp,
                    Color(0xFF8BC34A),
                )
            score >= 40 ->
                Triple(
                    stringResource(R.string.health_score_average),
                    Icons.AutoMirrored.Filled.TrendingFlat,
                    Color(0xFFFF9800),
                )
            else ->
                Triple(
                    stringResource(R.string.health_score_poor),
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
private fun getHealthScoreColor(score: Double): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Зеленый
        score >= 60 -> Color(0xFF8BC34A) // Светло-зеленый
        score >= 40 -> Color(0xFFFF9800) // Оранжевый
        else -> Color(0xFFF44336) // Красный
    }
}

/**
 * Получает цвет для компонента скора
 */
private fun getComponentScoreColor(progress: Double): Color {
    return when {
        progress >= 0.8 -> Color(0xFF4CAF50)
        progress >= 0.6 -> Color(0xFF8BC34A)
        progress >= 0.4 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}
