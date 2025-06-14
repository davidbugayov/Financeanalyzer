package com.davidbugayov.financeanalyzer.presentation.components

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
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Общий диалог для действий с транзакцией (удаление/редактирование)
 *
 * @param transaction Транзакция, с которой совершается действие
 * @param onDismiss Обработчик закрытия диалога
 * @param onDelete Обработчик удаления транзакции
 * @param onEdit Обработчик редактирования транзакции
 */
@Composable
fun TransactionActionsDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Действия с транзакцией") },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column {
                Text(
                    text = "Сумма: ${transaction.amount}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Категория: ${transaction.category}",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (transaction.isExpense) {
                        "-${transaction.amount.abs()}"
                    } else {
                        "+${transaction.amount}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.isExpense) {
                        LocalExpenseColor.current
                    } else {
                        LocalIncomeColor.current
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                        transaction.date,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Определяем цвет источника один раз
                val effectiveSourceColor = rememberSourceColor(transaction, isDarkTheme)

                // Отображаем источник
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                effectiveSourceColor,
                                CircleShape,
                            ),
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Источник: ${transaction.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = effectiveSourceColor,
                    )
                }

                // Отображаем примечание, если оно есть
                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Примечание: $note",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete(transaction)
                    onDismiss()
                },
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEdit(transaction)
                    onDismiss()
                },
            ) {
                Text("Редактировать")
            }
        },
    )
}

@Composable
private fun rememberSourceColor(transaction: Transaction, isDarkTheme: Boolean): Color {
    return remember(transaction.source, transaction.sourceColor, transaction.isExpense, isDarkTheme) {
        val sourceColorInt = transaction.sourceColor
        var colorFromInt: Color? = null
        if (sourceColorInt != 0) { // 0 может быть маркером отсутствия цвета
            colorFromInt = try {
                Color(sourceColorInt)
            } catch (_: Exception) {
                null
            }
        }
        // Если цвет из sourceColorInt не подошел, или его не было, пробуем по имени источника.
        // sourceColorHex передаем null, так как мы уже обработали sourceColorInt.
        colorFromInt ?: ColorUtils.getEffectiveSourceColor(
            sourceName = transaction.source,
            sourceColorHex = null,
            isExpense = transaction.isExpense,
            isDarkTheme = isDarkTheme,
        )
    }
}
