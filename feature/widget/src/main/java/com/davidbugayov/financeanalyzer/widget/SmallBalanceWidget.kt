package com.davidbugayov.financeanalyzer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.fold
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.LoadTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Компактный виджет для отображения текущего баланса.
 */
class SmallBalanceWidget : AppWidgetProvider(), KoinComponent {
    private val loadTransactionsUseCase: LoadTransactionsUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val views = RemoteViews(context.packageName, R.layout.small_balance_widget_layout)

        val launchAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchAppIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    1,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            views.setOnClickPendingIntent(R.id.small_widget_container, pendingIntent)
        }

        scope.launch {
            loadTransactionsUseCase().fold(
                onSuccess = { transactions: List<Transaction> ->
                    val currency = if (transactions.isNotEmpty()) transactions.first().amount.currency else Money.zero().currency
                    val income =
                        transactions.filter { !it.isExpense }.fold(
                            Money.zero(currency),
                        ) { acc, t -> acc + t.amount }
                    val expense =
                        transactions.filter { it.isExpense }.fold(
                            Money.zero(currency),
                        ) { acc, t -> acc + t.amount }
                    val firstExpense = transactions.firstOrNull { it.isExpense }
                    val balance =
                        if (firstExpense != null && firstExpense.amount.isNegative()) {
                            transactions.fold(Money.zero(currency)) { acc, t -> acc + t.amount }
                        } else {
                            income - expense
                        }
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(
                            R.id.small_widget_balance,
                            balance.formatForDisplay(useMinimalDecimals = true),
                        )
                        val color =
                            if (balance.isPositive()) {
                                context.getColor(UiR.color.income)
                            } else {
                                context.getColor(UiR.color.expense)
                            }
                        views.setTextColor(R.id.small_widget_balance, color)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                },
                onFailure = { e ->
                    Timber.e(e, "Failed to load transactions")
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.small_widget_balance, "?")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                },
            )
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        scope.cancel()
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, SmallBalanceWidget::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
