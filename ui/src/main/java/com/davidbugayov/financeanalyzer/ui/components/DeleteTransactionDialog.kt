package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.R
import com.davidbugayov.financeanalyzer.ui.utils.ColorUtils
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Диалог подтверждения удаления транзакции.
 * Отображает подробную информацию о транзакции перед удалением.
 *
 * @param transaction Транзакция, которую нужно удалить
 * @param onConfirm Callback, вызываемый при подтверждении удаления
 * @param onDismiss Callback, вызываемый при отмене удаления
 */
@Composable
fun DeleteTransactionDialog(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val moneyFormatter = transaction.amount

    val isDarkTheme = isSystemInDarkTheme()
    val effectiveSourceColor =
        remember(
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Заголовок с крестиком
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.delete_transaction),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Контент диалога
                Column {
                    Text(
                        text =
                            stringResource(R.string.amount, transaction.amount.formatForDisplay(useMinimalDecimals = true)) + "\n" +
                                stringResource(R.string.category, transaction.category),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = (if (transaction.isExpense) "-" else "+") + moneyFormatter.abs().formatForDisplay(showCurrency = true, useMinimalDecimals = true),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.date, dateFormatter.format(transaction.date)),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(16.dp)
                                    .background(effectiveSourceColor, CircleShape),
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.source, transaction.source),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = effectiveSourceColor,
                        )
                    }

                    transaction.note?.let { note ->
                        if (note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.note, note),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.delete_transaction_confirm),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(
                        onClick = onConfirm,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
