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
     * Открывает системные настройки приложения для управления разрешениями.
     * Используется, когда пользователь отказал в предоставлении разрешения и нужно направить его
     * в настройки для ручного включения разрешения.
     * 
     * @param context Контекст приложения
     */
    fun openApplicationSettings(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
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
            // Для Android 15 (API 35) и выше используем READ_MEDIA_VISUAL_USER_SELECTED
            Build.VERSION.SDK_INT >= 35 -> { // Android 15
                try {
                    // Используем строковые константы для избежания ошибок компиляции на старых SDK
                    val permission = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                } catch (e: Exception) {
                    // Fallback к разрешениям Android 13+
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
            // Для Android 13 и выше используем READ_MEDIA_* разрешения
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
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
        return when {
            // Для Android 15 (API 35) и выше используем READ_MEDIA_VISUAL_USER_SELECTED
            Build.VERSION.SDK_INT >= 35 -> {
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED" // Используем строковую константу
            }
            // Для Android 13 (API 33) и выше используем READ_MEDIA_IMAGES
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Manifest.permission.READ_MEDIA_IMAGES
            }
            // Для более старых версий используем READ_EXTERNAL_STORAGE
            else -> {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }
    }
} 