package com.davidbugayov.financeanalyzer

import android.app.Activity
import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.davidbugayov.financeanalyzer.presentation.MainScreen
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.CrashlyticsUtils
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.davidbugayov.financeanalyzer.widget.BalanceWidget
import com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget
import timber.log.Timber
import com.davidbugayov.financeanalyzer.utils.PreferencesManager

class FinanceActivity : ComponentActivity() {
    
    // Начальный экран для навигации
    private var startDestination = "home"
    
    // Флаг, указывающий, что приложение запущено через shortcut
    private var isLaunchedFromShortcut = false
    
    // Код запроса для разрешения на использование точных будильников
    private val REQUEST_EXACT_ALARM_PERMISSION = 1001
    
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
        
        // Делаем статус бар прозрачным
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Устанавливаем цвет иконок в статус-баре в зависимости от темы
        val isDarkTheme = when (PreferencesManager(this).getThemeMode()) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> resources.configuration.uiMode and 
                                 android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
                                 android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        // Применяем настройки иконок статус-бара
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // true = темные иконки (для светлого фона)
            // false = светлые иконки (для темного фона)
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
                    isLaunchedFromShortcut = true
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
                        data = android.net.Uri.parse("package:$packageName")
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