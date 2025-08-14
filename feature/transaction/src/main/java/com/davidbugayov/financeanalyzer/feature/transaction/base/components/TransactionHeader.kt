package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R as UiR
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

/**
 * Улучшенный заголовок с акцентом на дату и типе транзакции
 */
@Composable
fun transactionHeader(
    date: Date,
    isExpense: Boolean,
    incomeColor: Color,
    expenseColor: Color,
    onDateClick: () -> Unit,
    onToggleTransactionType: () -> Unit,
    forceExpense: Boolean,
    modifier: Modifier = Modifier,
) {
    // Добавляем лог для отслеживания состояния при каждом рендеринге
    Timber.d("TransactionHeader рендеринг: isExpense=$isExpense, forceExpense=$forceExpense")

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Кнопка выбора даты: строго используем локаль приложения из Resources
        val ctx = LocalContext.current
        val cfg = ctx.resources.configuration

        @Suppress("DEPRECATION")
        val appLocale: Locale = if (Build.VERSION.SDK_INT >= 24) cfg.locales[0] else cfg.locale
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", appLocale)
        val formattedDate = dateFormat.format(date)

        Surface(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDateClick),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Переключатели типа транзакции с улучшенным дизайном
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = !isExpense,
                onClick = {
                    if (isExpense) onToggleTransactionType()
                },
                label = {
                    Text(
                        stringResource(UiR.string.income_type),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (!isExpense) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = incomeColor.copy(alpha = 0.15f),
                        selectedLabelColor = incomeColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border =
                    FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = !isExpense,
                        borderColor =
                            if (!isExpense) {
                                incomeColor
                            } else {
                                MaterialTheme.colorScheme.outline.copy(
                                    alpha = 0.3f,
                                )
                            },
                        selectedBorderColor = incomeColor,
                        borderWidth = if (!isExpense) 2.dp else 1.dp,
                    ),
            )

            FilterChip(
                selected = isExpense,
                onClick = {
                    if (!isExpense) onToggleTransactionType()
                },
                label = {
                    Text(
                        stringResource(UiR.string.expense_type),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isExpense) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = expenseColor.copy(alpha = 0.15f),
                        selectedLabelColor = expenseColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border =
                    FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isExpense,
                        borderColor =
                            if (isExpense) {
                                expenseColor
                            } else {
                                MaterialTheme.colorScheme.outline.copy(
                                    alpha = 0.3f,
                                )
                            },
                        selectedBorderColor = expenseColor,
                        borderWidth = if (isExpense) 2.dp else 1.dp,
                    ),
            )
        }
    }
}
