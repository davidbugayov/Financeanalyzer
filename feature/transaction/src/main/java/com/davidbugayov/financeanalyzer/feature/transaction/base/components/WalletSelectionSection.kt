package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R as UiR
import timber.log.Timber

/**
 * Секция выбора кошельков для транзакций
 *
 * @param addToWallet Флаг добавления/списания в/из кошельков
 * @param selectedWallets Список выбранных кошельков
 * @param onToggleAddToWallet Обработчик переключения флага добавления в кошельки
 * @param onSelectWalletsClick Обработчик нажатия на кнопку выбора кошельков
 * @param isVisible Отображать ли секцию (показываем если есть кошельки)
 * @param isExpense Является ли транзакция расходом
 * @param targetWalletName Название целевого кошелька (если есть)
 */
@Composable
fun WalletSelectionSection(
    addToWallet: Boolean,
    selectedWallets: List<String>,
    onToggleAddToWallet: () -> Unit,
    onSelectWalletsClick: () -> Unit,
    isVisible: Boolean,
    isExpense: Boolean = false,
    targetWalletName: String? = null,
) {
    // Добавляем логирование состояния
    Timber.d(
        "WalletSelectionSection: isVisible=$isVisible, isExpense=$isExpense, addToWallet=$addToWallet, selectedWallets=$selectedWallets, targetWallet=$targetWalletName",
    )

    if (isVisible) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        if (isExpense) {
                            stringResource(UiR.string.deduct_from_wallets)
                        } else {
                            stringResource(UiR.string.add_to_wallets)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                Switch(
                    checked = addToWallet,
                    onCheckedChange = { onToggleAddToWallet() },
                )
            }

            if (addToWallet) {
                Spacer(modifier = Modifier.height(8.dp))

                SelectWalletsButton(
                    selectedWallets = selectedWallets,
                    onClick = onSelectWalletsClick,
                    targetWalletName = targetWalletName,
                    isExpense = isExpense,
                )

                // Показываем количество выбранных кошельков с правильным текстом
                if (selectedWallets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val statusText =
                        if (isExpense) {
                            // Для расходов обычно выбирается один кошелёк
                            if (selectedWallets.size == 1) {
                                stringResource(UiR.string.wallet_selected_for_expense)
                            } else {
                                stringResource(UiR.string.wallets_selected_for_expense, selectedWallets.size)
                            }
                        } else {
                            // Для доходов можно выбрать несколько кошельков
                            if (selectedWallets.size == 1) {
                                stringResource(UiR.string.wallet_selected_for_income)
                            } else {
                                stringResource(UiR.string.wallets_selected_for_income, selectedWallets.size)
                            }
                        }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Добавляем пояснительный текст
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        if (isExpense) {
                            stringResource(UiR.string.wallet_expense_explanation)
                        } else {
                            stringResource(UiR.string.wallet_income_explanation)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Кнопка выбора кошельков
 */
@Composable
private fun SelectWalletsButton(
    selectedWallets: List<String>,
    onClick: () -> Unit,
    targetWalletName: String?,
    isExpense: Boolean,
) {
    val text =
        when {
            selectedWallets.isEmpty() -> {
                if (isExpense) {
                    stringResource(UiR.string.select_wallet_for_expense)
                } else {
                    stringResource(UiR.string.select_wallets_for_income)
                }
            }
            selectedWallets.size == 1 && targetWalletName != null -> {
                targetWalletName
            }
            isExpense && selectedWallets.size == 1 -> {
                // Для расходов показываем название единственного выбранного кошелька
                targetWalletName ?: stringResource(UiR.string.selected_wallet_count, selectedWallets.size)
            }
            else -> {
                stringResource(UiR.string.selected_wallets_count, selectedWallets.size)
            }
        }

    WalletSelectorButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        isExpense = isExpense,
    )
}

/**
 * Кнопка выбора кошельков
 */
@Composable
private fun WalletSelectorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    isExpense: Boolean,
) {
    val containerColor =
        if (isExpense) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        }

    val contentColor =
        if (isExpense) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

    val borderColor =
        if (isExpense) {
            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
        }

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(UiR.string.select_wallets_content_description),
            tint = contentColor,
        )
    }
}
