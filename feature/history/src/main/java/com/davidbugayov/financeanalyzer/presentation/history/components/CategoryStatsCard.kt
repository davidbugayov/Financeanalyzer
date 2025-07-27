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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.feature.history.R
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
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.spacing_medium)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal)),
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Текущий период",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = currentTotal.format(false),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Предыдущий период",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = previousTotal.format(false),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            if (percentChange != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                val percentChangeInt = percentChange.setScale(0, java.math.RoundingMode.FLOOR).toInt()
                val changeText =
                    when {
                        percentChangeInt > 0 ->
                            "Увеличение на $percentChangeInt%"
                        percentChangeInt < 0 ->
                            "Уменьшение на ${kotlin.math.abs(percentChangeInt)}%"
                        else -> "Без изменений"
                    }
                val percentChangeColor =
                    when {
                        percentChangeInt > 0 -> MaterialTheme.colorScheme.error // Красный для увеличения расходов
                        percentChangeInt < 0 -> MaterialTheme.colorScheme.primary // Зеленый для уменьшения расходов
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                Text(
                    text = changeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = percentChangeColor,
                )
            }
        }
    }
}
