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
import com.davidbugayov.financeanalyzer.widget.BalanceWidget
import com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget
import timber.log.Timber

class FinanceActivity : ComponentActivity() {
    
    // Начальный экран для навигации
    private var startDestination = "home"
    
    // Флаг, указывающий, что приложение запущено через shortcut
    private var isLaunchedFromShortcut = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем сплеш-скрин перед вызовом super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Обрабатываем deep links
        handleIntent(intent)
        
        // Обновляем виджеты при запуске приложения
        updateWidgets()
        
        // Делаем статус бар прозрачным и учитываем системные отступы
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
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
}