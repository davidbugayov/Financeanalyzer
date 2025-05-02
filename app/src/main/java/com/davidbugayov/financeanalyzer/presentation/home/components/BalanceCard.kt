package com.davidbugayov.financeanalyzer.presentation.home.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.ui.theme.LocalIncomeColor
import com.davidbugayov.financeanalyzer.ui.theme.LocalExpenseColor
import java.math.BigDecimal

/**
 * Компонент для отображения текущего баланса пользователя.
 *
 * @param balance Текущий баланс пользователя
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun BalanceCard(
    balance: Money,
    modifier: Modifier = Modifier
) {
    val incomeColor = LocalIncomeColor.current
    val expenseColor = LocalExpenseColor.current
    
    // Определяем цвет карточки в зависимости от баланса
    val cardColor = MaterialTheme.colorScheme.background
    
    // Получаем цвета из локального контекста для текста
    val balanceTextColor = if (balance.amount.signum() >= 0) 
        incomeColor // Зеленый текст для положительного баланса
    else 
        expenseColor // Красный текст для отрицательного баланса
    
    // Цвет заголовка (немного светлее основного цвета текста)
    val titleColor = if (balance.amount.signum() >= 0) 
        incomeColor.copy(alpha = 0.7f) // Зеленый для положительного баланса
    else 
        expenseColor.copy(alpha = 0.7f) // Красный для отрицательного баланса
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(
            width = 3.dp,
            color = balanceTextColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.current_balance),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = balance.format(true),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance.amount.signum() >= 0) incomeColor else expenseColor
            )
        }
    }
} 