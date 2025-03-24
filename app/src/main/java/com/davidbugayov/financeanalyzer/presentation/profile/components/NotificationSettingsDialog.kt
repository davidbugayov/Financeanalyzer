package com.davidbugayov.financeanalyzer.presentation.profile.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.utils.PermissionUtils

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
    val selectedTime by remember { 
        mutableStateOf(reminderTime ?: Pair(20, 0)) // По умолчанию 20:00
    }
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember {
        mutableStateOf(PermissionUtils.hasNotificationPermission(context))
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.first,
        initialMinute = selectedTime.second,
        is24Hour = true
    )

    // Launcher для запроса разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            hasNotificationPermission = true
            notificationsEnabled = true
        } else {
            // Если пользователь отказал, предлагаем перейти в настройки
            showSettingsDialog = true
        }
    }

    // Диалог с предложением перейти в настройки (когда пользователь отказал в разрешении)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSettingsDialog = false
                notificationsEnabled = false
            },
            title = { Text(stringResource(R.string.permission_denied)) },
            text = { Text(stringResource(R.string.notification_permission_settings_message)) },
            confirmButton = {
                Button(onClick = {
                    PermissionUtils.openNotificationSettings(context)
                    showSettingsDialog = false
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    notificationsEnabled = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Диалог запроса разрешений
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                notificationsEnabled = false
            },
            title = { Text(stringResource(R.string.permission_required)) },
            text = { Text(stringResource(R.string.notification_permission_message)) },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    // Запрашиваем разрешение напрямую
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text(stringResource(R.string.request_permission))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    notificationsEnabled = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_normal))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_normal))
            ) {
                Text(
                    text = stringResource(R.string.transaction_reminder_settings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxlarge)))
                
                // Переключатель включения/выключения уведомлений
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.transaction_reminders),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked && !hasNotificationPermission) {
                                // Если пользователь включает уведомления, но нет разрешения
                                showPermissionDialog = true
                            } else {
                                notificationsEnabled = isChecked
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))
                
                if (notificationsEnabled) {
                    if (hasNotificationPermission) {
                        // Если разрешения есть, показываем настройки времени
                        Text(
                            text = stringResource(R.string.select_reminder_time),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                        // Выбор времени
                        TimeInput(
                            state = timePickerState,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                        // Отображение выбранного времени
                        Text(
                            text = stringResource(
                                R.string.selected_time,
                                timePickerState.hour,
                                String.format("%02d", timePickerState.minute)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                        Text(
                            text = stringResource(R.string.reminder_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Если разрешений нет, показываем сообщение и кнопку для запроса разрешений
                        Text(
                            text = stringResource(R.string.notification_permission_required),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.request_permission))
                        }

                        // Проверяем состояние разрешения при активации экрана и при возвращении фокуса
                        LaunchedEffect(Unit) {
                            hasNotificationPermission =
                                PermissionUtils.hasNotificationPermission(context)
                        }
                    }
                } else {
                    // Текст-подсказка, если уведомления отключены
                    Text(
                        text = stringResource(R.string.notifications_disabled_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
                
                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val time = if (notificationsEnabled) {
                                Pair(timePickerState.hour, timePickerState.minute)
                            } else {
                                null
                            }
                            onSave(notificationsEnabled, time)
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
} 