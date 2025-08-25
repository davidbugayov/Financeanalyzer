package com.davidbugayov.financeanalyzer.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher

/**
 * Android-реализация обновления виджетов
 * @param context контекст приложения
 */
class AndroidWidgetRefresher(
    context: Context,
) : WidgetRefresher {
    private val appContext = context.applicationContext

    /**
     * Обновляет все активные виджеты
     */
    override fun refresh() {
        val manager = AppWidgetManager.getInstance(appContext)
        
        // Обновляем большой виджет баланса
        val balanceComp = ComponentName(appContext, BalanceWidget::class.java)
        val balanceIds = manager.getAppWidgetIds(balanceComp)
        if (balanceIds.isNotEmpty()) {
            val intent =
                Intent(appContext, BalanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, balanceIds)
                }
            appContext.sendBroadcast(intent)
        }
        
        // Обновляем маленький виджет баланса
        val smallComp = ComponentName(appContext, SmallBalanceWidget::class.java)
        val smallIds = manager.getAppWidgetIds(smallComp)
        if (smallIds.isNotEmpty()) {
            val intent =
                Intent(appContext, SmallBalanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallIds)
                }
            appContext.sendBroadcast(intent)
        }
    }
}
