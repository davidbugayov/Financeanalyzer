package com.davidbugayov.financeanalyzer.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Сервис для обновления виджета баланса.
 * Может быть запущен из приложения для принудительного обновления виджета.
 */
class BalanceWidgetService : Service() {

    private val loadTransactionsUseCase: LoadTransactionsUseCase by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Обновляем виджет
        updateWidget()
        
        // Останавливаем сервис после обновления
        stopSelf()
        
        return START_NOT_STICKY
    }

    /**
     * Обновляет все экземпляры виджета баланса
     */
    private fun updateWidget() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получаем AppWidgetManager
                val appWidgetManager = AppWidgetManager.getInstance(this@BalanceWidgetService)
                
                // Получаем все ID виджетов
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(this@BalanceWidgetService, BalanceWidget::class.java)
                )
                
                // Отправляем широковещательное сообщение для обновления виджетов
                val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                    component = ComponentName(this@BalanceWidgetService, BalanceWidget::class.java)
                }
                sendBroadcast(updateIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 