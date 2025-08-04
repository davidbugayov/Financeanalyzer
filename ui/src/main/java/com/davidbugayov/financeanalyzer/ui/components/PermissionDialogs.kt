package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

object PermissionDialogs {
    @Composable
    fun SettingsPermissionDialog(
        titleResId: Int = R.string.permission_required,
        messageResId: Int = R.string.permission_settings_rationale,
        confirmButtonTextResId: Int = R.string.open_settings,
        dismissButtonTextResId: Int = R.string.cancel,
        onOpenSettings: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(titleResId), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(messageResId)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = onOpenSettings) { Text(stringResource(confirmButtonTextResId)) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(dismissButtonTextResId)) } },
        )
    }

    @Composable
    fun RationalePermissionDialog(
        titleResId: Int = R.string.permission_required,
        messageResId: Int = R.string.permission_rationale,
        confirmButtonTextResId: Int = R.string.grant_permission,
        dismissButtonTextResId: Int = R.string.cancel,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(titleResId), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(messageResId)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = onConfirm) { Text(stringResource(confirmButtonTextResId)) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(dismissButtonTextResId)) } },
        )
    }
}
