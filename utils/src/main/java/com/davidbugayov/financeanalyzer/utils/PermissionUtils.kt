package com.davidbugayov.financeanalyzer.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import timber.log.Timber

object PermissionUtils {
    private const val PREFS_NAME = "permission_prefs"
    private const val KEY_NOTIFICATION_REQUESTED = "notification_permission_requested"

    fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun openNotificationSettings(context: Context) {
        try {
            val intent =
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        }
    }

    fun hasReadExternalStoragePermission(context: Context): Boolean =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                ) == PackageManager.PERMISSION_GRANTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES,
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    fun getReadStoragePermission(): String =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                Manifest.permission.READ_MEDIA_IMAGES
            else -> Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun hasRequestedNotificationPermission(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_REQUESTED, false)
    }

    fun setRequestedNotificationPermission(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_NOTIFICATION_REQUESTED, true) }
    }

    fun handleNotificationPermission(
        context: Context,
        launcher: ActivityResultLauncher<String>,
        openSettingsOnDeny: Boolean,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        if (hasNotificationPermission(context)) {
            onGranted()
        } else {
            val hasRequestedBefore = hasRequestedNotificationPermission(context)
            if (shouldShowSettingsDialog(context) && hasRequestedBefore) {
                if (openSettingsOnDeny) {
                    openNotificationSettings(context)
                }
                onDenied()
            } else {
                setRequestedNotificationPermission(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    fun shouldShowSettingsDialog(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? Activity ?: return false
            val hasPermission = hasNotificationPermission(context)
            val shouldShowRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            Timber.d(
                "[PermissionUtils] shouldShowSettingsDialog: hasPermission=%b, rationale=%b, context=%s",
                hasPermission,
                shouldShowRationale,
                context::class.java.simpleName,
            )
            return !hasPermission && !shouldShowRationale
        }
        return false
    }
}
