package com.davidbugayov.financeanalyzer

import android.app.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.davidbugayov.financeanalyzer.presentation.MainScreen
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme

class FinanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Обновляем виджеты при запуске приложения
        updateWidgets()
        
        // Делаем статус бар прозрачным и учитываем системные отступы
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            FinanceAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
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