package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = transaction.amount.formatted(showSign = true),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.isExpense) 
                        Color(ColorUtils.EXPENSE_COLOR)
                    else 
                        Color(ColorUtils.INCOME_COLOR)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(transaction.date),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Отображаем источник
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Добавляем цветной индикатор источника
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(ColorUtils.getEffectiveSourceColor(transaction.source, transaction.sourceColor, transaction.isExpense)), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Источник: ${transaction.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(ColorUtils.getEffectiveSourceColor(transaction.source, transaction.sourceColor, transaction.isExpense))
                    )
                }
                
                // Отображаем примечание, если оно есть
                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
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