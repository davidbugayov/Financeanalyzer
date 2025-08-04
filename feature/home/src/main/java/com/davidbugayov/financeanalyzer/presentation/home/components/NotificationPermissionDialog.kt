package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.utils.PermissionManager
import com.davidbugayov.financeanalyzer.feature.home.R

/**
 * Диалог для запроса разрешения на уведомления после завершения онбординга.
 * Показывается пользователю после первого входа в приложение.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Иконка уведомлений
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Заголовок
                Text(
                    text = stringResource(R.string.notification_permission_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Описание
                Text(
                    text = stringResource(R.string.notification_permission_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Кнопка "Позже"
                    OutlinedButton(
                        onClick = {
                            permissionManager.processEvent(
                                PermissionManager.PermissionEvent.DISMISS_DIALOG
                            )
                            onPermissionDenied()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.notification_permission_later))
                    }
                    
                    // Кнопка "Разрешить"
                    Button(
                        onClick = {
                            permissionManager.processEvent(
                                PermissionManager.PermissionEvent.REQUEST_PERMISSION
                            )
                            // Запрашиваем разрешение
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                // Здесь должен быть ActivityResultLauncher, но пока просто отмечаем
                                permissionManager.processEvent(
                                    PermissionManager.PermissionEvent.GRANT_PERMISSION
                                )
                                onPermissionGranted()
                            } else {
                                onPermissionGranted()
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.notification_permission_allow))
                    }
                }
            }
        }
    }
} 