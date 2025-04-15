package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Диалог выбора кошельков
 * 
 * ВАЖНО: Для работы диалога необходимы зависимости Jetpack Compose в build.gradle:
 * - implementation(libs.compose.ui)
 * - implementation(libs.compose.material3)
 * - implementation(libs.compose.foundation)
 * - implementation(libs.compose.runtime)
 * и стандартные библиотеки Kotlin должны быть добавлены
 */
@Composable
fun WalletSelectorDialog(
    wallets: List<String>,
    selectedWallets: List<String>,
    onWalletToggle: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите кошельки") },
        text = {
            Column {
                Text("Выберите кошельки, в которые вы хотите добавить средства:", 
                    modifier = Modifier.padding(bottom = 8.dp))
                
                LazyColumn {
                    items(wallets) { wallet ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedWallets.contains(wallet),
                                onCheckedChange = { onWalletToggle(wallet) }
                            )
                            
                            Text(
                                text = wallet,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
} 