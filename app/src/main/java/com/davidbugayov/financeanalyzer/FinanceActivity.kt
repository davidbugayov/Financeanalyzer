package com.davidbugayov.financeanalyzer

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.presentation.MainScreen
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.CrashlyticsUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.davidbugayov.financeanalyzer.widget.BalanceWidget
import com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget
import timber.log.Timber

class FinanceActivity : ComponentActivity() {
    
    // Начальный экран для навигации
    private var startDestination = "home"
    
    // Флаг, указывающий, что приложение запущено через shortcut
    private var isLaunchedFromShortcut = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Устанавливаем SplashScreen
        installSplashScreen()
        
        // Настраиваем отображение на весь экран
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Тестируем отправку крешей в Crashlytics
        testCrashlytics()
        
        // Настраиваем обработку нажатия кнопки "Назад"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Обработка нажатия кнопки "Назад"
                // Здесь можно добавить логику для подтверждения выхода
                finish()
            }
        })
        
        // Устанавливаем контент
        setContent {
            FinanceAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(startDestination = startDestination)
                }
            }
        }
        
        // Обновляем виджеты
        updateWidgets()
    }
    
    /**
     * Тестирует отправку крешей в Crashlytics
     */
    private fun testCrashlytics() {
        try {
            // Логируем сообщение
            Timber.d("Testing Crashlytics")
            
            // Отправляем тестовое исключение
            FirebaseCrashlytics.getInstance().log("Test log from FinanceActivity")
            FirebaseCrashlytics.getInstance().recordException(Exception("Test exception from FinanceActivity"))
            
            // Отправляем через утилитарный класс
            CrashlyticsUtils.log("Test log via CrashlyticsUtils")
            CrashlyticsUtils.recordException(Exception("Test exception via CrashlyticsUtils"))
            
            // Принудительно отправляем отчеты
            FirebaseCrashlytics.getInstance().sendUnsentReports()
            
            Timber.d("Crashlytics test completed")
        } catch (e: Exception) {
            Timber.e(e, "Failed to test Crashlytics")
        }
    }
    
    /**
     * Обновляет виджеты на главном экране
     */
    private fun updateWidgets() {
        try {
            // Обновляем виджет баланса
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
}