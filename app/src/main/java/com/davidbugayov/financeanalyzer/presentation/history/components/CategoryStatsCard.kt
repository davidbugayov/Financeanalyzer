package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    percentChange: BigDecimal?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_medium)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.current_period),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = currentTotal.format(false),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.previous_period),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = previousTotal.format(false),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (percentChange != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                val percentChangeInt = percentChange.setScale(0, java.math.RoundingMode.FLOOR).toInt()
                val changeText = when {
                    percentChangeInt > 0 -> stringResource(R.string.change_increase, percentChangeInt)
                    percentChangeInt < 0 -> stringResource(R.string.change_decrease, kotlin.math.abs(percentChangeInt))
                    else -> stringResource(R.string.change_no_change)
                }
                val percentChangeColor = when {
                    percentChangeInt > 0 -> MaterialTheme.colorScheme.error // Красный для увеличения расходов
                    percentChangeInt < 0 -> MaterialTheme.colorScheme.primary // Зеленый для уменьшения расходов
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = changeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = percentChangeColor
                )
            }
        }
    }
} 