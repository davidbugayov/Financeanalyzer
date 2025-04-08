package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.utils.ColorUtils

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
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val moneyFormatter = transaction.amount
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_transaction)) },
        text = { 
            Column {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (transaction.isExpense) 
                        "-${moneyFormatter.abs().formatted(showCurrency = true)}" 
                    else 
                        "+${moneyFormatter.formatted(showCurrency = true)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.isExpense) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Дата: ${dateFormatter.format(transaction.date)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(ColorUtils.getEffectiveSourceColor(transaction.source, transaction.sourceColor, transaction.isExpense)), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Источник: ${transaction.source}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(ColorUtils.getEffectiveSourceColor(transaction.source, transaction.sourceColor, transaction.isExpense))
                    )
                }
                
                transaction.note?.let { note ->
                    if (note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Примечание: $note",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Вы уверены, что хотите удалить эту транзакцию?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
} 