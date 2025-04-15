package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Улучшенный компонент заголовка транзакции с визуальными акцентами
 * 
 * @param date текущая выбранная дата
 * @param isExpense флаг расхода (true) или дохода (false)
 * @param incomeColor цвет для дохода
 * @param expenseColor цвет для расхода
 * @param onDateClick обработчик нажатия для выбора даты
 * @param onToggleTransactionType обработчик переключения типа транзакции
 * @param forceExpense флаг принудительного режима расхода
 * @param modifier модификатор компонента
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionHeader(
    date: Date,
    isExpense: Boolean,
    incomeColor: Color,
    expenseColor: Color,
    onDateClick: () -> Unit,
    onToggleTransactionType: () -> Unit,
    forceExpense: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
    
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Дата транзакции (более заметная)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onDateClick)
                )
            }
            
            // Переключатель типа транзакции
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    selected = !isExpense,
                    onClick = { if (isExpense && !forceExpense) onToggleTransactionType() },
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = incomeColor,
                        activeContentColor = Color.White,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Доход", fontWeight = FontWeight.Bold)
                }
                
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    selected = isExpense,
                    onClick = { if (!isExpense) onToggleTransactionType() },
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = expenseColor,
                        activeContentColor = Color.White,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Расход", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
} 