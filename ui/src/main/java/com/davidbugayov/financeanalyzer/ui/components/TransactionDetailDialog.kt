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
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Диалог для отображения детальной информации о транзакции.
 * Показывает полную информацию о транзакции без возможности редактирования.
 *
 * @param transaction Транзакция для отображения
 * @param onDismiss Обработчик закрытия диалога
 * @param subcategoryName Название подкатегории (если есть)
 */
@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    subcategoryName: String = "",
    categoryIcon: ImageVector? = null,
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
                    .padding(dimensionResource(R.dimen.spacing_medium)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_xlarge)),
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                        Text(
                            text = stringResource(R.string.transaction_details),
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
                    categoryIcon = categoryIcon,
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                // Кнопка закрытия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier.padding(
                                horizontal = dimensionResource(R.dimen.card_horizontal_padding),
                                vertical = dimensionResource(R.dimen.card_vertical_padding),
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.close),
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
