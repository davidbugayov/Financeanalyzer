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
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Виджет для отображения текущего баланса, доходов и расходов.
 * Обновляется каждые 30 минут или при нажатии на виджет.
 * Визуально соответствует компоненту BalanceCard из приложения.
 * При нажатии на виджет открывается приложение.
 */
class BalanceWidget : AppWidgetProvider(), KoinComponent {

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
        val views = RemoteViews(context.packageName, R.layout.balance_widget_layout)
        
        // Создаем Intent для запуска главной активности приложения
        val launchAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchAppIntent != null) {
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            // Создаем PendingIntent для запуска приложения
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    context,
                    0,
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    context,
                    0,
                    launchAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            
            // Устанавливаем обработчик нажатия на весь виджет
            views.setOnClickPendingIntent(R.id.widget_balance, pendingIntent)
            
            // Также добавляем обработчик на весь контейнер для лучшей отзывчивости
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }

        // Загружаем данные о транзакциях в фоновом потоке
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val transactions = loadTransactionsUseCase()
                
                // Рассчитываем баланс, доходы и расходы
                val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
                val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
                val balance = income - expense
                
                // Обновляем UI виджета в главном потоке
                CoroutineScope(Dispatchers.Main).launch {
                    // Форматируем числа с двумя знаками после запятой
                    val formattedBalance = String.format("%.2f", balance)
                    val formattedIncome = String.format("%.2f", income)
                    val formattedExpense = String.format("%.2f", expense)
                    
                    // Устанавливаем значения в виджет
                    views.setTextViewText(R.id.widget_balance, "₽ $formattedBalance")
                    views.setTextViewText(R.id.widget_income, "₽ $formattedIncome")
                    views.setTextViewText(R.id.widget_expense, "₽ $formattedExpense")
                    
                    // Устанавливаем цвет баланса в зависимости от значения
                    if (balance >= 0) {
                        views.setTextColor(R.id.widget_balance, 0xFF4CAF50.toInt())
                    } else {
                        views.setTextColor(R.id.widget_balance, 0xFFF44336.toInt())
                    }
                    
                    // Обновляем виджет
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                // В случае ошибки показываем сообщение об ошибке
                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_balance, "Ошибка загрузки")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
    
    /**
     * Вызывается при получении широковещательного сообщения
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Если это запрос на обновление, запускаем сервис обновления
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // Обновляем данные виджета
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, BalanceWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
} 