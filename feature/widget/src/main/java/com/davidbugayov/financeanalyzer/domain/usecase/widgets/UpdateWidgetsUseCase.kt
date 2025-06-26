package com.davidbugayov.financeanalyzer.domain.usecase.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.davidbugayov.financeanalyzer.widget.BalanceWidget
import com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget

class UpdateWidgetsUseCase {
    operator fun invoke(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val balanceComp = ComponentName(context, BalanceWidget::class.java)
        val balanceIds = manager.getAppWidgetIds(balanceComp)
        if (balanceIds.isNotEmpty()) {
            val intent = Intent(context, BalanceWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, balanceIds)
            }
            context.sendBroadcast(intent)
        }
        val smallComp = ComponentName(context, SmallBalanceWidget::class.java)
        val smallIds = manager.getAppWidgetIds(smallComp)
        if (smallIds.isNotEmpty()) {
            val intent = Intent(context, SmallBalanceWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
