package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Улучшенный компонент для выбора даты с кнопками быстрого выбора
 * 
 * @param date текущая выбранная дата
 * @param onClick обработчик нажатия для открытия диалога выбора даты
 * @param onTodayClick обработчик нажатия на кнопку "Сегодня"
 * @param onYesterdayClick обработчик нажатия на кнопку "Вчера"
 * @param modifier модификатор компонента
 */
@Composable
fun DateField(
    date: Date,
    onClick: () -> Unit,
    onTodayClick: () -> Unit = {},
    onYesterdayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Дата",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Основное поле ввода даты
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Выбрать дату",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Кнопки быстрого выбора даты
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            OutlinedButton(
                onClick = onTodayClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Сегодня")
            }
            
            OutlinedButton(
                onClick = onYesterdayClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Вчера")
            }
        }
    }
} 