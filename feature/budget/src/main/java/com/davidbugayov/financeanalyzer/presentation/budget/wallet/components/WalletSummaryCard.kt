package com.davidbugayov.financeanalyzer.presentation.budget.wallet.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * A friendly and informative summary card for a [Wallet].
 * Shows budget progress as a circular indicator with percentage, spent, remaining and limit values.
 * Also displays the current balance and a single action button to spend from the wallet.
 *
 * @param wallet   Wallet instance to display data for.
 * @param modifier Optional [Modifier].
 * @param onSpendClick Callback invoked when user presses the "Spend" button.
 */
@Composable
fun WalletSummaryCard(
    wallet: Wallet,
    modifier: Modifier = Modifier,
    onSpendClick: () -> Unit,
) {
    // Calculate progress (0f..n)
    val rawProgress = if (wallet.limit.amount > BigDecimal.ZERO) {
        wallet.spent.amount.divide(wallet.limit.amount, 4, RoundingMode.HALF_EVEN).toFloat()
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = min(rawProgress, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "wallet_progress",
    )
    val progressColor = if (rawProgress > 1f) Color.Red else MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Wallet name
            Text(
                text = wallet.name,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Budget progress circle
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(120.dp),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeWidth = 12.dp,
                    )
                    Text(
                        text = "${(rawProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = progressColor,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Spent / Remaining / Budget figures
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    AmountRow(
                        label = "Потрачено",
                        amount = wallet.spent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val remaining = wallet.limit - wallet.spent
                    AmountRow(
                        label = "Остаток",
                        amount = remaining,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        amountColor = if (remaining.isNegative()) Color.Red else MaterialTheme.colorScheme.onSurface,
                    )
                    AmountRow(
                        label = "Бюджет",
                        amount = wallet.limit,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current balance row
            AmountRow(
                label = "Баланс кошелька",
                amount = wallet.balance,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                amountColor = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = onSpendClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Потратить")
            }
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: Money,
    labelColor: Color,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor,
        )
        Text(
            text = amount.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
            style = MaterialTheme.typography.bodyLarge,
            color = amountColor,
        )
    }
} 