package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.window.Dialog
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог выбора кошельков для добавления дохода
 *
 * @param wallets Список доступных кошельков
 * @param selectedWalletIds Список ID выбранных кошельков
 * @param onWalletSelected Обработчик выбора/отмены выбора кошелька
 * @param onConfirm Обработчик подтверждения выбора
 * @param onDismiss Обработчик закрытия диалога
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectorDialog(
    wallets: List<Wallet>,
    selectedWalletIds: List<String>,
    onWalletSelected: (String, Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            shape = RoundedCornerShape(dimensionResource(UiR.dimen.radius_xlarge)),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(UiR.dimen.spacing_large)),
            ) {
                Text(
                    text = stringResource(UiR.string.select_wallets),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(
                            bottom = dimensionResource(UiR.dimen.spacing_large),
                        ),
                )

                if (wallets.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    dimensionResource(UiR.dimen.height_empty_state),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(UiR.string.no_wallets_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    max = dimensionResource(UiR.dimen.height_dialog),
                                ),
                    ) {
                        items(wallets) { wallet ->
                            WalletItem(
                                wallet = wallet,
                                isSelected = selectedWalletIds.contains(wallet.id),
                                onCheckedChange = { isSelected ->
                                    onWalletSelected(wallet.id, isSelected)
                                },
                            )
                        }
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(UiR.dimen.spacing_large),
                            ),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(UiR.string.cancel))
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                dimensionResource(UiR.dimen.spacing_small),
                            ),
                    )

                    Button(
                        onClick = onConfirm,
                        enabled = selectedWalletIds.isNotEmpty(),
                    ) {
                        Text(stringResource(UiR.string.done))
                    }
                }
            }
        }
    }
}

/**
 * Элемент списка кошельков с чекбоксом
 */
@Composable
private fun WalletItem(
    wallet: Wallet,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
        ) {
            Text(
                text = wallet.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = stringResource(UiR.string.wallet_balance) + ": " + wallet.balance.formatForDisplay(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}