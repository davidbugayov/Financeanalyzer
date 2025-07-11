package com.davidbugayov.financeanalyzer.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher

class AndroidWidgetRefresher(context: Context) : WidgetRefresher {
    private val appContext = context.applicationContext

    override fun refresh() {
        val manager = AppWidgetManager.getInstance(appContext)
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
