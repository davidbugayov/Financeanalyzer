package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

/**
 * Компонент для отображения заголовка списка транзакций.
 *
 * @param currentFilter Текущий фильтр транзакций
 * @param showGroupSummary Флаг отображения сводки по группе
 * @param onShowGroupSummaryChange Callback, вызываемый при изменении флага отображения сводки
 * @param onShowAllClick Callback, вызываемый при нажатии на кнопку "Все"
 */
@Composable
fun HomeTransactionsHeader(
    currentFilter: TransactionFilter,
    showGroupSummary: Boolean,
    onShowGroupSummaryChange: (Boolean) -> Unit,
    onShowAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (currentFilter) {
                TransactionFilter.TODAY -> stringResource(R.string.transactions_today)
                TransactionFilter.WEEK -> stringResource(R.string.transactions_week)
                TransactionFilter.MONTH -> stringResource(R.string.transactions_month)
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Кнопка для управления видимостью сводки
            IconButton(
                onClick = { onShowGroupSummaryChange(!showGroupSummary) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (showGroupSummary) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = stringResource(R.string.show_summary),
                    modifier = Modifier.size(24.dp)
                )
            }

            TextButton(
                onClick = onShowAllClick,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.all),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 