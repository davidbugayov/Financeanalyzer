package com.davidbugayov.financeanalyzer.presentation.chart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Money

/**
 * Элемент списка категорий в стиле CoinKeeper
 */
@Composable
fun CategoryListItem(
    categoryName: String,
    amount: Money,
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Цветной индикатор категории
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )

        // Название категории
        Text(
            text = categoryName,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        )

        // Сумма
        Text(
            text = amount.format(false),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Процент
        Text(
            text = String.format("%.1f%%", percentage),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
} 