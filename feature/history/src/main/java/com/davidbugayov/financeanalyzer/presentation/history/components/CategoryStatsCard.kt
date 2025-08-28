package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.shared.model.Money
import com.davidbugayov.financeanalyzer.ui.R as UiR
import java.math.BigDecimal

/**
 * Карточка со статистикой по выбранной категории.
 * Отображает текущую и предыдущую суммы, а также процент изменения.
 *
 * @param category Название категории
 * @param currentTotal Сумма за текущий период
 * @param previousTotal Сумма за предыдущий период
 * @param percentChange Процент изменения между периодами (может быть null)
 */
@Composable
fun CategoryStatsCard(
    category: String,
    currentTotal: Money,
    previousTotal: Money,
    percentChange: BigDecimal?,
) {
    val isPositiveChange = percentChange != null && percentChange > BigDecimal.ZERO
    val isNegativeChange = percentChange != null && percentChange < BigDecimal.ZERO

    // Градиент для карточки в зависимости от изменения
    val cardGradient = Brush.verticalGradient(
        colors = when {
            isPositiveChange -> listOf(
                MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                MaterialTheme.colorScheme.error.copy(alpha = 0.02f)
            )
            isNegativeChange -> listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
            )
            else -> listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isPositiveChange -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                isNegativeChange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(12.dp)
        ) {
            Column {
                // Заголовок с иконкой тренда
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Иконка тренда
                    if (percentChange != null) {
                        val trendIcon = when {
                            percentChange > BigDecimal.ZERO -> Icons.AutoMirrored.Filled.TrendingUp
                            percentChange < BigDecimal.ZERO -> Icons.AutoMirrored.Filled.TrendingDown
                            else -> null
                        }

                        trendIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = when {
                                    percentChange > BigDecimal.ZERO -> MaterialTheme.colorScheme.error
                                    percentChange < BigDecimal.ZERO -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Основная статистика
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Текущий период
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(UiR.string.current_period),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = currentTotal.toPlainString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Предыдущий период
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(UiR.string.previous_period),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = previousTotal.toPlainString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Процент изменения
                if (percentChange != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val percentChangeInt = percentChange.setScale(0, java.math.RoundingMode.FLOOR).toInt()
                    val changeText = when {
                        percentChangeInt > 0 -> "+$percentChangeInt%"
                        percentChangeInt < 0 -> "$percentChangeInt%"
                        else -> "0%"
                    }

                    val trendColor = when {
                        percentChangeInt > 0 -> MaterialTheme.colorScheme.error
                        percentChangeInt < 0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (percentChangeInt >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = changeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = trendColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
