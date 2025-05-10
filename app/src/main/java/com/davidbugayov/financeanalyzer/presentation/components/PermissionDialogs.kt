package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import timber.log.Timber
import androidx.compose.material3.MaterialTheme

@Composable
fun PermissionDialogs(
    show: Boolean,
    onDismiss: () -> Unit,
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
) {
    if (!show) return

    val context = LocalContext.current
    val permissionLauncher = PermissionUtils.rememberNotificationPermissionLauncher { isGranted ->
        if (isGranted) onPermissionGranted() else onPermissionDenied()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_required_title)) },
        text = { Text(stringResource(R.string.permission_required_text)) },
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(onClick = {
                PermissionUtils.handleNotificationPermission(
                    context = context,
                    launcher = permissionLauncher,
                    openSettingsOnDeny = true,
                    onGranted = onPermissionGranted,
                    onDenied = onPermissionDenied
                )
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