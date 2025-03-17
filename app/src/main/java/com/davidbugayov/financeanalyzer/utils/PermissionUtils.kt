package com.davidbugayov.financeanalyzer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/**
 * Утилитарный класс для работы с разрешениями.
 */
object PermissionUtils {

    /**
     * Проверяет, есть ли у приложения разрешение на запись во внешнее хранилище.
     * @param context Контекст приложения
     * @return true, если разрешение есть, иначе false
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Начиная с Android 10 (API 29), разрешение не требуется для записи в приватную директорию приложения
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Composable функция для запроса разрешения на запись во внешнее хранилище.
     * @param onPermissionGranted Callback, вызываемый при предоставлении разрешения
     * @param onPermissionDenied Callback, вызываемый при отказе в разрешении
     */
    @Composable
    fun RequestStoragePermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        var hasPermission by remember { mutableStateOf(false) }
        val context = androidx.compose.ui.platform.LocalContext.current
        
        // Проверяем, есть ли уже разрешение
        LaunchedEffect(Unit) {
            hasPermission = hasStoragePermission(context)
            if (hasPermission) {
                onPermissionGranted()
            }
        }
        
        // Если разрешение уже есть, ничего не делаем
        if (hasPermission || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return
        }
        
        // Создаем launcher для запроса разрешения
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                hasPermission = true
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
        
        // Запрашиваем разрешение
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
} 