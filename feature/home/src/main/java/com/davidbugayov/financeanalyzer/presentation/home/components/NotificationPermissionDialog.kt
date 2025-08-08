package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.feature.home.R
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.utils.PermissionManager
import com.davidbugayov.financeanalyzer.utils.PermissionUtils

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

    // ActivityResultLauncher для запроса разрешения на уведомления
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                permissionManager.processEvent(PermissionManager.PermissionEvent.GRANT_PERMISSION)
                onPermissionGranted()
            } else {
                // Если разрешение не предоставлено, проверяем, нужно ли показать диалог настроек
                if (PermissionUtils.shouldShowSettingsDialog(context)) {
                    // Здесь можно показать диалог для перехода в настройки
                    // Пока просто отмечаем как отклоненное
                    permissionManager.processEvent(PermissionManager.PermissionEvent.DENY_PERMISSION)
                    onPermissionDenied()
                } else {
                    permissionManager.processEvent(PermissionManager.PermissionEvent.DENY_PERMISSION)
                    onPermissionDenied()
                }
            }
            onDismiss()
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    Modifier
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
                    text = stringResource(UiR.string.notification_permission_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Описание
                Text(
                    text = stringResource(UiR.string.notification_permission_description),
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
                                PermissionManager.PermissionEvent.DISMISS_DIALOG,
                            )
                            onPermissionDenied()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(UiR.string.notification_permission_later))
                    }

                    // Кнопка "Разрешить"
                    Button(
                        onClick = {
                            permissionManager.processEvent(
                                PermissionManager.PermissionEvent.REQUEST_PERMISSION,
                            )
                            // Запрашиваем разрешение
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                // Для Android 12 и ниже разрешение считается предоставленным
                                permissionManager.processEvent(
                                    PermissionManager.PermissionEvent.GRANT_PERMISSION,
                                )
                                onPermissionGranted()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(UiR.string.notification_permission_allow))
                    }
                }
            }
        }
    }
}
