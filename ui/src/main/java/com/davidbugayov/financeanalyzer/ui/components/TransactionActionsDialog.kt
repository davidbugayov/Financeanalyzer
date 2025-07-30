package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.R

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.transaction_actions),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        text = {
            TransactionDetailContent(transaction = transaction)
        },
        confirmButton = {
            TextButton(
                onClick = { onEdit(transaction) },
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.edit),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDelete(transaction) },
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.delete),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
    )
}
