package com.davidbugayov.financeanalyzer.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import timber.log.Timber

/**
 * Утилитарный класс для работы с разрешениями.
 */
object PermissionUtils {

    private const val PREFS_NAME = "permission_prefs"
    private const val KEY_NOTIFICATION_REQUESTED = "notification_permission_requested"

    /**
     * Проверяет, есть ли у приложения разрешение на отправку уведомлений.
     * @param context Контекст приложения
     * @return true, если разрешение есть, иначе false
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Начиная с Android 13 (API 33), требуется специальное разрешение для уведомлений
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // До Android 13 отдельное разрешение на уведомления не требуется
            true
        }
    }


    /**
     * Открывает системные настройки уведомлений для приложения.
     * @param context Контекст приложения
     */
    fun openNotificationSettings(context: Context) {
        try {
            // Попытка открыть настройки уведомлений (Android 8+)
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback: открыть настройки приложения
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Проверяет наличие разрешения на чтение внешнего хранилища.
     * Учитывает различия в API уровнях Android.
     *
     * @param context Контекст приложения
     * @return true, если разрешение предоставлено
     */
    fun hasReadExternalStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+ (API 34)
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                ) == PackageManager.PERMISSION_GRANTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13 (API 33)
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES,
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                // Android 12 и ниже
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Возвращает необходимое разрешение для чтения файлов в зависимости от версии Android.
     *
     * @return Строка с необходимым разрешением
     */
    fun getReadStoragePermission(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+ (API 34)
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13 (API 33)
                Manifest.permission.READ_MEDIA_IMAGES
            }
            else -> {
                // Android 12 и ниже
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }
    }

    /**
     * Проверяет, запрашивалось ли уже разрешение на уведомления
     */
    fun hasRequestedNotificationPermission(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_REQUESTED, false)
    }

    /**
     * Сохраняет факт запроса разрешения на уведомления
     */
    fun setRequestedNotificationPermission(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_NOTIFICATION_REQUESTED, true) }
    }

    /**
     * Проверяет и запрашивает разрешение на уведомления, если нужно открывает настройки.
     * @param context Контекст
     * @param launcher Лаунчер для запроса разрешения
     * @param openSettingsOnDeny Открывать ли настройки при отказе
     * @param onGranted Callback при успехе
     * @param onDenied Callback при отказе
     */
    @Suppress("InlinedApi")
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
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Определяет, нужно ли показывать диалог перехода в настройки (например, если пользователь навсегда запретил разрешение).
     * @param context Контекст
     * @return true, если нужно открыть настройки
     */
    fun shouldShowSettingsDialog(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? Activity ?: return false
            val hasPermission = hasNotificationPermission(context)
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS,
            )
            Timber.d(
                "[PermissionUtils] shouldShowSettingsDialog: hasPermission=%b, shouldShowRationale=%b, context=%s",
                hasPermission,
                shouldShowRationale,
                context::class.java.simpleName,
            )
            return !hasPermission && !shouldShowRationale
        }
        return false
    }
}
