package com.davidbugayov.financeanalyzer

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
            
            // Проверяем наличие основного виджета баланса
            val balanceWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(this, com.davidbugayov.financeanalyzer.widget.BalanceWidget::class.java)
            )
            // Обновляем основной виджет только если он добавлен (есть хотя бы один экземпляр)
            if (balanceWidgetIds.isNotEmpty()) {
                com.davidbugayov.financeanalyzer.widget.BalanceWidget.updateAllWidgets(this)
            }
            
            // Проверяем наличие маленького виджета баланса
            val smallBalanceWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(this, com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget::class.java)
            )
            // Обновляем маленький виджет только если он добавлен (есть хотя бы один экземпляр)
            if (smallBalanceWidgetIds.isNotEmpty()) {
                com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget.updateAllWidgets(this)
            }
        } catch (e: Exception) {
            // Игнорируем ошибки при обновлении виджетов
        }
    }
}