package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import com.davidbugayov.financeanalyzer.ui.utils.ColorUtils
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay

/**
 * Диалог подтверждения удаления транзакции.
 * Отображает подробную информацию о транзакции перед удалением.
 *
 * @param transaction Транзакция, которую нужно удалить
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 */
@Composable
fun DeleteTransactionDialog(transaction: Transaction, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val moneyFormatter = transaction.amount

    val isDarkTheme = isSystemInDarkTheme()
    val effectiveSourceColor = remember(
        transaction.source,
        transaction.sourceColor,
        transaction.isExpense,
        isDarkTheme,
    ) {
        val sourceColorInt = transaction.sourceColor
        val colorFromInt: Color? = if (sourceColorInt != 0) Color(sourceColorInt) else null

        colorFromInt ?: ColorUtils.getEffectiveSourceColor(
            sourceName = transaction.source,
            sourceColorHex = null,
            isExpense = transaction.isExpense,
            isDarkTheme = isDarkTheme,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_transaction)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column {
                Text(
                    text = stringResource(R.string.amount, transaction.amount.formatForDisplay(useMinimalDecimals = true)) + "\n" +
                        stringResource(R.string.category, transaction.category),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = (if (transaction.isExpense) "-" else "+") + moneyFormatter.abs().formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.isExpense) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.date, dateFormatter.format(transaction.date)),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(effectiveSourceColor, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(R.string.source, transaction.source),
                        style = MaterialTheme.typography.bodyMedium,
                        color = effectiveSourceColor,
                    )
                }

                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.note, note),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.delete_transaction_confirm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
