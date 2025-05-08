package com.davidbugayov.financeanalyzer.presentation.components

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.utils.PermissionManager
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import timber.log.Timber

@Composable
fun PermissionDialogs(
    show: Boolean,
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    openSettingsOnDeny: Boolean = true
) {
    if (!show) return

    val context = LocalContext.current
    val permissionManager = remember(context) { PermissionManager(context) }
    var permissionCheckTrigger by remember { mutableStateOf(0) }

    val permissionLauncher = PermissionUtils.rememberNotificationPermissionLauncher { isGranted ->
        if (isGranted) {
            Timber.d("[PermissionDialogs] Permission granted via launcher")
            permissionManager.processEvent(PermissionManager.PermissionEvent.GRANT_PERMISSION)
            onPermissionGranted()
        } else {
            Timber.d("[PermissionDialogs] Permission denied via launcher")
            permissionManager.processEvent(PermissionManager.PermissionEvent.DENY_PERMISSION)
            if (openSettingsOnDeny) {
                Timber.d("[PermissionDialogs] openSettingsOnDeny=true, opening notification settings")
                PermissionUtils.openNotificationSettings(context)
                permissionManager.processEvent(PermissionManager.PermissionEvent.OPEN_SETTINGS)
            } else {
                Timber.d("[PermissionDialogs] openSettingsOnDeny=false, just closing dialog")
            }
            onPermissionDenied()
        }
        permissionCheckTrigger++
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_required_title)) },
        text = { Text(stringResource(R.string.permission_required_text)) },
        confirmButton = {
            Button(onClick = {
                if (permissionManager.shouldShowSettingsDialog()) {
                    if (openSettingsOnDeny) {
                        Timber.d("[PermissionDialogs] confirmButton: openSettingsOnDeny=true, opening notification settings")
                        PermissionUtils.openNotificationSettings(context)
                        permissionManager.processEvent(PermissionManager.PermissionEvent.OPEN_SETTINGS)
                    } else {
                        Timber.d("[PermissionDialogs] confirmButton: openSettingsOnDeny=false, just closing dialog")
                        onPermissionDenied()
                    }
                } else {
                    Timber.d("[PermissionDialogs] confirmButton: requesting permission via launcher")
                    permissionManager.processEvent(PermissionManager.PermissionEvent.REQUEST_PERMISSION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Timber.d("[PermissionDialogs] confirmButton: permission auto-granted (pre-TIRAMISU)")
                        onPermissionGranted()
                    }
                }
            }) {
                Text(stringResource(R.string.request_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                Timber.d("[PermissionDialogs] Dialog dismissed via dismissButton")
                onDismiss()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 