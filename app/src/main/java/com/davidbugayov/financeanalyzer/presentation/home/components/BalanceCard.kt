package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.extensions.formatForDisplay
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor

/**
 * Компонент для отображения текущего баланса пользователя.
 *
 * @param balance Текущий баланс пользователя
 */
@Composable
private fun BalanceCardTitle(balance: Money) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val titleColor = if (balance.amount.signum() >= 0) {
        incomeColor.copy(alpha = 0.7f)
    } else {
        expenseColor.copy(alpha = 0.7f)
    }
    Text(
        text = stringResource(R.string.current_balance),
        style = MaterialTheme.typography.titleMedium,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = titleColor,
    )
}

@Composable
private fun BalanceCardAmount(balance: Money) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    Text(
        text = balance.formatForDisplay(showCurrency = true, useMinimalDecimals = true),
        style = MaterialTheme.typography.headlineMedium,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = if (balance.amount.signum() >= 0) incomeColor else expenseColor,
    )
}

@Composable
fun BalanceCard(balance: Money, modifier: Modifier = Modifier) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    val cardColor = MaterialTheme.colorScheme.background
    val balanceTextColor = if (balance.amount.signum() >= 0) incomeColor else expenseColor
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(width = 3.dp, color = balanceTextColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BalanceCardTitle(balance)
            Spacer(modifier = Modifier.height(12.dp))
            BalanceCardAmount(balance)
        }
    }
}
