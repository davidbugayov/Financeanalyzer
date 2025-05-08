package com.davidbugayov.financeanalyzer.presentation.profile.components

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.PermissionDialogs
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.utils.PermissionManager
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import timber.log.Timber

/**
 * Диалог настройки уведомлений о транзакциях.
 * @param onDismiss Обработчик закрытия диалога.
 * @param viewModel ViewModel для управления состоянием настроек.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    var pendingEnableNotifications by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(PermissionUtils.hasNotificationPermission(context)) }

    // Если разрешение только что появилось и был запрос на включение — включаем напоминания
    LaunchedEffect(hasNotificationPermission) {
        if (hasNotificationPermission && pendingEnableNotifications) {
            viewModel.onEvent(ProfileEvent.ChangeNotifications(true), context)
            pendingEnableNotifications = false
        }
    }

    LaunchedEffect(Unit) {
        hasNotificationPermission = PermissionUtils.hasNotificationPermission(context)
    }

    LaunchedEffect(state.isTransactionReminderEnabled) {
        Timber.d("[UI] NotificationSettingsDialog: isTransactionReminderEnabled=${state.isTransactionReminderEnabled}")
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = state.transactionReminderTime?.first ?: 20,
            initialMinute = state.transactionReminderTime?.second ?: 0,
            onTimeSelected = { hour, minute ->
                viewModel.updateReminderTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    PermissionDialogs(
        show = showPermissionDialog,
        onDismiss = { showPermissionDialog = false },
        onPermissionGranted = {
            showPermissionDialog = false
            hasNotificationPermission = PermissionUtils.hasNotificationPermission(context)
            viewModel.onEvent(ProfileEvent.ChangeNotifications(true), context)
        },
        onPermissionDenied = { showPermissionDialog = false }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.notification_settings),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Text(
                        text = stringResource(R.string.notification_permission_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = {
                            permissionManager.processEvent(PermissionManager.PermissionEvent.REQUEST_PERMISSION)
                            showPermissionDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.request_permission))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enable_reminders),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.reminder_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isTransactionReminderEnabled,
                        onCheckedChange = { checked ->
                            if (checked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionManager.processEvent(PermissionManager.PermissionEvent.REQUEST_PERMISSION)
                                showPermissionDialog = true
                                pendingEnableNotifications = true
                            } else {
                                viewModel.onEvent(ProfileEvent.ChangeNotifications(checked), context)
                                pendingEnableNotifications = false
                            }
                        }
                    )
                }

                if (state.isTransactionReminderEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.reminder_time),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(
                                    R.string.reminder_time_description,
                                    String.format("%02d:%02d", state.transactionReminderTime?.first ?: 20, state.transactionReminderTime?.second ?: 0)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_time)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
} 