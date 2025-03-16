package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Заголовок с датой и типом транзакции
 */
@Composable
fun TransactionHeader(
    date: Date,
    isExpense: Boolean,
    incomeColor: Color,
    expenseColor: Color,
    onDateClick: () -> Unit,
    onToggleTransactionType: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dateFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
        val formattedDate = dateFormat.format(date)

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onDateClick)
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isExpense,
                onClick = {
                    if (isExpense) onToggleTransactionType()
                },
                label = { Text(stringResource(R.string.income_type)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = incomeColor.copy(alpha = 0.2f),
                    selectedLabelColor = incomeColor
                )
            )

            FilterChip(
                selected = isExpense,
                onClick = {
                    if (!isExpense) onToggleTransactionType()
                },
                label = { Text(stringResource(R.string.expense_type)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = expenseColor.copy(alpha = 0.2f),
                    selectedLabelColor = expenseColor
                )
            )
        }
    }
} 