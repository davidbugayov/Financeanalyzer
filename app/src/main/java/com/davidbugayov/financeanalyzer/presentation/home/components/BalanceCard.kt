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
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Money
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
    // Определяем цвет карточки в зависимости от баланса
    val cardColor = if (balance.amount >= BigDecimal.ZERO) 
        Color(0xFFE0F7E0) // Светло-зеленый для положительного баланса
    else 
        Color(0xFFFFE0E0) // Светло-красный для отрицательного баланса
    
    // Получаем цвета из локального контекста для текста
    val balanceTextColor = if (balance.amount >= BigDecimal.ZERO) 
        Color(0xFF2E7D32) // Темно-зеленый текст для положительного баланса
    else 
        Color(0xFFB71C1C) // Темно-красный текст для отрицательного баланса
    
    // Цвет заголовка (немного светлее основного цвета текста)
    val titleColor = if (balance.amount >= BigDecimal.ZERO) 
        Color(0xFF388E3C) // Зеленый для положительного баланса
    else 
        Color(0xFFC62828) // Красный для отрицательного баланса
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
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
                text = balance.formatted(),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = balanceTextColor
            )
        }
    }
} 