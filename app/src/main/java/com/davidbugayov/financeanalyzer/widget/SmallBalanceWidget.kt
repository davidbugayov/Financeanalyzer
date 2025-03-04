package com.davidbugayov.financeanalyzer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import com.davidbugayov.financeanalyzer.util.formatNumberWithCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Компактный виджет для отображения текущего баланса.
 * Размер 1x1, отображает только сумму баланса.
 * При нажатии на виджет открывается приложение.
 * Обновляется каждые 30 минут или при запуске приложения.
 */
class SmallBalanceWidget : AppWidgetProvider(), KoinComponent {

    private val loadTransactionsUseCase: LoadTransactionsUseCase by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Обновляем каждый экземпляр виджета
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Обновляет виджет с актуальными данными о балансе
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Создаем RemoteViews для обновления виджета
        val views = RemoteViews(context.packageName, R.layout.small_balance_widget_layout)
        
        // Создаем Intent для запуска главной активности приложения
        val launchAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchAppIntent != null) {
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            // Создаем PendingIntent для запуска приложения
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    context,
                    1, // Используем другой requestCode, чтобы не конфликтовать с основным виджетом
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    context,
                    1,
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            
            // Устанавливаем обработчик нажатия на весь виджет
            views.setOnClickPendingIntent(R.id.small_widget_container, pendingIntent)
        }

        // Загружаем данные о транзакциях в фоновом потоке
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val transactions = loadTransactionsUseCase()
                
                // Рассчитываем баланс
                val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
                val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
                val balance = income - expense
                
                // Обновляем UI виджета
                withContext(Dispatchers.Main) {
                    // Форматируем баланс для компактного отображения
                    val formattedBalance = formatNumberWithCurrency(balance)
                    
                    // Устанавливаем значение в виджет
                    views.setTextViewText(R.id.small_widget_balance, formattedBalance)
                    
                    // Устанавливаем цвет баланса в зависимости от значения
                    val balanceColor = if (balance >= 0) {
                        0xFF4CAF50.toInt() // Green
                    } else {
                        0xFFF44336.toInt() // Red
                    }
                    views.setTextColor(R.id.small_widget_balance, balanceColor)
                    
                    // Обновляем виджет
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.small_widget_balance, "?")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                coroutineContext.cancel()
            }
        }
    }
    
    /**
     * Вызывается при получении широковещательного сообщения
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Если это запрос на обновление, обновляем данные виджета
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SmallBalanceWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
} 