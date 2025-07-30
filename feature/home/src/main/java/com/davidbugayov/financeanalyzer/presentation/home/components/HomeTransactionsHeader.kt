package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.presentation.home.model.TransactionFilter

/**
 * Компонент для отображения заголовка списка транзакций.
 *
 * @param currentFilter Текущий фильтр транзакций
 * @param showGroupSummary Флаг отображения сводки
 * @param onToggleGroupSummary Callback, вызываемый при переключении отображения сводки
 */
@Composable
fun HomeTransactionsHeader(
    currentFilter: TransactionFilter,
    showGroupSummary: Boolean,
    onToggleGroupSummary: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = headerTitleForFilter(currentFilter),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
        TextButton(
            onClick = { onToggleGroupSummary(!showGroupSummary) },
        ) {
            Text(
                text =
                    if (showGroupSummary) {
                        stringResource(R.string.hide_summary)
                    } else {
                        stringResource(R.string.show_summary)
                    },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun headerTitleForFilter(filter: TransactionFilter): String {
    return when (filter) {
        TransactionFilter.TODAY -> stringResource(R.string.transactions_today)
        TransactionFilter.WEEK -> stringResource(R.string.transactions_week)
        TransactionFilter.MONTH -> stringResource(R.string.transactions_month)
        TransactionFilter.ALL -> stringResource(R.string.transactions_all)
    }
}
