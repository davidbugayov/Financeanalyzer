package com.davidbugayov.financeanalyzer.domain.usecase.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.davidbugayov.financeanalyzer.widget.BalanceWidget
import com.davidbugayov.financeanalyzer.widget.SmallBalanceWidget

/**
 * UseCase для обновления всех виджетов баланса приложения.
 */
class UpdateWidgetsUseCase {

    operator fun invoke(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Обновляем основной виджет баланса
        val balanceWidgetComponent = ComponentName(context, BalanceWidget::class.java)
        val balanceWidgetIds = appWidgetManager.getAppWidgetIds(balanceWidgetComponent)
        if (balanceWidgetIds.isNotEmpty()) {
            val intent = Intent(context, BalanceWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, balanceWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        // Обновляем маленький виджет баланса
        val smallBalanceWidgetComponent = ComponentName(context, SmallBalanceWidget::class.java)
        val smallBalanceWidgetIds = appWidgetManager.getAppWidgetIds(smallBalanceWidgetComponent)
        if (smallBalanceWidgetIds.isNotEmpty()) {
            val intent = Intent(context, SmallBalanceWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallBalanceWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
