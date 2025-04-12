package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R

/**
 * Секция выбора кошельков для добавления дохода
 *
 * @param addToWallet Флаг добавления дохода в кошельки
 * @param selectedWallets Список выбранных кошельков
 * @param onToggleAddToWallet Обработчик переключения флага добавления в кошельки
 * @param onSelectWalletsClick Обработчик нажатия на кнопку выбора кошельков
 * @param isVisible Отображать ли секцию (только для доходов)
 */
@Composable
fun WalletSelectionSection(
    addToWallet: Boolean,
    selectedWallets: List<String>,
    onToggleAddToWallet: () -> Unit,
    onSelectWalletsClick: () -> Unit,
    isVisible: Boolean
) {
    if (!isVisible) return
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.spacing_normal))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onToggleAddToWallet() }
        ) {
            Checkbox(
                checked = addToWallet,
                onCheckedChange = { onToggleAddToWallet() }
            )
            
            Text(
                text = "Добавить в кошельки",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (addToWallet) {
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectWalletsClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedWallets.isEmpty()) "Выбрать кошельки" 
                           else "Выбрано: ${selectedWallets.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Выбрать кошельки",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
} 