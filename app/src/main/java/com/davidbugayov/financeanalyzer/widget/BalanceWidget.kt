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
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.fold
import com.davidbugayov.financeanalyzer.domain.usecase.LoadTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Виджет для отображения текущего баланса, доходов и расходов.
 * Обновляется каждые 30 минут или при запуске приложения.
 * Визуально соответствует компоненту BalanceCard из приложения.
 * При нажатии на виджет открывается приложение.
 *
 * @property loadTransactionsUseCase UseCase для загрузки транзакций
 * @property scope Корутин скоуп для асинхронных операций
 */
class BalanceWidget : AppWidgetProvider(), KoinComponent {

    private val loadTransactionsUseCase: LoadTransactionsUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

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
        scope.launch {
            loadTransactionsUseCase().fold(
                onSuccess = { transactions ->
                    // Рассчитываем баланс, доходы и расходы
                    val income = transactions
                        .filter { transaction -> !transaction.isExpense }
                        .map { transaction -> transaction.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()

                    val expense = transactions
                        .filter { transaction -> transaction.isExpense }
                        .map { transaction -> transaction.amount }
                        .reduceOrNull { acc, money -> acc + money } ?: Money.zero()
                        
                    val balance = income - expense

                    // Обновляем UI виджета
                    withContext(Dispatchers.Main) {
                        // Форматируем числа для компактного отображения
                        val formattedBalance = balance.format(false)
                        val formattedIncome = income.format(false)
                        val formattedExpense = expense.format(false)

                        // Обновляем виджет с новыми данными
                        views.setTextViewText(R.id.widget_balance, formattedBalance)
                        views.setTextViewText(R.id.widget_income, formattedIncome)
                        views.setTextViewText(R.id.widget_expense, formattedExpense)

                        // Устанавливаем цвет баланса в зависимости от его значения
                        val balanceColor = if (balance >= Money.zero()) {
                            0xFF4CAF50.toInt() // Green
                        } else {
                            0xFFF44336.toInt() // Red
                        }
                        views.setTextColor(R.id.widget_balance, balanceColor)

                        // Применяем изменения к виджету
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                },
                onFailure = { exception: Throwable ->
                    Timber.e(exception, "Failed to load transactions for widget")
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_balance, "?")
                        views.setTextViewText(R.id.widget_income, "?")
                        views.setTextViewText(R.id.widget_expense, "?")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            )
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
                ComponentName(context, BalanceWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
} 