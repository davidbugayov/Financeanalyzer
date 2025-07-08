package com.davidbugayov.financeanalyzer.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.WidgetRefresher

class AndroidWidgetRefresher(private val context: Context) : WidgetRefresher {
    override fun refresh() {
        val manager = AppWidgetManager.getInstance(context)
        val balanceComp = ComponentName(context, BalanceWidget::class.java)
        val balanceIds = manager.getAppWidgetIds(balanceComp)
        if (balanceIds.isNotEmpty()) {
            val intent =
                Intent(context, BalanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, balanceIds)
                }
            context.sendBroadcast(intent)
        }
        val smallComp = ComponentName(context, SmallBalanceWidget::class.java)
        val smallIds = manager.getAppWidgetIds(smallComp)
        if (smallIds.isNotEmpty()) {
            val intent =
                Intent(context, SmallBalanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallIds)
                }
            context.sendBroadcast(intent)
        }
    }
}
