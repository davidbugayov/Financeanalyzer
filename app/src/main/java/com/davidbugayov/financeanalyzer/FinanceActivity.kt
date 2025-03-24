package com.davidbugayov.financeanalyzer

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.davidbugayov.financeanalyzer.presentation.MainScreen
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import com.davidbugayov.financeanalyzer.widget.BalanceWidget
import com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget
import timber.log.Timber

class FinanceActivity : ComponentActivity() {
    
    // Начальный экран для навигации
    private var startDestination = "home"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем сплеш-скрин перед вызовом super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Обрабатываем deep links
        handleIntent(intent)
        
        // Проверяем разрешение на использование точных будильников
        checkExactAlarmPermission()
        
        // Обновляем виджеты при запуске приложения
        updateWidgets()

        // Делаем контент приложения отображаться под системными панелями
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Устанавливаем прозрачные цвета для статус-бара и навигационной панели
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Для Android 11+ мы уже установили WindowCompat.setDecorFitsSystemWindows(window, false)
            // Явная установка цветов не требуется, система сделает их прозрачными
        } else {
            // На более старых версиях используем устаревший метод, но с подавлением предупреждения
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        // Определяем, какую тему использует приложение
        val isDarkTheme = when (PreferencesManager(this).getThemeMode()) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> resources.configuration.uiMode and 
                                 android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
                                 android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        // Устанавливаем цвет иконок в зависимости от темы: 
        // - светлые иконки на темном фоне (isDarkTheme = true)
        // - темные иконки на светлом фоне (isDarkTheme = false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
        
        // Настраиваем поведение сплеш-скрина
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }
        
        setContent {
            FinanceAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(startDestination = startDestination)
                    isReady = true
                }
            }
        }
    }
    
    /**
     * Обрабатывает входящий intent и определяет начальный экран
     */
    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            when (uri.toString()) {
                "financeanalyzer://add" -> {
                    startDestination = "add"
                }
            }
        }
    }
    
    /**
     * Обновляет все виджеты приложения, но только если они добавлены на домашний экран
     */
    private fun updateWidgets() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            
            // Обновляем основной виджет баланса
            val balanceWidgetComponent = ComponentName(this, BalanceWidget::class.java)
            val balanceWidgetIds = appWidgetManager.getAppWidgetIds(balanceWidgetComponent)
            if (balanceWidgetIds.isNotEmpty()) {
                val intent = Intent(this, BalanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, balanceWidgetIds)
                }
                sendBroadcast(intent)
            }
            
            // Обновляем маленький виджет баланса
            val smallBalanceWidgetComponent = ComponentName(this, SmallBalanceWidget::class.java)
            val smallBalanceWidgetIds = appWidgetManager.getAppWidgetIds(smallBalanceWidgetComponent)
            if (smallBalanceWidgetIds.isNotEmpty()) {
                val intent = Intent(this, SmallBalanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallBalanceWidgetIds)
                }
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating widgets")
        }
    }
    
    /**
     * Проверяет, есть ли у приложения разрешение на использование точных будильников,
     * и запрашивает его, если необходимо.
     */
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Timber.d("Requesting SCHEDULE_EXACT_ALARM permission")
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:$packageName".toUri()
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to request SCHEDULE_EXACT_ALARM permission")
                }
            } else {
                Timber.d("SCHEDULE_EXACT_ALARM permission already granted")
            }
        }
    }
}