package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
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
    onEdit: (Transaction) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Действия с транзакцией") },
        text = { 
            Column {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Money(transaction.amount).formatted(showSign = true),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.isExpense) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(transaction.date),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Отображаем источник, если он есть
                transaction.source?.let { source ->
                    Text(
                        text = "Источник: $source",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Отображаем примечание, если оно есть
                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = "Примечание: $note",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { 
                    onEdit(transaction)
                    onDismiss()
                }
            ) {
                Text("Редактировать")
            }
        }
    )
} 