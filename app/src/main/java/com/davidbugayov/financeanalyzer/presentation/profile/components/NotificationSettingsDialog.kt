package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Диалог настройки уведомлений о транзакциях.
 * @param isEnabled Включены ли уведомления.
 * @param reminderTime Время напоминания (часы и минуты).
 * @param onSave Обработчик сохранения настроек.
 * @param onDismiss Обработчик закрытия диалога.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    isEnabled: Boolean,
    reminderTime: Pair<Int, Int>?, // Часы и минуты
    onSave: (isEnabled: Boolean, reminderTime: Pair<Int, Int>?) -> Unit,
    onDismiss: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(isEnabled) }
    var selectedTime by remember { 
        mutableStateOf(reminderTime ?: Pair(20, 0)) // По умолчанию 20:00
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.first,
        initialMinute = selectedTime.second,
        is24Hour = true
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Настройка напоминаний",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Переключатель включения/выключения уведомлений
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Напоминания о транзакциях",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (notificationsEnabled) {
                    Text(
                        text = "Выберите время напоминания:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Выбор времени
                    TimeInput(
                        state = timePickerState,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Отображение выбранного времени
                    Text(
                        text = "Выбранное время: ${timePickerState.hour}:${String.format("%02d", timePickerState.minute)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Вы будете получать ежедневные напоминания о необходимости внести транзакции в указанное время.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Напоминания отключены. Включите их, чтобы не забывать вносить транзакции.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Кнопки действий
                Button(
                    onClick = {
                        val time = if (notificationsEnabled) {
                            Pair(timePickerState.hour, timePickerState.minute)
                        } else {
                            null
                        }
                        onSave(notificationsEnabled, time)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Сохранить")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Отмена")
                }
            }
        }
    }
} 