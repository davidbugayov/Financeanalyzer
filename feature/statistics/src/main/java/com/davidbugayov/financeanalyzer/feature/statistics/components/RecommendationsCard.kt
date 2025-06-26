package com.davidbugayov.financeanalyzer.feature.statistics.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.statistics.model.FinancialMetrics

@Composable
fun RecommendationsCard(
    metrics: FinancialMetrics,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Рекомендации",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Рекомендации на основе нормы сбережений
            if (metrics.savingsRate < 0.2) {
                Text(
                    text = "Попробуйте увеличить норму сбережений до 20% для лучшего финансового здоровья",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Отличная норма сбережений! Продолжайте в том же духе",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Рекомендации по категориям расходов
            metrics.topExpenseCategories.firstOrNull()?.let { topCategory ->
                if (topCategory.percentage > 0.4) {
                    Text(
                        text = "Категория '${topCategory.name}' занимает ${String.format(
                            "%.1f",
                            topCategory.percentage * 100,
                        )}% расходов. Рассмотрите возможность оптимизации",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
