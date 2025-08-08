package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Общий диалог для действий с транзакцией (удаление/редактирование)
 *
 * @param transaction Транзакция, с которой совершается действие
 * @param onDismiss Обработчик закрытия диалога
 * @param onDelete Обработчик удаления транзакции
 * @param onEdit Обработчик редактирования транзакции
 * @param subcategoryName Название подкатегории (если есть)
 */
@Composable
fun TransactionActionsDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    subcategoryName: String = "",
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_large)),
            shape = RoundedCornerShape(24.dp),
            colors =
                androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.dialog_padding)),
            ) {
                // Заголовок с крестиком
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = stringResource(R.string.transaction_actions),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontSize = dimensionResource(R.dimen.text_size_large).value.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_24dp)),
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(dimensionResource(R.dimen.finance_chart_screen_analytics_text_padding)),
                )

                // Контент диалога
                TransactionDetailContent(
                    transaction = transaction,
                    subcategoryName = subcategoryName,
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDelete(transaction) },
                        modifier =
                            Modifier.padding(
                                horizontal = dimensionResource(R.dimen.spacing_small),
                                vertical = dimensionResource(R.dimen.spacing_small),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_20dp)),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = stringResource(R.string.delete),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                                ),
                        )
                    }
                    TextButton(
                        onClick = { onEdit(transaction) },
                        modifier =
                            Modifier.padding(
                                horizontal = dimensionResource(R.dimen.spacing_small),
                                vertical = dimensionResource(R.dimen.spacing_small),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_20dp)),
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        Text(
                            text = stringResource(R.string.edit),
                            fontWeight = FontWeight.Medium,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = dimensionResource(R.dimen.text_size_medium).value.sp,
                                ),
                        )
                    }
                }
            }
        }
    }
}
