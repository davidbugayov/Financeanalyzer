package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

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
import com.davidbugayov.financeanalyzer.R
import timber.log.Timber

/**
 * Секция выбора кошельков для добавления дохода
 *
 * @param addToWallet Флаг добавления дохода в кошельки
 * @param selectedWallets Список выбранных кошельков
 * @param onToggleAddToWallet Обработчик переключения флага добавления в кошельки
 * @param onSelectWalletsClick Обработчик нажатия на кнопку выбора кошельков
 * @param isVisible Отображать ли секцию (только для доходов)
 * @param targetWalletName Название целевого кошелька (если есть)
 */
@Composable
fun WalletSelectionSection(
    addToWallet: Boolean,
    selectedWallets: List<String>,
    onToggleAddToWallet: () -> Unit,
    onSelectWalletsClick: () -> Unit,
    isVisible: Boolean,
    targetWalletName: String? = null
) {
    // Добавляем логирование состояния
    Timber.d("WalletSelectionSection: isVisible=$isVisible, addToWallet=$addToWallet, selectedWallets=$selectedWallets, targetWallet=$targetWalletName")
    
    if (isVisible) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_to_wallets),
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = addToWallet,
                    onCheckedChange = { onToggleAddToWallet() }
                )
            }
            
            if (addToWallet) {
                Spacer(modifier = Modifier.height(8.dp))
                
                SelectWalletsButton(
                    selectedWallets = selectedWallets,
                    onClick = onSelectWalletsClick,
                    targetWalletName = targetWalletName
                )
                
                // Добавляем явный вывод о том, сколько кошельков выбрано
                if (selectedWallets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedWallets.size == 1) {
                            stringResource(R.string.wallet_selected_singular)
                        } else {
                            stringResource(R.string.wallet_selected_plural, selectedWallets.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
    targetWalletName: String?
) {
    val text = if (selectedWallets.isEmpty()) {
        stringResource(R.string.select_wallets)
    } else if (selectedWallets.size == 1 && targetWalletName != null) {
        targetWalletName
    } else {
        stringResource(R.string.selected_wallets_count, selectedWallets.size)
    }
    
    WalletSelectorButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Кнопка выбора кошельков
 */
@Composable
private fun WalletSelectorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(R.string.select_wallets_content_description),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
} 