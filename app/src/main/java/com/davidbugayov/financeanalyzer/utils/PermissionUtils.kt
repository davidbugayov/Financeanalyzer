package com.davidbugayov.financeanalyzer.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * Утилитарный класс для работы с разрешениями.
 */
object PermissionUtils {

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
                Manifest.permission.POST_NOTIFICATIONS
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
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) == null) {
            // Fallback для старых версий или если настройки уведомлений недоступны
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        } else {
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
            // Для Android 13 и выше используем READ_MEDIA_* разрешения
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
            }
            // Для Android 10-12 используем READ_EXTERNAL_STORAGE
            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
} 